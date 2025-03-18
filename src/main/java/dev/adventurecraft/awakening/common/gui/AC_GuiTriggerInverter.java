package dev.adventurecraft.awakening.common.gui;

import dev.adventurecraft.awakening.common.AC_Blocks;
import dev.adventurecraft.awakening.common.AC_TileEntityTriggerInverter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.level.Level;

public class AC_GuiTriggerInverter extends Screen {
    private AC_TileEntityTriggerInverter trigger;
    private int blockX;
    private int blockY;
    private int blockZ;
    private Level world;

    public AC_GuiTriggerInverter(Level var1, int var2, int var3, int var4, AC_TileEntityTriggerInverter var5) {
        this.world = var1;
        this.blockX = var2;
        this.blockY = var3;
        this.blockZ = var4;
        this.trigger = var5;
    }

    public void tick() {
    }

    public void init() {
        this.buttons.add(new OptionButton(0, 4, 40, "Use Current Selection"));
    }

    protected void buttonClicked(Button var1) {
        int var2 = this.world.getTile(this.blockX, this.blockY, this.blockZ);
        if (var2 == AC_Blocks.triggerInverter.id) {
            AC_Blocks.triggerInverter.setTriggerToSelection(this.world, this.blockX, this.blockY, this.blockZ);
        }

        this.world.getChunkAt(this.blockX, this.blockZ).markUnsaved();
    }

    public void render(int var1, int var2, float var3) {
        this.fill(0, 0, this.width, this.height, Integer.MIN_VALUE);
        this.drawString(this.font, String.format("Min: (%d, %d, %d)", this.trigger.minX, this.trigger.minY, this.trigger.minZ), 4, 4, 14737632);
        this.drawString(this.font, String.format("Max: (%d, %d, %d)", this.trigger.maxX, this.trigger.maxY, this.trigger.maxZ), 4, 24, 14737632);
        super.render(var1, var2, var3);
    }

    public static void showUI(Level var0, int var1, int var2, int var3, AC_TileEntityTriggerInverter var4) {
        Minecraft.instance.setScreen(new AC_GuiTriggerInverter(var0, var1, var2, var3, var4));
    }

    public boolean isPauseScreen() {
        return false;
    }
}
