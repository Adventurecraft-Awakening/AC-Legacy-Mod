package dev.adventurecraft.awakening.script;

import java.util.LinkedList;
import java.util.List;

import dev.adventurecraft.awakening.extension.client.gui.ExInGameHud;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.texture.TextureManager;
import org.lwjgl.opengl.GL11;

@SuppressWarnings("unused")
public class ScriptUIContainer extends UIElement {

    public String text;
    private List<UIElement> uiElements;

    public ScriptUIContainer(float var1, float var2) {
        this(var1, var2, ((ExInGameHud) Minecraft.instance.overlay).getScriptUI());
    }

    public ScriptUIContainer(float var1, float var2, ScriptUIContainer var3) {
        this.text = "";
        this.prevX = this.curX = var1;
        this.prevY = this.curY = var2;
        this.uiElements = new LinkedList<>();
        if (var3 != null) {
            var3.add(this);
        }
    }

    @Override
    public void render(TextRenderer var1, TextureManager var2, float var3) {
        float var4 = this.getXAtTime(var3);
        float var5 = this.getYAtTime(var3);
        if (var4 != 0.0F || var5 != 0.0F) {
            GL11.glPushMatrix();
            GL11.glTranslated(var4, var5, 0.0D);
        }

        for (UIElement var7 : this.uiElements) {
            var7.render(var1, var2, var3);
        }

        if (var4 != 0.0F || var5 != 0.0F) {
            GL11.glPopMatrix();
        }
    }

    public void add(UIElement var1) {
        if (var1.parent != null) {
            var1.parent.remove(var1);
        }

        this.uiElements.add(var1);
        var1.parent = this;
    }

    public void addToBack(UIElement var1) {
        if (var1.parent != null) {
            var1.parent.remove(var1);
        }

        this.uiElements.add(0, var1);
        var1.parent = this;
    }

    public void remove(UIElement var1) {
        this.uiElements.remove(var1);
        var1.parent = null;
    }

    public void clear() {
        this.uiElements.clear();
    }

    @Override
    public void onUpdate() {
        this.prevX = this.curX;
        this.prevY = this.curY;

        for (UIElement var2 : this.uiElements) {
            var2.onUpdate();
        }
    }
}
