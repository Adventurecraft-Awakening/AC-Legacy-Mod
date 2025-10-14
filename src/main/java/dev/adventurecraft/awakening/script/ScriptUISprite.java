package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.extension.client.gui.ExInGameHud;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.Tesselator;
import net.minecraft.client.renderer.Textures;

@SuppressWarnings("unused")
public class ScriptUISprite extends UIElement implements ScriptColor {

    public String texture;
    public double width;
    public double height;
    public double imageWidth;
    public double imageHeight;
    public double u;
    public double v;

    private ScriptVec4 color;

    public ScriptUISprite(String texture, double x, double y, double width, double height, double u, double v) {
        this(texture, x, y, width, height, u, v, ((ExInGameHud) Minecraft.instance.gui).getScriptUI());
    }

    public ScriptUISprite(
        String texture,
        double x,
        double y,
        double width,
        double height,
        double u,
        double v,
        ScriptUIContainer container
    ) {
        super(x, y);
        this.imageWidth = 256.0F;
        this.imageHeight = 256.0F;
        this.texture = texture;
        this.width = width;
        this.height = height;
        this.u = u;
        this.v = v;
        this.color = new ScriptVec4(1.0);
        if (container != null) {
            container.add(this);
        }
    }

    @Override
    public void render(Font font, Textures textures, float deltaTime) {
        if (this.texture.startsWith("http")) {
            textures.bind(textures.loadHttpTexture(this.texture, "./pack.png"));
        }
        else {
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
        ScriptVec4 c = this.getColor();
        float r = (float) c.getR();
        float g = (float) c.getG();
        float b = (float) c.getB();
        float a = (float) c.getA();
        ts.color(r, g, b, a);
        ts.vertexUV(x, y + h, 0.0D, this.u * ru, (this.v + h) * rv);
        ts.vertexUV(x + w, y + h, 0.0D, (this.u + w) * ru, (this.v + h) * rv);
        ts.vertexUV(x + w, y, 0.0D, (this.u + w) * ru, this.v * rv);
        ts.vertexUV(x, y, 0.0D, this.u * ru, this.v * rv);
        ts.end();
    }

    @Override
    public ScriptVec4 getColor() {
        return this.color;
    }

    @Override
    public void setColor(ScriptVec4 value) {
        if (value == null) {
            value = new ScriptVec4(1.0);
        }
        this.color = value;
    }
}
