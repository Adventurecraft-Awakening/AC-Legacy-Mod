package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.extension.world.ExWorld;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Options;
import net.minecraft.world.level.Level;
import org.lwjgl.input.Keyboard;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

@SuppressWarnings("unused")
public class ScriptKeyboard {

    private Options gameSettings;
    public String keyForwardScript = "";
    public String keyBackScript = "";
    public String keyLeftScript = "";
    public String keyRightScript = "";
    public String keyJumpScript = "";
    public String keySneakScript = "";
    String allKeys;
    Int2ObjectOpenHashMap<String> keyBinds;
    Level world;
    Scriptable scope;

    ScriptKeyboard(Level var1, Options var2, Scriptable var3) {
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

        boolean isPut = false;
        String binding = this.keyBinds.get(var1);
        if (binding != null) {
            isPut = true;
            ScriptableObject.putProperty(scope, "keyID", var1);
            ((ExWorld) this.world).getScriptHandler().runScript(binding, scope);
        }

        if (this.allKeys != null) {
            if (!isPut) {
                ScriptableObject.putProperty(scope, "keyID", var1);
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

    public boolean processPlayerKeyPress(int key, boolean isDown) {
        String script = "";
        if (key == this.gameSettings.keyUp.key) {
            script = this.keyForwardScript;
        }

        if (key == this.gameSettings.keyDown.key) {
            script = this.keyBackScript;
        }

        if (key == this.gameSettings.keyLeft.key) {
            script = this.keyLeftScript;
        } else if (key == this.gameSettings.keyRight.key) {
            script = this.keyRightScript;
        } else if (key == this.gameSettings.keyJump.key) {
            script = this.keyJumpScript;
        } else if (key == this.gameSettings.keySneak.key) {
            script = this.keySneakScript;
        }

        boolean result = true;
        if (script != null && !script.isEmpty()) {
            result = this.runScript(script, key, isDown);
        }
        return result;
    }

    private boolean runScript(String script, int key, boolean isDown) {
        var world = (ExWorld) this.world;
        ScriptableObject.putProperty(world.getScope(), "keyID", key);
        ScriptableObject.putProperty(this.scope, "keyState", isDown);
        Object var5 = world.getScriptHandler().runScript(script, this.scope);
        return var5 instanceof Boolean b ? b : true;
    }
}
