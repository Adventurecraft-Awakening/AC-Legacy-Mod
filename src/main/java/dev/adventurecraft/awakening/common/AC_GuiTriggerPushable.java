package dev.adventurecraft.awakening.common;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widgets.OptionButtonWidget;

public class AC_GuiTriggerPushable extends Screen {
    private AC_TileEntityTriggerPushable trigger;

    public AC_GuiTriggerPushable(AC_TileEntityTriggerPushable var1) {
        this.trigger = var1;
    }

    public void tick() {
    }

    public void initVanillaScreen() {
        this.buttons.add(new OptionButtonWidget(0, 4, 40, "Use Current Selection"));
        OptionButtonWidget var1 = new OptionButtonWidget(1, 4, 60, "Trigger Target");
        if (this.trigger.resetOnTrigger) {
            var1.text = "Reset Target";
        }

        this.buttons.add(var1);
    }

    protected void buttonClicked(ButtonWidget var1) {
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
                var1.text = "Reset Target";
            } else {
                var1.text = "Trigger Target";
            }
        }

        this.trigger.world.getChunk(this.trigger.x, this.trigger.z).method_885();
    }

    public void render(int var1, int var2, float var3) {
        this.fill(0, 0, this.width, this.height, Integer.MIN_VALUE);
        this.drawTextWithShadow(this.textRenderer, String.format("Min: (%d, %d, %d)", this.trigger.minX, this.trigger.minY, this.trigger.minZ), 4, 4, 14737632);
        this.drawTextWithShadow(this.textRenderer, String.format("Max: (%d, %d, %d)", this.trigger.maxX, this.trigger.maxY, this.trigger.maxZ), 4, 24, 14737632);
        super.render(var1, var2, var3);
    }

    public static void showUI(AC_TileEntityTriggerPushable var0) {
        Minecraft.instance.openScreen(new AC_GuiTriggerPushable(var0));
    }

    public boolean isPauseScreen() {
        return false;
    }
}
