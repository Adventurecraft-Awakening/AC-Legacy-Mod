package dev.adventurecraft.awakening.script;

import java.util.Iterator;
import java.util.LinkedList;

import dev.adventurecraft.awakening.extension.client.gui.ExInGameHud;
import net.minecraft.client.Minecraft;
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
            this.cx.setClassShutter(var11 -> var11.startsWith(SCRIPT_PACKAGE) ||
                var11.equals("java.lang.Object") ||
                var11.equals("java.lang.String") ||
                var11.equals("java.lang.Double") ||
                var11.equals("java.lang.Boolean"));
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
        Object var2 = Context.javaToJS(this.time, this.globalScope);
        ScriptableObject.putProperty(this.globalScope, "time", var2);
        var2 = Context.javaToJS(this.world, this.globalScope);
        ScriptableObject.putProperty(this.globalScope, "world", var2);
        var2 = Context.javaToJS(this.chat, this.globalScope);
        ScriptableObject.putProperty(this.globalScope, "chat", var2);
        var2 = Context.javaToJS(this.weather, this.globalScope);
        ScriptableObject.putProperty(this.globalScope, "weather", var2);
        var2 = Context.javaToJS(this.effect, this.globalScope);
        ScriptableObject.putProperty(this.globalScope, "effect", var2);
        var2 = Context.javaToJS(this.sound, this.globalScope);
        ScriptableObject.putProperty(this.globalScope, "sound", var2);
        var2 = Context.javaToJS(this.ui, this.globalScope);
        ScriptableObject.putProperty(this.globalScope, "ui", var2);
        var2 = Context.javaToJS(((ExInGameHud) Minecraft.instance.overlay).getScriptUI(), this.globalScope);
        ScriptableObject.putProperty(this.globalScope, "screen", var2);
        var2 = Context.javaToJS(this.script, this.globalScope);
        ScriptableObject.putProperty(this.globalScope, "script", var2);
        var2 = Context.javaToJS(this.keyboard, this.globalScope);
        ScriptableObject.putProperty(this.globalScope, "keyboard", var2);
        var2 = Context.javaToJS(null, this.globalScope);
        ScriptableObject.putProperty(this.globalScope, "hitEntity", var2);
        ScriptableObject.putProperty(this.globalScope, "hitBlock", var2);
        this.runString(String.format("Item = Packages.%s.ScriptItem", SCRIPT_PACKAGE));
        this.runString(String.format("UILabel = Packages.%s.ScriptUILabel", SCRIPT_PACKAGE));
        this.runString(String.format("UISprite = Packages.%s.ScriptUISprite", SCRIPT_PACKAGE));
        this.runString(String.format("UIRect = Packages.%s.ScriptUIRect", SCRIPT_PACKAGE));
        this.runString(String.format("UIContainer = Packages.%s.ScriptUIContainer", SCRIPT_PACKAGE));
        this.runString(String.format("UIContainer = Packages.%s.ScriptUIContainer", SCRIPT_PACKAGE));
        this.runString(String.format("Model = Packages.%s.ScriptModel", SCRIPT_PACKAGE));
        this.runString(String.format("Vec3 = Packages.%s.ScriptVec3", SCRIPT_PACKAGE));
    }

    public void addObject(String var1, Object var2) {
        Object var3 = Context.javaToJS(var2, this.globalScope);
        ScriptableObject.putProperty(this.globalScope, var1, var3);
    }

    /* TODO:
    protected void finalize() {
        Context var10000 = this.cx;
        Context.exit();
    }
    */

    public void initPlayer() {
        this.player = new ScriptEntityPlayer(Minecraft.instance.player);
        Object var1 = Context.javaToJS(this.player, this.globalScope);
        ScriptableObject.putProperty(this.globalScope, "player", var1);
    }

    public String runString(String var1) {
        org.mozilla.javascript.Script var2 = this.compileString(var1, "<cmd>");
        if (var2 != null) {
            Object var3 = this.runScript(var2, this.runScope);
            if (var3 != null) {
                Context var10000 = this.cx;
                return Context.toString(var3);
            }
        }

        return null;
    }

    public org.mozilla.javascript.Script compileString(String var1, String var2) {
        try {
            return this.cx.compileString(var1, var2, 1, (Object) null);
        } catch (Exception var4) {
            Minecraft.instance.overlay.addChatMessage("Javascript Error: " + var4.getMessage());
            return null;
        }
    }

    public Scriptable getNewScope() {
        Scriptable var1 = this.cx.newObject(this.globalScope);
        var1.setParentScope(this.globalScope);
        return var1;
    }

    public Object runScript(org.mozilla.javascript.Script var1, Scriptable var2) {
        if (this.curScope == null) {
            try {
                this.curScope = var2;
                Object var3 = this.cx.executeScriptWithContinuations(var1, var2);
                return var3;
            } catch (ContinuationPending var8) {
            } catch (Exception var9) {
                Minecraft.instance.overlay.addChatMessage("Javascript Error: " + var9.getMessage());
            } finally {
                this.curScope = null;
            }

            return null;
        } else {
            return var1.exec(this.cx, this.curScope);
        }
    }

    public void wakeupScripts(long var1) {
        Iterator<ScriptContinuation> var3 = this.sleepingScripts.iterator();

        ScriptContinuation var4;
        while (var3.hasNext()) {
            var4 = var3.next();
            if (var4.wakeUp <= var1) {
                this.removeMe.add(var4);
            }
        }

        var3 = this.removeMe.iterator();

        while (var3.hasNext()) {
            var4 = var3.next();
            this.sleepingScripts.remove(var4);

            try {
                this.curScope = var4.scope;
                this.cx.resumeContinuation(var4.contituation, var4.scope, null);
            } catch (ContinuationPending var10) {
            } catch (Exception var11) {
                Minecraft.instance.overlay.addChatMessage("Javascript Error: " + var11.getMessage());
            } finally {
                this.curScope = null;
            }
        }

        this.removeMe.clear();
    }

    public void sleep(float var1) {
        int var2 = (int) (20.0F * var1);
        ContinuationPending var3 = this.cx.captureContinuation();
        this.sleepingScripts.add(new ScriptContinuation(var3.getContinuation(), this.time.getTickCount() + (long) var2, this.curScope));
        throw var3;
    }
}
