package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.common.gui.AC_GuiUrlRequest;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.world.level.Level;

@SuppressWarnings("unused")
public class ScriptScript {

    Level world;

    ScriptScript(Level var1) {
        this.world = var1;
    }

    public Object runScript(String var1) {
        return ((ExWorld) this.world).getScriptHandler().runScript(var1, null);
    }

    public void openUrl(String var1) {
        AC_GuiUrlRequest.showUI(var1);
    }

    public void openUrl(String var1, String var2) {
        AC_GuiUrlRequest.showUI(var1, var2);
    }
}
