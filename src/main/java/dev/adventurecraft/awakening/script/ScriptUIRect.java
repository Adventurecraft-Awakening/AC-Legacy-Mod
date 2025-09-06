package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.extension.client.gui.ExInGameHud;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.Tesselator;
import net.minecraft.client.renderer.Textures;
import org.lwjgl.opengl.GL11;

@SuppressWarnings("unused")
public class ScriptUIRect extends UIElement {

    public float width;
    public float height;
    public float red;
    public float green;
    public float blue;
    public float alpha;

    public ScriptUIRect(float x, float y, float width, float height, float red, float green, float blue, float alpha) {
        this(x, y, width, height, red, green, blue, alpha, ((ExInGameHud) Minecraft.instance.gui).getScriptUI());
    }

    public ScriptUIRect(float x, float y, float width, float height, float red, float green, float blue, float alpha, ScriptUIContainer container) {
        this.red = 1.0F;
        this.green = 1.0F;
        this.blue = 1.0F;
        this.alpha = 1.0F;
        this.prevX = this.curX = x;
        this.prevY = this.curY = y;
        this.width = width;
        this.height = height;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
        if (container != null) {
            container.add(this);
        }
    }

    @Override
    public void render(Font font, Textures textures, float deltaTime) {
        float x = this.getXAtTime(deltaTime);
        float y = this.getYAtTime(deltaTime);
        Tesselator ts = Tesselator.instance;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        ts.begin();
        ts.color(this.red, this.green, this.blue, this.alpha);
        ts.vertex(x, y + this.height, 0.0D);
        ts.vertex(x + this.width, y + this.height, 0.0D);
        ts.vertex(x + this.width, y, 0.0D);
        ts.vertex(x, y, 0.0D);
        ts.end();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }
}
