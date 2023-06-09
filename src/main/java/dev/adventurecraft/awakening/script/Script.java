package dev.adventurecraft.awakening.script;

import java.io.Reader;
import java.util.Iterator;
import java.util.LinkedList;

import dev.adventurecraft.awakening.extension.client.gui.ExInGameHud;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.world.World;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.ContinuationPending;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

@SuppressWarnings("unused")
public class Script {

    static final String SCRIPT_PACKAGE = "dev.adventurecraft.awakening.script";

    public Scriptable globalScope;
    Scriptable curScope;
    Scriptable runScope;
    Context cx = ContextFactory.getGlobal().enterContext();
    ScriptTime time;
    ScriptWorld world;
    ScriptEntityPlayer player;
    ScriptChat chat;
    ScriptWeather weather;
    ScriptEffect effect;
    ScriptSound sound;
    ScriptUI ui;
    ScriptScript script;
    public ScriptKeyboard keyboard;
    LinkedList<ScriptContinuation> sleepingScripts = new LinkedList<>();
    LinkedList<ScriptContinuation> removeMe = new LinkedList<>();
    static boolean shutterSet = false;

    public Script(World var1) {
        this.cx.setOptimizationLevel(-1);
        if (!shutterSet) {
            this.cx.setClassShutter(fullClassName -> fullClassName.startsWith(SCRIPT_PACKAGE) ||
                fullClassName.equals("java.lang.Object") ||
                fullClassName.equals("java.lang.String") ||
                fullClassName.equals("java.lang.Double") ||
                fullClassName.equals("java.lang.Boolean"));
            shutterSet = true;
        }

        this.globalScope = this.cx.initStandardObjects();
        this.runScope = this.cx.newObject(this.globalScope);
        this.runScope.setParentScope(this.globalScope);
        this.time = new ScriptTime(var1);
        this.world = new ScriptWorld(var1);
        this.chat = new ScriptChat();
        this.weather = new ScriptWeather(var1);
        this.effect = new ScriptEffect(var1, Minecraft.instance.worldRenderer);
        this.sound = new ScriptSound(Minecraft.instance.soundHelper);
        this.ui = new ScriptUI();
        this.script = new ScriptScript(var1);
        this.keyboard = new ScriptKeyboard(var1, Minecraft.instance.options, this.getNewScope());

        this.addObject("time", this.time);
        this.addObject("world", this.world);
        this.addObject("chat", this.chat);
        this.addObject("weather", this.weather);
        this.addObject("effect", this.effect);
        this.addObject("sound", this.sound);
        this.addObject("ui", this.ui);
        this.addObject("screen", ((ExInGameHud) Minecraft.instance.overlay).getScriptUI());
        this.addObject("script", this.script);
        this.addObject("keyboard", this.keyboard);
        this.addObject("hitEntity", null);
        this.addObject("hitBlock", null);

        this.runString(String.format("Item = Packages.%s.ScriptItem", SCRIPT_PACKAGE));
        this.runString(String.format("UILabel = Packages.%s.ScriptUILabel", SCRIPT_PACKAGE));
        this.runString(String.format("UISprite = Packages.%s.ScriptUISprite", SCRIPT_PACKAGE));
        this.runString(String.format("UIRect = Packages.%s.ScriptUIRect", SCRIPT_PACKAGE));
        this.runString(String.format("UIContainer = Packages.%s.ScriptUIContainer", SCRIPT_PACKAGE));
        this.runString(String.format("UIContainer = Packages.%s.ScriptUIContainer", SCRIPT_PACKAGE));
        this.runString(String.format("Model = Packages.%s.ScriptModel", SCRIPT_PACKAGE));
        this.runString(String.format("Vec3 = Packages.%s.ScriptVec3", SCRIPT_PACKAGE));
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

    public void initPlayer(AbstractClientPlayerEntity player) {
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
            Minecraft.instance.overlay.addChatMessage("Javascript Error: " + e.getMessage());
            return null;
        }
    }

    public org.mozilla.javascript.Script compileReader(Reader sourceReader, String sourceName) {
        try {
            return this.cx.compileReader(sourceReader, sourceName, 1, null);
        } catch (Exception e) {
            Minecraft.instance.overlay.addChatMessage("Javascript Error: " + e.getMessage());
            return null;
        }
    }

    public Scriptable getNewScope() {
        Scriptable var1 = this.cx.newObject(this.globalScope);
        var1.setParentScope(this.globalScope);
        return var1;
    }

    public Object runScript(org.mozilla.javascript.Script script, Scriptable scope) {
        if (this.curScope != null) {
            return script.exec(this.cx, this.curScope);
        }

        try {
            this.curScope = scope;
            Object result = this.cx.executeScriptWithContinuations(script, scope);
            return result;
        } catch (ContinuationPending e) {
        } catch (Exception e) {
            Minecraft.instance.overlay.addChatMessage("Javascript Error: " + e.getMessage());
        } finally {
            this.curScope = null;
        }
        return null;
    }

    public void wakeupScripts(long currentTime) {
        Iterator<ScriptContinuation> continuations = this.sleepingScripts.iterator();

        while (continuations.hasNext()) {
            ScriptContinuation continuation = continuations.next();
            if (continuation.wakeUp <= currentTime) {
                this.removeMe.add(continuation);
            }
        }

        continuations = this.removeMe.iterator();

        while (continuations.hasNext()) {
            ScriptContinuation continuation = continuations.next();
            this.sleepingScripts.remove(continuation);

            try {
                this.curScope = continuation.scope;
                this.cx.resumeContinuation(continuation.contituation, continuation.scope, null);
            } catch (ContinuationPending e) {
            } catch (Exception e) {
                Minecraft.instance.overlay.addChatMessage("Javascript Error: " + e.getMessage());
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
}
