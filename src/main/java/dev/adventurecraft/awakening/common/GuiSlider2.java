package dev.adventurecraft.awakening.common;

import org.lwjgl.opengl.GL11;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;

public class GuiSlider2 extends Button {
    public static int DEFAULT_WIDTH = 150;
    public static int DEFAULT_HEIGHT = 20;
    public float sliderValue;
    public boolean dragging;

    public GuiSlider2(int id, int x, int y, int unused, String label, float value) {
        super(id, x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT, label);
        this.sliderValue = value;
    }

    protected int getYImage(boolean hovered) { return 0; }

    /** Existing unbound screens retain drag polling until they adopt input-bound controls. */
    protected boolean updateWhileRendering() { return true; }

    protected void renderBg(Minecraft minecraft, int mouseX, int mouseY) {
        if (!this.visible) return;
        if (this.updateWhileRendering()) this.dragTo(mouseX);
        GL11.glColor4f(1, 1, 1, 1);
        int thumbX = this.x + (int) (this.sliderValue * (this.width - 8));
        this.blit(thumbX, this.y, 0, 66, 4, 20);
        this.blit(thumbX + 4, this.y, 196, 66, 4, 20);
    }

    public boolean isMouseOver(Minecraft minecraft, int mouseX, int mouseY) {
        if (!super.isMouseOver(minecraft, mouseX, mouseY)) return false;
        this.dragging = true;
        this.updateFromMouse(mouseX);
        return true;
    }

    public void dragTo(int mouseX) {
        if (this.dragging) this.updateFromMouse(mouseX);
    }

    public void mouseReleased(int mouseX, int mouseY) {
        this.dragTo(mouseX);
        this.dragging = false;
    }

    protected void valueChanged(float value) { }

    private void updateFromMouse(int mouseX) {
        float value = Math.clamp((float) (mouseX - (this.x + 4)) / Math.max(1, this.width - 8), 0, 1);
        if (Float.compare(value, this.sliderValue) == 0) return;
        this.sliderValue = value;
        this.valueChanged(value);
    }
}
