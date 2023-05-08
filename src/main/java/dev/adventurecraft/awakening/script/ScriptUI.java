package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.extension.client.gui.ExInGameHud;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ScreenScaler;

@SuppressWarnings("unused")
public class ScriptUI {

    Minecraft mc = Minecraft.instance;

    public int getWidth() {
        ScreenScaler var1 = new ScreenScaler(this.mc.options, this.mc.actualWidth, this.mc.actualHeight);
        return var1.getScaledWidth();
    }

    public int getHeight() {
        ScreenScaler var1 = new ScreenScaler(this.mc.options, this.mc.actualWidth, this.mc.actualHeight);
        return var1.getScaledHeight();
    }

    public int getStringWidth(String var1) {
        return this.mc.textRenderer.getTextWidth(var1);
    }

    public int getScale() {
        return this.mc.options.guiScale;
    }

    public boolean getGUIHidden() {
        return this.mc.options.hideHud;
    }

    public void setGUIHidden(boolean var1) {
        this.mc.options.hideHud = var1;
    }

    public boolean getThirdPerson() {
        return this.mc.options.thirdPerson;
    }

    public void setThirdPerson(boolean var1) {
        this.mc.options.thirdPerson = var1;
    }

    public boolean getFancyGraphics() {
        return this.mc.options.fancyGraphics;
    }

    public boolean getHudEnabled() {
        return ((ExInGameHud) this.mc.overlay).getHudEnabled();
    }

    public void setHudEnabled(boolean var1) {
        ((ExInGameHud) this.mc.overlay).setHudEnabled(var1);
    }
}
