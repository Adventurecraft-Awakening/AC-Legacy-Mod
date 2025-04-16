package dev.adventurecraft.awakening.common.gui;

import dev.adventurecraft.awakening.tile.AC_Blocks;
import dev.adventurecraft.awakening.tile.entity.AC_TileEntityMinMax;
import dev.adventurecraft.awakening.tile.entity.AC_TileEntityTriggerInverter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionButton;
import net.minecraft.client.gui.screens.Screen;

public class AC_GuiTriggerInverter extends Screen {
    private AC_TileEntityTriggerInverter trigger;

    public AC_GuiTriggerInverter(AC_TileEntityTriggerInverter entity) {
        this.trigger = entity;
    }

    public void tick() {
    }

    public void init() {
        this.buttons.add(new OptionButton(0, 4, 40, "Use Current Selection"));
    }

    protected void buttonClicked(Button btn) {
        AC_TileEntityTriggerInverter t = this.trigger;
        int id = t.level.getTile(t.x, t.y, t.z);
        if (id == AC_Blocks.triggerInverter.id) {
            AC_Blocks.triggerInverter.setTriggerToSelection(t.level, t.x, t.y, t.z);
            t.setChanged();
        }
    }

    public void render(int mouseX, int mouseY, float tick) {
        this.fill(0, 0, this.width, this.height, Integer.MIN_VALUE);

        AC_TileEntityMinMax t = this.trigger;
        int color = 14737632;
        this.drawString(this.font, String.format("Min: (%d, %d, %d)", t.minX, t.minY, t.minZ), 4, 4, color);
        this.drawString(this.font, String.format("Max: (%d, %d, %d)", t.maxX, t.maxY, t.maxZ), 4, 24, color);

        super.render(mouseX, mouseY, tick);
    }

    public static void showUI(AC_TileEntityTriggerInverter entity) {
        Minecraft.instance.setScreen(new AC_GuiTriggerInverter(entity));
    }

    public boolean isPauseScreen() {
        return false;
    }
}
