package dev.adventurecraft.awakening.common.gui;

import dev.adventurecraft.awakening.tile.entity.AC_TileEntityTree;
import dev.adventurecraft.awakening.common.GuiSlider2;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.level.Level;

public class AC_GuiTree extends Screen {
    AC_TileEntityTree tree;
    GuiSlider2 treeSize;
    float prevValue;

    public AC_GuiTree(AC_TileEntityTree entity) {
        this.tree = entity;
    }

    public void tick() {
    }

    public void init() {
        this.treeSize = new GuiSlider2(
            4, 4, 4, 10,
            String.format("Tree Size: %.2f", this.tree.size),
            (this.tree.size - 0.5F) / 3.5F);
        this.buttons.add(this.treeSize);

        this.prevValue = this.treeSize.sliderValue;
    }

    protected void buttonClicked(Button btn) {
    }

    public void render(int mouseX, int mouseY, float tick) {
        this.fill(0, 0, this.width, this.height, Integer.MIN_VALUE);

        if (this.prevValue != this.treeSize.sliderValue) {
            AC_TileEntityTree tree = this.tree;
            tree.size = this.treeSize.sliderValue * 3.5F + 0.5F;
            this.treeSize.message = String.format("Tree Size: %.2f", this.tree.size);
            tree.level.setTileDirty(tree.x, tree.y, tree.z);
            tree.setChanged();
        }

        super.render(mouseX, mouseY, tick);
    }

    public static void showUI(AC_TileEntityTree entity) {
        Minecraft.instance.setScreen(new AC_GuiTree(entity));
    }

    public boolean isPauseScreen() {
        return false;
    }
}
