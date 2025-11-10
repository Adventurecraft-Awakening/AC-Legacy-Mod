package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.extension.client.gui.ExInGameHud;
import dev.adventurecraft.awakening.extension.client.options.ExGameOptions;
import dev.adventurecraft.awakening.primitives.TickTime;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.Level;
import org.mozilla.javascript.*;

import java.io.Closeable;
import java.util.*;

@SuppressWarnings("unused")
public class Script implements Closeable {

    static final String SCRIPT_PACKAGE = "dev.adventurecraft.awakening.script";

    private static final Set<String> allowedClassNames = new ObjectOpenHashSet<>();

    static {
        allowedClassNames.add("java.lang.Object");
        allowedClassNames.add("java.lang.Character");
        allowedClassNames.add("java.lang.String");
        allowedClassNames.add("java.lang.Float");
        allowedClassNames.add("java.lang.Double");
        allowedClassNames.add("java.lang.Boolean");
        allowedClassNames.add("java.lang.Byte");
        allowedClassNames.add("java.lang.Short");
        allowedClassNames.add("java.lang.Integer");
        allowedClassNames.add("java.lang.Long");
        allowedClassNames.add("org.mozilla.javascript.ConsString");
    }

    public final Scriptable globalScope;
    Scriptable curScope;
    final Scriptable runScope;
    final Context cx;

    private final Timers timers;

    final ScriptTime time;
    final ScriptWorld world;
    ScriptEntityPlayer player;
    final ScriptChat chat;
    final ScriptWeather weather;
    final ScriptEffect effect;
    final ScriptParticle particle;
    final ScriptSound sound;
    final ScriptUI ui;
    final ScriptRenderer renderer;
    final ScriptScript script;
    public final ScriptKeyboard keyboard;
    final List<ContinuationPending> continuations = new ArrayList<>();
    final List<ContinuationPending> newContinuations = new ArrayList<>();

    public static class CustomContextFactory extends ContextFactory {

        @Override
        protected Context makeContext() {
            Context cx = super.makeContext();
            cx.setLanguageVersion(Context.VERSION_ECMASCRIPT);
            cx.setInterpretedMode(true);
            cx.setClassShutter(fullClassName -> fullClassName.startsWith(SCRIPT_PACKAGE) ||
                allowedClassNames.contains(fullClassName));
            return cx;
        }
    }

    public Script(Level level) {
        var gameOptions = (ExGameOptions) Minecraft.instance.options;

        this.cx = ContextFactory.getGlobal().enterContext();

        this.globalScope = gameOptions.getAllowJavaInScript()
            ? this.cx.initStandardObjects(null, false)
            : this.cx.initSafeStandardObjects(null, false);
        this.curScope = this.globalScope;
        this.runScope = this.cx.newObject(this.globalScope);
        this.runScope.setParentScope(this.globalScope);

        this.timers = new Timers() {
            protected @Override long getTimeMillis() {
                return time.getTickCount() * TickTime.MILLIS_PER_TICK;
            }
        };
        this.timers.install(this.globalScope);

        this.time = new ScriptTime(level);
        this.world = new ScriptWorld(level);
        this.chat = new ScriptChat();
        this.weather = new ScriptWeather(level);
        this.effect = new ScriptEffect(level, Minecraft.instance.levelRenderer);
        this.particle = new ScriptParticle(Minecraft.instance.levelRenderer);
        this.sound = new ScriptSound(level, Minecraft.instance.soundEngine);
        this.ui = new ScriptUI();
        this.script = new ScriptScript(level);
        this.keyboard = new ScriptKeyboard(level, Minecraft.instance.options, this.getNewScope());
        this.renderer = new ScriptRenderer(Minecraft.instance.levelRenderer);

        this.addObject("time", this.time);
        this.addObject("world", this.world);
        this.addObject("chat", this.chat);
        this.addObject("weather", this.weather);
        this.addObject("effect", this.effect);
        this.addObject("particle", this.particle);
        this.addObject("sound", this.sound);
        this.addObject("ui", this.ui);
        this.addObject("screen", ((ExInGameHud) Minecraft.instance.gui).getScriptUI());
        this.addObject("script", this.script);
        this.addObject("keyboard", this.keyboard);
        this.addObject("hitEntity", null);
        this.addObject("hitBlock", null);
        this.addObject("renderer", this.renderer);

        if (gameOptions.getAllowJavaInScript()) {
            // Alias our package as `net.minecraft.script` for back-compat.
            String initStr = String.join(
                "\n", new String[] {
                    String.format("net = { minecraft: { script: Packages.%s } };", SCRIPT_PACKAGE),
                }
            );
            this.runString(initStr, "<init>", this.globalScope);
        }

        defineClass("Item", ScriptItem.class);
        defineClass("UILabel", ScriptUILabel.class);
        defineClass("UISprite", ScriptUISprite.class);
        defineClass("UIRect", ScriptUIRect.class);
        defineClass("UIContainer", ScriptUIContainer.class);
        defineClass("Model", ScriptModel.class);
        defineClass("ModelBlockbench", ScriptModelBlockbench.class);
        defineClass("Vec3", ScriptVec3.class);
        defineClass("Vec4", ScriptVec4.class);
        defineClass("VecRot", ScriptVecRot.class);
    }

