package dev.adventurecraft.awakening.common;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widgets.OptionButtonWidget;

public class AC_GuiStorage extends Screen {
    private AC_TileEntityStorage storage;

    public AC_GuiStorage(AC_TileEntityStorage var1) {
        this.storage = var1;
    }

    public void tick() {
    }

    public void initVanillaScreen() {
        this.buttons.add(new OptionButtonWidget(0, 4, 40, "Use Current Selection"));
        this.buttons.add(new OptionButtonWidget(1, 4, 60, "Resave Set Selection"));
        this.buttons.add(new OptionButtonWidget(2, 4, 80, "Load Saved Data"));
    }

    protected void buttonClicked(ButtonWidget var1) {
        if (var1.id == 0) {
            this.storage.setArea();
        } else if (var1.id == 1) {
            this.storage.saveCurrentArea();
        } else if (var1.id == 2) {
            this.storage.loadCurrentArea();
        }

    }

    public void render(int var1, int var2, float var3) {
        this.fill(0, 0, this.width, this.height, Integer.MIN_VALUE);
        this.drawTextWithShadow(this.textRenderer, String.format("Min: (%d, %d, %d)", this.storage.minX, this.storage.minY, this.storage.minZ), 4, 4, 14737632);
        this.drawTextWithShadow(this.textRenderer, String.format("Max: (%d, %d, %d)", this.storage.maxX, this.storage.maxY, this.storage.maxZ), 4, 24, 14737632);
        super.render(var1, var2, var3);
    }

    public static void showUI(AC_TileEntityStorage var0) {
        Minecraft.instance.openScreen(new AC_GuiStorage(var0));
    }

    public boolean isPauseScreen() {
        return false;
    }
}
