package dev.adventurecraft.awakening.script;

import java.util.LinkedList;
import java.util.List;

import dev.adventurecraft.awakening.extension.client.gui.ExInGameHud;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.Textures;
import org.lwjgl.opengl.GL11;

@SuppressWarnings("unused")
public class ScriptUIContainer extends UIElement {

    public String text;

    private final LinkedList<UIElement> uiElements;

    public ScriptUIContainer(float x, float y) {
        this(x, y, ((ExInGameHud) Minecraft.instance.gui).getScriptUI());
    }

    public ScriptUIContainer(float x, float y, ScriptUIContainer parent) {
        this.text = "";
        this.prevX = this.curX = x;
        this.prevY = this.curY = y;
        this.uiElements = new LinkedList<>();
        if (parent != null) {
            parent.add(this);
        }
    }

    @Override
    public void render(Font textRenderer, Textures texManager, float deltaTime) {
        float x = this.getXAtTime(deltaTime);
        float y = this.getYAtTime(deltaTime);
        if (x != 0.0F || y != 0.0F) {
            GL11.glPushMatrix();
            GL11.glTranslated(x, y, 0.0D);
        }

        for (UIElement element : this.uiElements) {
            element.render(textRenderer, texManager, deltaTime);
        }

        if (x != 0.0F || y != 0.0F) {
            GL11.glPopMatrix();
        }
    }

    public void add(UIElement element) {
        if (element.parent != null) {
            element.parent.remove(element);
        }

        this.uiElements.add(element);
        element.parent = this;
    }

    public void addToBack(UIElement element) {
        if (element.parent != null) {
            element.parent.remove(element);
        }

        this.uiElements.add(0, element);
        element.parent = this;
    }

    public void remove(UIElement element) {
        this.uiElements.remove(element);
        element.parent = null;
    }

    public void clear() {
        this.uiElements.clear();
    }

    @Override
    public void onUpdate() {
        this.prevX = this.curX;
        this.prevY = this.curY;

        for (UIElement element : this.uiElements) {
            element.onUpdate();
        }
    }
}
