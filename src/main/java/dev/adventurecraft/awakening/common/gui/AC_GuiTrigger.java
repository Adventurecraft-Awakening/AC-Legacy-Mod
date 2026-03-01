package dev.adventurecraft.awakening.common.gui;

import dev.adventurecraft.awakening.tile.AC_Blocks;
import dev.adventurecraft.awakening.tile.entity.AC_TileEntityTrigger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionButton;
import net.minecraft.client.gui.screens.Screen;

public class AC_GuiTrigger extends Screen {

    private AC_TileEntityTrigger trigger;

    public AC_GuiTrigger(AC_TileEntityTrigger entity) {
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
        AC_TileEntityTrigger t = this.trigger;
        if (btn.id == 0) {
            int id = t.level.getTile(t.x, t.y, t.z);
            if (id == AC_Blocks.triggerBlock.id) {
                AC_Blocks.triggerBlock.setTriggerToSelection(t.level, t.x, t.y, t.z);
            }
        } else if (btn.id == 1) {
            int id = t.level.getTile(t.x, t.y, t.z);
            if (id == AC_Blocks.triggerBlock.id) {
                AC_Blocks.triggerBlock.setTriggerReset(t.level, t.x, t.y, t.z, !t.resetOnTrigger);
            }

            if (t.resetOnTrigger) {
                btn.message = "Reset Target";
            } else {
                btn.message = "Trigger Target";
            }
        }
        t.setChanged();
    }

    public void render(int mouseX, int mouseY, float tick) {
        this.fill(0, 0, this.width, this.height, Integer.MIN_VALUE);
        AC_GuiStrings.drawMinMax(this, this.trigger, 4, 4, 0xe0e0e0);
        super.render(mouseX, mouseY, tick);
    }

    public static void showUI(AC_TileEntityTrigger entity) {
        Minecraft.instance.setScreen(new AC_GuiTrigger(entity));
    }

    public boolean isPauseScreen() {
        return false;
    }
}
