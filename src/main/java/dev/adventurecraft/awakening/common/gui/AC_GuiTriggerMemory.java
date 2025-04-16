package dev.adventurecraft.awakening.common.gui;

import dev.adventurecraft.awakening.tile.AC_Blocks;
import dev.adventurecraft.awakening.tile.entity.AC_TileEntityTriggerMemory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionButton;
import net.minecraft.client.gui.screens.Screen;

public class AC_GuiTriggerMemory extends Screen {
    private AC_TileEntityTriggerMemory trigger;

    public AC_GuiTriggerMemory(AC_TileEntityTriggerMemory entity) {
        this.trigger = entity;
    }

    public void tick() {
    }

    public void init() {
        this.buttons.add(new OptionButton(0, 4, 40, "Use Current Selection"));

        var var1 = new OptionButton(1, 4, 60, "Activate on Trigger");
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

    protected void buttonClicked(Button btn) {
        AC_TileEntityTriggerMemory t = this.trigger;
        if (btn.id == 0) {
            int var2 = t.level.getTile(t.x, t.y, t.z);
            if (var2 == AC_Blocks.triggerMemory.id) {
                AC_Blocks.triggerMemory.setTriggerToSelection(t.level, t.x, t.y, t.z);
            }
        } else if (btn.id == 1) {
            t.activateOnDetrigger = !t.activateOnDetrigger;
            if (t.activateOnDetrigger) {
                btn.message = "Activate on Detrigger";
            } else {
                btn.message = "Activate on Trigger";
            }
        } else if (btn.id == 2) {
            t.resetOnDeath = !t.resetOnDeath;
            if (t.resetOnDeath) {
                btn.message = "Reset on Death";
            } else {
                btn.message = "Don't Reset on Death";
            }
        }

        t.setChanged();
    }

    public void render(int mouseX, int mouseY, float tick) {
        this.fill(0, 0, this.width, this.height, Integer.MIN_VALUE);

        AC_TileEntityTriggerMemory t = this.trigger;
        int color = 14737632;

        this.drawString(this.font, String.format("Min: (%d, %d, %d)", t.minX, t.minY, t.minZ), 4, 4, color);
        this.drawString(this.font, String.format("Max: (%d, %d, %d)", t.maxX, t.maxY, t.maxZ), 4, 24, color);
        if (t.isActivated) {
            this.drawString(this.font, "Memory Set", 4, 104, color);
        } else {
            this.drawString(this.font, "Memory Unset", 4, 104, color);
        }

        super.render(mouseX, mouseY, tick);
    }

    public static void showUI(AC_TileEntityTriggerMemory entity) {
        Minecraft.instance.setScreen(new AC_GuiTriggerMemory(entity));
    }

    public boolean isPauseScreen() {
        return false;
    }
}
