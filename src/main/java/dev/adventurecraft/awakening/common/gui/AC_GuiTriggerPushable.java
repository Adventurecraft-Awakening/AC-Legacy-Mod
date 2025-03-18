package dev.adventurecraft.awakening.common.gui;

import dev.adventurecraft.awakening.common.AC_ItemCursor;
import dev.adventurecraft.awakening.tile.entity.AC_TileEntityTriggerPushable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionButton;
import net.minecraft.client.gui.screens.Screen;

public class AC_GuiTriggerPushable extends Screen {
    private AC_TileEntityTriggerPushable trigger;

    public AC_GuiTriggerPushable(AC_TileEntityTriggerPushable var1) {
        this.trigger = var1;
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
        if (var1.id == 0) {
            this.trigger.minX = AC_ItemCursor.minX;
            this.trigger.minY = AC_ItemCursor.minY;
            this.trigger.minZ = AC_ItemCursor.minZ;
            this.trigger.maxX = AC_ItemCursor.maxX;
            this.trigger.maxY = AC_ItemCursor.maxY;
            this.trigger.maxZ = AC_ItemCursor.maxZ;
        } else if (var1.id == 1) {
            this.trigger.resetOnTrigger = !this.trigger.resetOnTrigger;
            if (this.trigger.resetOnTrigger) {
                var1.message = "Reset Target";
            } else {
                var1.message = "Trigger Target";
            }
        }

        this.trigger.level.getChunkAt(this.trigger.x, this.trigger.z).markUnsaved();
    }

    public void render(int var1, int var2, float var3) {
        this.fill(0, 0, this.width, this.height, Integer.MIN_VALUE);
        this.drawString(this.font, String.format("Min: (%d, %d, %d)", this.trigger.minX, this.trigger.minY, this.trigger.minZ), 4, 4, 14737632);
        this.drawString(this.font, String.format("Max: (%d, %d, %d)", this.trigger.maxX, this.trigger.maxY, this.trigger.maxZ), 4, 24, 14737632);
        super.render(var1, var2, var3);
    }

    public static void showUI(AC_TileEntityTriggerPushable var0) {
        Minecraft.instance.setScreen(new AC_GuiTriggerPushable(var0));
    }

    public boolean isPauseScreen() {
        return false;
    }
}
