package dev.adventurecraft.awakening.common;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.level.Level;

public class AC_GuiLightBulb extends Screen {
    private int blockX;
    private int blockY;
    private int blockZ;
    private Level world;
    GuiSlider2 lightSlider;
    int lightValue;

    public AC_GuiLightBulb(Level var1, int var2, int var3, int var4) {
        this.world = var1;
        this.blockX = var2;
        this.blockY = var3;
        this.blockZ = var4;
        this.lightValue = var1.getData(var2, var3, var4);
    }

    public void tick() {
    }

    public void init() {
        this.lightSlider = new GuiSlider2(4, 4, 4, 10, String.format("Light Value: %d", this.lightValue), (float) this.lightValue / 15.0F);
        this.buttons.add(this.lightSlider);
    }

    protected void buttonClicked(Button var1) {
    }

    public void render(int var1, int var2, float var3) {
        this.fill(0, 0, this.width, this.height, Integer.MIN_VALUE);
        this.lightValue = (int) (this.lightSlider.sliderValue * 15.0F + 0.5F);
        this.lightSlider.message = String.format("Light Value: %d", this.lightValue);
        if (this.lightValue != this.world.getData(this.blockX, this.blockY, this.blockZ)) {
            this.world.setTileAndData(this.blockX, this.blockY, this.blockZ, 0, 0);
            this.world.setTileAndData(this.blockX, this.blockY, this.blockZ, AC_Blocks.lightBulb.id, this.lightValue);
        }

        super.render(var1, var2, var3);
        this.world.getChunkAt(this.blockX, this.blockZ).markUnsaved();
    }

    public static void showUI(Level var0, int var1, int var2, int var3) {
        Minecraft.instance.setScreen(new AC_GuiLightBulb(var0, var1, var2, var3));
    }

    public boolean isPauseScreen() {
        return false;
    }
}
