package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.client.render.UiDrawContext;
import dev.adventurecraft.awakening.extension.client.gui.ExInGameHud;
import net.minecraft.client.Minecraft;

@SuppressWarnings("unused")
public class ScriptUIRect extends UIElement implements ScriptColor {

    public double width;
    public double height;

    private ScriptVec4 color;

    public ScriptUIRect(double x, double y, double width, double height, ScriptVec4 color) {
        this(x, y, width, height, color, ((ExInGameHud) Minecraft.instance.gui).getScriptUI());
    }

    @Deprecated
    public ScriptUIRect(double x, double y, double width, double height, double red, double green, double blue, double alpha) {
        this(x, y, width, height, red, green, blue, alpha, ((ExInGameHud) Minecraft.instance.gui).getScriptUI());
    }

    public ScriptUIRect(double x, double y, double width, double height, ScriptVec4 color, ScriptUIContainer container) {
        super(x, y);
        this.width = width;
        this.height = height;
        this.color = color;
        if (container != null) {
            container.add(this);
        }
    }

    @Deprecated
    public ScriptUIRect(
        double x,
        double y,
        double width,
        double height,
        double red,
        double green,
        double blue,
        double alpha,
        ScriptUIContainer container
    ) {
        this(x, y, width, height, new ScriptVec4(red, green, blue, alpha), container);
    }

    @Override
    public void render(UiDrawContext context, float deltaTime) {
        double x = this.getXAtTime(deltaTime);
        double y = this.getYAtTime(deltaTime);
        ScriptVec4 c = this.getColor();
        int rgba = dev.adventurecraft.awakening.image.Rgba.fromRgba8(
            dev.adventurecraft.awakening.image.Rgba.saturateToByte(c.getR()),
            dev.adventurecraft.awakening.image.Rgba.saturateToByte(c.getG()),
            dev.adventurecraft.awakening.image.Rgba.saturateToByte(c.getB()),
            dev.adventurecraft.awakening.image.Rgba.saturateToByte(c.getA()));
        context.fill(new dev.adventurecraft.awakening.layout.Rect(x, y, this.width, this.height), rgba);
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
