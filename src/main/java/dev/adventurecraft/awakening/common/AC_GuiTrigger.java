package dev.adventurecraft.awakening.common;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.level.Level;

public class AC_GuiTrigger extends Screen {
    private AC_TileEntityTrigger trigger;
    private int blockX;
    private int blockY;
    private int blockZ;
    private Level world;

    public AC_GuiTrigger(Level var1, int var2, int var3, int var4, AC_TileEntityTrigger var5) {
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
        OptionButton var1 = new OptionButton(1, 4, 60, "Trigger Target");
        if (this.trigger.resetOnTrigger) {
            var1.message = "Reset Target";
        }

        this.buttons.add(var1);
    }

    protected void buttonClicked(Button var1) {
        int var2;
        if (var1.id == 0) {
            var2 = this.world.getTile(this.blockX, this.blockY, this.blockZ);
            if (var2 == AC_Blocks.triggerBlock.id) {
                AC_Blocks.triggerBlock.setTriggerToSelection(this.world, this.blockX, this.blockY, this.blockZ);
            }
        } else if (var1.id == 1) {
            var2 = this.world.getTile(this.blockX, this.blockY, this.blockZ);
            if (var2 == AC_Blocks.triggerBlock.id) {
                AC_Blocks.triggerBlock.setTriggerReset(this.world, this.blockX, this.blockY, this.blockZ, !this.trigger.resetOnTrigger);
            }

            if (this.trigger.resetOnTrigger) {
                var1.message = "Reset Target";
            } else {
                var1.message = "Trigger Target";
            }
        }

        this.world.getChunkAt(this.blockX, this.blockZ).markUnsaved();
    }

    public void render(int var1, int var2, float var3) {
        this.fill(0, 0, this.width, this.height, Integer.MIN_VALUE);
        this.drawString(this.font, String.format("Min: (%d, %d, %d)", this.trigger.minX, this.trigger.minY, this.trigger.minZ), 4, 4, 14737632);
        this.drawString(this.font, String.format("Max: (%d, %d, %d)", this.trigger.maxX, this.trigger.maxY, this.trigger.maxZ), 4, 24, 14737632);
        super.render(var1, var2, var3);
    }

    public static void showUI(Level var0, int var1, int var2, int var3, AC_TileEntityTrigger var4) {
        Minecraft.instance.setScreen(new AC_GuiTrigger(var0, var1, var2, var3, var4));
    }

    public boolean isPauseScreen() {
        return false;
    }
}
