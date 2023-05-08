package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.extension.world.ExWorld;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.options.GameOptions;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

@SuppressWarnings("unused")
public class ScriptKeyboard {

    private GameOptions gameSettings;
    public String keyForwardScript = "";
    public String keyBackScript = "";
    public String keyLeftScript = "";
    public String keyRightScript = "";
    public String keyJumpScript = "";
    public String keySneakScript = "";
    String allKeys;
    Int2ObjectOpenHashMap<String> keyBinds;
    World world;
    Scriptable scope;

    ScriptKeyboard(World var1, GameOptions var2, Scriptable var3) {
        this.world = var1;
        this.keyBinds = new Int2ObjectOpenHashMap<>();
        this.allKeys = null;
        this.scope = var3;
        this.gameSettings = var2;
    }

    public void bindKey(int var1, String var2) {
        this.keyBinds.put(var1, var2);
    }

    public void unbindKey(int var1) {
        this.keyBinds.remove(var1);
    }

    public void bindAllKeyScript(String var1) {
        this.allKeys = var1;
    }

    public void unbindAllKeyScript() {
        this.allKeys = null;
    }

    public void processKeyPress(int var1) {
        Scriptable scope = ((ExWorld) this.world).getScope();

        boolean var2 = false;
        String var3 = this.keyBinds.get(var1);
        Object var4;
        if (var3 != null) {
            var2 = true;
            var4 = Context.javaToJS(var1, scope);
            ScriptableObject.putProperty(scope, "keyID", var4);
            ((ExWorld) this.world).getScriptHandler().runScript(var3, scope);
        }

        if (this.allKeys != null) {
            if (!var2) {
                var4 = Context.javaToJS(var1, scope);
                ScriptableObject.putProperty(scope, "keyID", var4);
            }

            ((ExWorld) this.world).getScriptHandler().runScript(this.allKeys, scope);
        }
    }

    public boolean isKeyDown(int var1) {
        return Keyboard.isKeyDown(var1);
    }

    public String getKeyName(int var1) {
        return Keyboard.getKeyName(var1);
    }

    public int getKeyID(String var1) {
        return Keyboard.getKeyIndex(var1);
    }

    public boolean processPlayerKeyPress(int var1, boolean var2) {
        boolean var3 = true;
        String var4 = "";
        if (var1 == this.gameSettings.forwardKey.key) {
            var4 = this.keyForwardScript;
        }

        if (var1 == this.gameSettings.backKey.key) {
            var4 = this.keyBackScript;
        }

        if (var1 == this.gameSettings.leftKey.key) {
            var4 = this.keyLeftScript;
        } else if (var1 == this.gameSettings.rightKey.key) {
            var4 = this.keyRightScript;
        } else if (var1 == this.gameSettings.jumpKey.key) {
            var4 = this.keyJumpScript;
        } else if (var1 == this.gameSettings.sneakKey.key) {
            var4 = this.keySneakScript;
        }

        if (var4 != null && !var4.equals("")) {
            var3 = this.runScript(var4, var1, var2);
        }

        return var3;
    }

    private boolean runScript(String var1, int var2, boolean var3) {
        Object var4 = Context.javaToJS(var2, ((ExWorld) this.world).getScope());
        ScriptableObject.putProperty(((ExWorld) this.world).getScope(), "keyID", var4);
        var4 = Context.javaToJS(var3, this.scope);
        ScriptableObject.putProperty(this.scope, "keyState", var4);
        Object var5 = ((ExWorld) this.world).getScriptHandler().runScript(var1, this.scope);
        return var5 instanceof Boolean b ? b : true;
    }
}
