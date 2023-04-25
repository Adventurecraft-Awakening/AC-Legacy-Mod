package dev.adventurecraft.awakening.common;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.world.World;

public class AC_GuiTree extends Screen {
    private int blockX;
    private int blockY;
    private int blockZ;
    private World world;
    AC_TileEntityTree tree;
    GuiSlider2 treeSize;
    float prevValue;

    public AC_GuiTree(World var1, int var2, int var3, int var4, AC_TileEntityTree var5) {
        this.world = var1;
        this.blockX = var2;
        this.blockY = var3;
        this.blockZ = var4;
        this.tree = var5;
    }

    public void tick() {
    }

    public void initVanillaScreen() {
        this.treeSize = new GuiSlider2(4, 4, 4, 10, String.format("Tree Size: %.2f", this.tree.size), (this.tree.size - 0.5F) / 3.5F);
        this.buttons.add(this.treeSize);
        this.prevValue = this.treeSize.sliderValue;
    }

    protected void buttonClicked(ButtonWidget var1) {
    }

    public void render(int var1, int var2, float var3) {
        this.fill(0, 0, this.width, this.height, Integer.MIN_VALUE);
        if (this.prevValue != this.treeSize.sliderValue) {
            this.tree.size = this.treeSize.sliderValue * 3.5F + 0.5F;
            this.treeSize.text = String.format("Tree Size: %.2f", this.tree.size);
            this.world.method_246(this.blockX, this.blockY, this.blockZ);
            this.world.getChunk(this.blockX, this.blockZ).method_885();
        }

        super.render(var1, var2, var3);
    }

    public static void showUI(World var0, int var1, int var2, int var3, AC_TileEntityTree var4) {
        Minecraft.instance.openScreen(new AC_GuiTree(var0, var1, var2, var3, var4));
    }

    public boolean isPauseScreen() {
        return false;
    }
}
