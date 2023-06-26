package dev.adventurecraft.awakening.common;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widgets.OptionButtonWidget;

public class AC_GuiStorage extends Screen {

    private AC_TileEntityStorage storage;

    public AC_GuiStorage(AC_TileEntityStorage storage) {
        this.storage = storage;
    }

    @Override
    public void tick() {
    }

    @Override
    public void initVanillaScreen() {
        this.buttons.add(new OptionButtonWidget(0, 4, 40, "Use Current Selection"));
        this.buttons.add(new OptionButtonWidget(1, 4, 60, "Resave Set Selection"));
        this.buttons.add(new OptionButtonWidget(2, 4, 80, "Load Saved Data"));
    }

    @Override
    protected void buttonClicked(ButtonWidget button) {
        if (button.id == 0) {
            this.storage.setArea();
        } else if (button.id == 1) {
            this.storage.saveCurrentArea();
        } else if (button.id == 2) {
            this.storage.loadCurrentArea();
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float deltaTime) {
        this.fill(0, 0, this.width, this.height, Integer.MIN_VALUE);
        this.drawTextWithShadow(this.textRenderer, String.format("Min: (%d, %d, %d)", this.storage.minX, this.storage.minY, this.storage.minZ), 4, 4, 14737632);
        this.drawTextWithShadow(this.textRenderer, String.format("Max: (%d, %d, %d)", this.storage.maxX, this.storage.maxY, this.storage.maxZ), 4, 24, 14737632);
        super.render(mouseX, mouseY, deltaTime);
    }

    public static void showUI(AC_TileEntityStorage var0) {
        Minecraft.instance.openScreen(new AC_GuiStorage(var0));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