    public Context getContext() {
        return this.cx;
    }

    private <T> void defineClass(String name, Class<T> clazz) {
        var instance = new NativeJavaClass(this.globalScope, clazz);
        ScriptableObject.putProperty(this.globalScope, name, instance);
    }

    public void addObject(String name, Object value) {
        Object tmp = Context.javaToJS(value, this.globalScope);
        ScriptableObject.putProperty(this.globalScope, name, tmp);
    }

    public void initPlayer(LocalPlayer player) {
        this.player = new ScriptEntityPlayer(player);
        Object tmp = Context.javaToJS(this.player, this.globalScope);
        ScriptableObject.putProperty(this.globalScope, "player", tmp);
    }

    public String runString(String sourceCode, String sourceName) {
        // TODO: is runScope needed? why not just run in globalScope?
        return this.runString(sourceCode, sourceName, this.runScope);
    }

    private String runString(String sourceCode, String sourceName, Scriptable scope) {
        org.mozilla.javascript.Script script = this.compileString(sourceCode, sourceName);
        if (script == null) {
            return null;
        }
        Object result = this.runScript(script, scope);
        if (result == null) {
            return null;
        }

        try {
            return Context.toString(result);
        }
        catch (RhinoException e) {
            this.printRhinoException(e);
            return null;
        }
    }

    public org.mozilla.javascript.Script compileString(String sourceCode, String sourceName) {
        try {
            return this.cx.compileString(sourceCode, sourceName, 1, null);
        }
        catch (Exception e) {
            Minecraft.instance.gui.addMessage("(JS) Compile: " + e.getMessage());
            return null;
        }
    }

    public Scriptable getNewScope() {
        Scriptable scope = this.cx.newObject(this.globalScope);
        scope.setParentScope(this.globalScope);
        return scope;
    }

    public Object runScript(org.mozilla.javascript.Script script, Scriptable scope) {
        // TODO: move AC_JScriptInfo into here?
        //       it makes more sense, and allows us to track <init> and <cmd> scripts

        Scriptable prevScope = this.curScope;
        try {
            if (scope != null) {
                this.curScope = scope;
            }
            return this.cx.executeScriptWithContinuations(script, this.curScope);
        }
        catch (ContinuationPending c) {
            this.newContinuations.add(c);
        }
        catch (RhinoException e) {
            this.printRhinoException(e);
        }
        finally {
            this.curScope = prevScope;
        }
        return null;
    }

    public void processContinuations() {
        this.continuations.addAll(this.newContinuations);
        this.newContinuations.clear();

        if (!this.continuations.isEmpty()) {
            this.executeContinuations(this.time.getTickCount());
        }

        this.timers.runTimers(this.cx, this.globalScope);
    }

    private void executeContinuations(long currentTime) {
        var iterator = this.continuations.iterator();
        while (iterator.hasNext()) {
            var item = iterator.next();
            if (!(item.getApplicationState() instanceof ScriptContinuation(long wakeUp, Scriptable scope))) {
                continue;
            }
            if (wakeUp > currentTime) {
                continue;
            }

            iterator.remove();
            Scriptable prevScope = this.curScope;
            try {
                this.curScope = scope;
                this.cx.resumeContinuation(item.getContinuation(), this.curScope, null);
            }
            catch (ContinuationPending c) {
                this.newContinuations.add(c);
            }
            catch (RhinoException e) {
                this.printRhinoException(e);
            }
            finally {
                this.curScope = prevScope;
            }
        }
    }

    public void sleep(float seconds) {
        int ticks = (int) (20.0F * seconds);
        if (ticks <= 0) {
            return;
        }

        long wakeUp = this.time.getTickCount() + (long) ticks;
        ContinuationPending continuation = this.cx.captureContinuation();
        continuation.setApplicationState(new ScriptContinuation(wakeUp, this.curScope));
        throw continuation;
    }

    @Override
    public void close() {
        this.cx.close();
    }

    private void printRhinoException(RhinoException ex) {
        String message = ex.getMessage();
        Minecraft.instance.gui.addMessage("JS: " + message);

        Exception logEx = ACMod.JS_LOGGER.isTraceEnabled() ? ex : null;
        ACMod.JS_LOGGER.warn("{}\n{}", message, ex.getScriptStackTrace(), logEx);
    }
}
