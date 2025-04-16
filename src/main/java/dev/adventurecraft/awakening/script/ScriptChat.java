package dev.adventurecraft.awakening.script;

import net.minecraft.client.Minecraft;

public class ScriptChat {

    public void print(String var1, Object... var2) {
        var1 = String.format(var1, var2);
        Minecraft.instance.gui.addMessage(var1);
    }
}
