package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.extension.client.gui.ExInGameHud;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.texture.TextureManager;

@SuppressWarnings("unused")
public class UIElement {

    public float curX = 0.0F;
    public float curY = 0.0F;
    public float prevX = 0.0F;
    public float prevY = 0.0F;
    protected ScriptUIContainer parent;

    public void addToScreen() {
        if (Minecraft.instance.overlay != null) {
            ((ExInGameHud) Minecraft.instance.overlay).getScriptUI().add(this);
        }
    }

    public void removeFromScreen() {
        if (this.parent != null) {
            this.parent.remove(this);
        }
    }

    public void pushToFront() {
        if (this.parent != null) {
            this.parent.add(this);
        }
    }

    public void pushToBack() {
        if (this.parent != null) {
            this.parent.addToBack(this);
        }
    }

    public void render(TextRenderer var1, TextureManager var2, float var3) {
    }

    public void onUpdate() {
        this.prevX = this.curX;
        this.prevY = this.curY;
    }

    public float getX() {
        return this.curX;
    }

    public void setX(float var1) {
        this.curX = this.prevX = var1;
    }

    public float getY() {
        return this.curY;
    }

    public void setY(float var1) {
        this.curY = this.prevY = var1;
    }

    protected float getXAtTime(float var1) {
        return (1.0F - var1) * this.prevX + var1 * this.curX;
    }

    protected float getYAtTime(float var1) {
        return (1.0F - var1) * this.prevY + var1 * this.curY;
    }

    public void moveTo(float var1, float var2) {
        this.curX = var1;
        this.curY = var2;
    }

    public void moveBy(float var1, float var2) {
        this.curX += var1;
        this.curY += var2;
    }
}
