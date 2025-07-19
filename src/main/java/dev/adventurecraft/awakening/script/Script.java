package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.extension.client.gui.ExInGameHud;
import dev.adventurecraft.awakening.extension.client.options.ExGameOptions;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.Level;
import org.mozilla.javascript.*;

import java.io.*;
import java.util.*;

@SuppressWarnings("unused")
public class Script {

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
    final List<ScriptContinuation> sleepingScripts = new ArrayList<>();
    final List<ScriptContinuation> removeMe = new ArrayList<>();

    public static final ContextFactory contextFactory = new CustomContextFactory();

    static class CustomContextFactory extends ContextFactory {

        @Override
        protected Context makeContext() {
            Context cx = super.makeContext();
            cx.setLanguageVersion(Context.VERSION_ECMASCRIPT);
            cx.setInterpretedMode(true);
            cx.setClassShutter(fullClassName ->
                fullClassName.startsWith(SCRIPT_PACKAGE) || allowedClassNames.contains(fullClassName));
            return cx;
        }
    }

    public Script(Level level) {
        var gameOptions = (ExGameOptions) Minecraft.instance.options;

        this.cx = contextFactory.enterContext();

        this.globalScope = gameOptions.getAllowJavaInScript()
            ? this.cx.initStandardObjects(null, false)
            : this.cx.initSafeStandardObjects(null, false);
        this.runScope = this.cx.newObject(this.globalScope);
        this.runScope.setParentScope(this.globalScope);

        this.time = new ScriptTime(level);
        this.world = new ScriptWorld(level);
        this.chat = new ScriptChat();
        this.weather = new ScriptWeather(level);
        this.effect = new ScriptEffect(level, Minecraft.instance.levelRenderer);
        this.particle = new ScriptParticle(Minecraft.instance.levelRenderer);
        this.sound = new ScriptSound(Minecraft.instance.soundEngine);
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
            String initStr = String.join("\n", new String[]{
                String.format("net = { minecraft: { script: Packages.%s } };", SCRIPT_PACKAGE),
            });
            this.cx.evaluateString(this.globalScope, initStr, "<init>", 0, null);
        }

        defineClass("Item", ScriptItem.class);
        defineClass("UILabel", ScriptUILabel.class);
        defineClass("UISprite", ScriptUISprite.class);
        defineClass("UIRect", ScriptUIRect.class);
        defineClass("UIContainer", ScriptUIContainer.class);
        defineClass("Model", ScriptModel.class);
        defineClass("ModelBlockbench", ScriptModelBlockbench.class);
        defineClass("Vec3", ScriptVec3.class);
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

    /* TODO:
    protected void finalize() {
        Context var10000 = this.cx;
        Context.exit();
    }
    */

    public void initPlayer(LocalPlayer player) {
        this.player = new ScriptEntityPlayer(player);
        Object tmp = Context.javaToJS(this.player, this.globalScope);
        ScriptableObject.putProperty(this.globalScope, "player", tmp);
    }

    public String runString(String sourceCode) {
        org.mozilla.javascript.Script script = this.compileString(sourceCode, "<cmd>");
        if (script != null) {
            Object result = this.runScript(script, this.runScope);
            if (result != null) {
                return Context.toString(result);
            }
        }
        return null;
    }

    public org.mozilla.javascript.Script compileString(String sourceCode, String sourceName) {
        try {
            return this.cx.compileString(sourceCode, sourceName, 1, null);
        } catch (Exception e) {
            Minecraft.instance.gui.addMessage("JS Compile: " + e.getMessage());
            return null;
        }
    }

    public org.mozilla.javascript.Script compileReader(Reader sourceReader, String sourceName)
        throws IOException {
        return this.cx.compileReader(sourceReader, sourceName, 1, null);
    }

    public Scriptable getNewScope() {
        Scriptable var1 = this.cx.newObject(this.globalScope);
        var1.setParentScope(this.globalScope);
        return var1;
    }

    public void setNewCurScope(Scriptable pScope) {
        this.curScope = pScope;
    }

    public Scriptable getCurScope() {
        return this.curScope;
    }

    public Object runScript(org.mozilla.javascript.Script script, Scriptable scope) {
        if (this.curScope != null) {
            return script.exec(this.cx, this.curScope);
        }

        try {
            this.curScope = scope;
            Object result = this.cx.executeScriptWithContinuations(script, scope);
            return result;
        } catch (ContinuationPending ignored) {
        } catch (RhinoException e) {
            this.printRhinoException(e);
        } finally {
            this.curScope = null;
        }
        return null;
    }

    public void wakeupScripts(long currentTime) {

        for (ScriptContinuation continuation : this.sleepingScripts) {
            if (continuation.wakeUp <= currentTime) {
                this.removeMe.add(continuation);
            }
        }

        for (ScriptContinuation continuation : this.removeMe) {
            this.sleepingScripts.remove(continuation);

            try {
                this.curScope = continuation.scope;
                this.cx.resumeContinuation(continuation.contituation, continuation.scope, null);
            } catch (ContinuationPending e) {
            } catch (RhinoException e) {
                this.printRhinoException(e);
            } finally {
                this.curScope = null;
            }
        }

        this.removeMe.clear();
    }

    public void sleep(float seconds) {
        int ticks = (int) (20.0F * seconds);
        ContinuationPending continuation = this.cx.captureContinuation();
        this.sleepingScripts.add(new ScriptContinuation(
            continuation.getContinuation(),
            this.time.getTickCount() + (long) ticks,
            this.curScope));
        throw continuation;
    }

    private void printRhinoException(RhinoException ex) {
        String message = ex.getMessage();
        Minecraft.instance.gui.addMessage("JS: " + message);

        Exception logEx = ACMod.JS_LOGGER.isTraceEnabled() ? ex : null;
        ACMod.JS_LOGGER.warn("{}\n{}", message, ex.getScriptStackTrace(), logEx);
    }
}
