package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.client.render.UiDrawContext;

import dev.adventurecraft.awakening.extension.client.gui.ExInGameHud;
import net.minecraft.client.Minecraft;

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
    public void render(UiDrawContext context, float deltaTime) {
        double x = this.getXAtTime(deltaTime), y = this.getYAtTime(deltaTime);
        ScriptVec4 c = this.getColor();
        int rgba = dev.adventurecraft.awakening.image.Rgba.fromRgba8(
            dev.adventurecraft.awakening.image.Rgba.saturateToByte(c.getR()),
            dev.adventurecraft.awakening.image.Rgba.saturateToByte(c.getG()),
            dev.adventurecraft.awakening.image.Rgba.saturateToByte(c.getB()),
            dev.adventurecraft.awakening.image.Rgba.saturateToByte(c.getA()));
        context.image(context.texture(this.texture),
            new dev.adventurecraft.awakening.layout.Rect(x, y, this.width, this.height),
            new dev.adventurecraft.awakening.layout.Rect(this.u / this.imageWidth, this.v / this.imageHeight,
                this.width / this.imageWidth, this.height / this.imageHeight),
            new dev.adventurecraft.awakening.layout.IntCorner(rgba));
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
