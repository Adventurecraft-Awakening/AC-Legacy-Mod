package dev.adventurecraft.awakening.common;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.world.World;

public class AC_GuiLightBulb extends Screen {
    private int blockX;
    private int blockY;
    private int blockZ;
    private World world;
    GuiSlider2 lightSlider;
    int lightValue;

    public AC_GuiLightBulb(World var1, int var2, int var3, int var4) {
        this.world = var1;
        this.blockX = var2;
        this.blockY = var3;
        this.blockZ = var4;
        this.lightValue = var1.getBlockMeta(var2, var3, var4);
    }

    public void tick() {
    }

    public void initVanillaScreen() {
        this.lightSlider = new GuiSlider2(4, 4, 4, 10, String.format("Light Value: %d", this.lightValue), (float) this.lightValue / 15.0F);
        this.buttons.add(this.lightSlider);
    }

    protected void buttonClicked(ButtonWidget var1) {
    }

    public void render(int var1, int var2, float var3) {
        this.fill(0, 0, this.width, this.height, Integer.MIN_VALUE);
        this.lightValue = (int) (this.lightSlider.sliderValue * 15.0F + 0.5F);
        this.lightSlider.text = String.format("Light Value: %d", this.lightValue);
        if (this.lightValue != this.world.getBlockMeta(this.blockX, this.blockY, this.blockZ)) {
            this.world.placeBlockWithMetaData(this.blockX, this.blockY, this.blockZ, 0, 0);
            this.world.placeBlockWithMetaData(this.blockX, this.blockY, this.blockZ, AC_Blocks.lightBulb.id, this.lightValue);
        }

        super.render(var1, var2, var3);
        this.world.getChunk(this.blockX, this.blockZ).method_885();
    }

    public static void showUI(World var0, int var1, int var2, int var3) {
        Minecraft.instance.openScreen(new AC_GuiLightBulb(var0, var1, var2, var3));
    }

    public boolean isPauseScreen() {
        return false;
    }
}
