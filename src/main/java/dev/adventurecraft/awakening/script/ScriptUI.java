package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.extension.client.gui.ExInGameHud;
import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ScreenSizeCalculator;

@SuppressWarnings("unused")
public class ScriptUI {

    Minecraft mc = Minecraft.instance;

    public int getWidth() {
        ScreenSizeCalculator var1 = new ScreenSizeCalculator(this.mc.options, this.mc.width, this.mc.height);
        return var1.getWidth();
    }

    public int getHeight() {
        ScreenSizeCalculator var1 = new ScreenSizeCalculator(this.mc.options, this.mc.width, this.mc.height);
        return var1.getHeight();
    }

    public int getStringWidth(String var1) {
        return this.mc.font.width(var1);
    }

    public int getScale() {
        return this.mc.options.guiScale;
    }

    public boolean getGUIHidden() {
        return this.mc.options.hideGui;
    }

    public void setGUIHidden(boolean var1) {
        this.mc.options.hideGui = var1;
    }

    public boolean getThirdPerson() {
        return this.mc.options.thirdPersonView;
    }

    public void setThirdPerson(boolean var1) {
        this.mc.options.thirdPersonView = var1;
    }

    public boolean getFancyGraphics() {
        return this.mc.options.fancyGraphics;
    }

    public boolean getHudEnabled() {
        return ((ExWorldProperties)this.mc.level.levelData).getHudEnabled();
    }

    public void setHudEnabled(boolean var1) {
        ((ExInGameHud) this.mc.gui).setHudEnabled(var1);
        // Set it in properties
        ((ExWorldProperties)this.mc.level.levelData).setHudEnabled(var1);
    }
}
