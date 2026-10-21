package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.client.render.UiDrawContext;

import java.util.LinkedList;

import dev.adventurecraft.awakening.extension.client.gui.ExInGameHud;
import net.minecraft.client.Minecraft;

@SuppressWarnings("unused")
public class ScriptUIContainer extends UIElement {

    public String text;

    private final LinkedList<UIElement> uiElements;

    public ScriptUIContainer(double x, double y) {
        this(x, y, ((ExInGameHud) Minecraft.instance.gui).getScriptUI());
    }

    public ScriptUIContainer(double x, double y, ScriptUIContainer parent) {
        super(x, y);
        this.text = "";
        this.uiElements = new LinkedList<>();
        if (parent != null) {
            parent.add(this);
        }
    }

    @Override
    public void render(UiDrawContext context, float deltaTime) {
        double x = this.getXAtTime(deltaTime);
        double y = this.getYAtTime(deltaTime);
        var local = context.translated(x, y);
        for (UIElement element : this.uiElements) {
            element.render(local, deltaTime);
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

        this.uiElements.addFirst(element);
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
