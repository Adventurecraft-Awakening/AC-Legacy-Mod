package dev.adventurecraft.awakening.common.gui;

import dev.adventurecraft.awakening.item.AC_ItemCursor;
import dev.adventurecraft.awakening.tile.entity.AC_TileEntityTriggerPushable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionButton;
import net.minecraft.client.gui.screens.Screen;

public class AC_GuiTriggerPushable extends Screen {
    private AC_TileEntityTriggerPushable trigger;

    public AC_GuiTriggerPushable(AC_TileEntityTriggerPushable entity) {
        this.trigger = entity;
    }

    public void tick() {
    }

    public void init() {
        this.buttons.add(new OptionButton(0, 4, 40, "Use Current Selection"));

        var var1 = new OptionButton(1, 4, 60, "Trigger Target");
        if (this.trigger.resetOnTrigger) {
            var1.message = "Reset Target";
        }
        this.buttons.add(var1);
    }

    protected void buttonClicked(Button btn) {
        AC_TileEntityTriggerPushable trigger = this.trigger;
        if (btn.id == 0) {
            trigger.minX = AC_ItemCursor.minX;
            trigger.minY = AC_ItemCursor.minY;
            trigger.minZ = AC_ItemCursor.minZ;
            trigger.maxX = AC_ItemCursor.maxX;
            trigger.maxY = AC_ItemCursor.maxY;
            trigger.maxZ = AC_ItemCursor.maxZ;
        } else if (btn.id == 1) {
            trigger.resetOnTrigger = !trigger.resetOnTrigger;
            if (trigger.resetOnTrigger) {
                btn.message = "Reset Target";
            } else {
                btn.message = "Trigger Target";
            }
        }

        this.trigger.setChanged();
    }

    public void render(int mouseX, int mouseY, float tick) {
        this.fill(0, 0, this.width, this.height, Integer.MIN_VALUE);

        AC_TileEntityTriggerPushable t = this.trigger;
        int color = 14737632;
        this.drawString(this.font, String.format("Min: (%d, %d, %d)", t.minX, t.minY, t.minZ), 4, 4, color);
        this.drawString(this.font, String.format("Max: (%d, %d, %d)", t.maxX, t.maxY, t.maxZ), 4, 24, color);

        super.render(mouseX, mouseY, tick);
    }

    public static void showUI(AC_TileEntityTriggerPushable entity) {
        Minecraft.instance.setScreen(new AC_GuiTriggerPushable(entity));
    }

    public boolean isPauseScreen() {
        return false;
    }
}
