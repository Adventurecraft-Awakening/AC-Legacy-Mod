package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.extension.client.gui.ExInGameHud;
import dev.adventurecraft.awakening.util.MathF;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.Textures;

@SuppressWarnings("unused")
public class UIElement {

    public double curX;
    public double curY;
    public double prevX;
    public double prevY;

    protected ScriptUIContainer parent;

    public UIElement(double x, double y) {
        this.curX = x;
        this.curY = y;
        this.prevX = x;
        this.prevY = y;
    }

    public UIElement() {
        this(0.0, 0.0);
    }

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

    public double getX() {
        return this.curX;
    }

    public void setX(double x) {
        this.curX = this.prevX = x;
    }

    public double getY() {
        return this.curY;
    }

    public void setY(double y) {
        this.curY = this.prevY = y;
    }

    protected double getXAtTime(float deltaTime) {
        return MathF.lerp(deltaTime, this.prevX, this.curX);
    }

    protected double getYAtTime(float deltaTime) {
        return MathF.lerp(deltaTime, this.prevY, this.curY);
    }

    public void moveTo(double x, double y) {
        this.curX = x;
        this.curY = y;
    }

    public void moveBy(double x, double y) {
        this.curX += x;
        this.curY += y;
    }
}
