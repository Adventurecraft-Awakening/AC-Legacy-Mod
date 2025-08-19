package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.extension.client.gui.ExInGameHud;
import dev.adventurecraft.awakening.util.MathF;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.Textures;

@SuppressWarnings("unused")
public class UIElement {

    public float curX = 0.0F;
    public float curY = 0.0F;
    public float prevX = 0.0F;
    public float prevY = 0.0F;
    protected ScriptUIContainer parent;

    public void addToScreen() {
        if (Minecraft.instance.gui != null) {
            ((ExInGameHud) Minecraft.instance.gui).getScriptUI().add(this);
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

    public void render(Font font, Textures textures, float deltaTime) {
    }

    public void onUpdate() {
        this.prevX = this.curX;
        this.prevY = this.curY;
    }

    public float getX() {
        return this.curX;
    }

    public void setX(float x) {
        this.curX = this.prevX = x;
    }

    public float getY() {
        return this.curY;
    }

    public void setY(float y) {
        this.curY = this.prevY = y;
    }

    protected float getXAtTime(float deltaTime) {
        return MathF.lerp(deltaTime, this.prevX, this.curX);
    }

    protected float getYAtTime(float deltaTime) {
        return MathF.lerp(deltaTime, this.prevY, this.curY);
    }

    public void moveTo(float x, float y) {
        this.curX = x;
        this.curY = y;
    }

    public void moveBy(float x, float y) {
        this.curX += x;
        this.curY += y;
    }
}
