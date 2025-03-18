package dev.adventurecraft.awakening.common.gui;

import dev.adventurecraft.awakening.common.AC_Blocks;
import dev.adventurecraft.awakening.common.AC_TileEntityTriggerMemory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.level.Level;

public class AC_GuiTriggerMemory extends Screen {
    private AC_TileEntityTriggerMemory trigger;
    private int blockX;
    private int blockY;
    private int blockZ;
    private Level world;

    public AC_GuiTriggerMemory(Level var1, int var2, int var3, int var4, AC_TileEntityTriggerMemory var5) {
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
        OptionButton var1 = new OptionButton(1, 4, 60, "Activate on Trigger");
        if (this.trigger.activateOnDetrigger) {
            var1.message = "Activate on Detrigger";
        }

        this.buttons.add(var1);
        var1 = new OptionButton(2, 4, 80, "Reset on Death");
        if (!this.trigger.resetOnDeath) {
            var1.message = "Don\'t Reset on Death";
        }

        this.buttons.add(var1);
    }

    protected void buttonClicked(Button var1) {
        if (var1.id == 0) {
            int var2 = this.world.getTile(this.blockX, this.blockY, this.blockZ);
            if (var2 == AC_Blocks.triggerMemory.id) {
                AC_Blocks.triggerMemory.setTriggerToSelection(this.world, this.blockX, this.blockY, this.blockZ);
            }
        } else if (var1.id == 1) {
            this.trigger.activateOnDetrigger = !this.trigger.activateOnDetrigger;
            if (this.trigger.activateOnDetrigger) {
                var1.message = "Activate on Detrigger";
            } else {
                var1.message = "Activate on Trigger";
            }
        } else if (var1.id == 2) {
            this.trigger.resetOnDeath = !this.trigger.resetOnDeath;
            if (this.trigger.resetOnDeath) {
                var1.message = "Reset on Death";
            } else {
                var1.message = "Don\'t Reset on Death";
            }
        }

        this.world.getChunkAt(this.blockX, this.blockZ).markUnsaved();
    }

    public void render(int var1, int var2, float var3) {
        this.fill(0, 0, this.width, this.height, Integer.MIN_VALUE);
        this.drawString(this.font, String.format("Min: (%d, %d, %d)", this.trigger.minX, this.trigger.minY, this.trigger.minZ), 4, 4, 14737632);
        this.drawString(this.font, String.format("Max: (%d, %d, %d)", this.trigger.maxX, this.trigger.maxY, this.trigger.maxZ), 4, 24, 14737632);
        if (this.trigger.isActivated) {
            this.drawString(this.font, "Memory Set", 4, 104, 14737632);
        } else {
            this.drawString(this.font, "Memory Unset", 4, 104, 14737632);
        }

        super.render(var1, var2, var3);
    }

    public static void showUI(Level var0, int var1, int var2, int var3, AC_TileEntityTriggerMemory var4) {
        Minecraft.instance.setScreen(new AC_GuiTriggerMemory(var0, var1, var2, var3, var4));
    }

    public boolean isPauseScreen() {
        return false;
    }
}
