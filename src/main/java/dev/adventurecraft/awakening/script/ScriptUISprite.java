package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.extension.client.gui.ExInGameHud;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.Tesselator;
import net.minecraft.client.renderer.Textures;
import org.lwjgl.opengl.GL11;

@SuppressWarnings("unused")
public class ScriptUISprite extends UIElement {

    public String texture;
    public float width;
    public float height;
    public float imageWidth;
    public float imageHeight;
    public double u;
    public double v;
    public float red;
    public float green;
    public float blue;
    public float alpha;

    public ScriptUISprite(String texture, float x, float y, float width, float height, double u, double v) {
        this(texture, x, y, width, height, u, v, ((ExInGameHud) Minecraft.instance.gui).getScriptUI());
    }

    public ScriptUISprite(String texture, float x, float y, float width, float height, double u, double v, ScriptUIContainer container) {
        this.imageWidth = 256.0F;
        this.imageHeight = 256.0F;
        this.red = 1.0F;
        this.green = 1.0F;
        this.blue = 1.0F;
        this.alpha = 1.0F;
        this.texture = texture;
        this.prevX = this.curX = x;
        this.prevY = this.curY = y;
        this.width = width;
        this.height = height;
        this.u = u;
        this.v = v;
        if (container != null) {
            container.add(this);
        }
    }

    @Override
    public void render(Font font, Textures textures, float deltaTime) {
        if (this.texture.startsWith("http")) {
            textures.bind(textures.loadHttpTexture(this.texture, "./pack.png"));
        } else {
            textures.bind(textures.loadTexture(this.texture));
        }

        double x = this.getXAtTime(deltaTime);
        double y = this.getYAtTime(deltaTime);
        double w = this.width;
        double h = this.height;
        double ru = 1.0F / this.imageWidth;
        double rv = 1.0F / this.imageHeight;
        Tesselator ts = Tesselator.instance;
        ts.begin();
        ts.color(this.red, this.green, this.blue, this.alpha);
        ts.vertexUV(x, y + h, 0.0D, this.u * ru, (this.v + h) * rv);
        ts.vertexUV(x + w, y + h, 0.0D, (this.u + w) * ru, (this.v + h) * rv);
        ts.vertexUV(x + w, y, 0.0D, (this.u + w) * ru, this.v * rv);
        ts.vertexUV(x, y, 0.0D, this.u * ru, this.v * rv);
        ts.end();
    }
}
