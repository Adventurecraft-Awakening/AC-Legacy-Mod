package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.common.TextRendererState;
import dev.adventurecraft.awakening.extension.client.gui.ExInGameHud;
import dev.adventurecraft.awakening.extension.client.render.ExTextRenderer;
import dev.adventurecraft.awakening.image.Rgba;
import dev.adventurecraft.awakening.util.MathF;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.Tesselator;
import net.minecraft.client.renderer.Textures;

@SuppressWarnings("unused")
public class ScriptUILabel extends UIElement implements ScriptColor {

    private String text;
    private String[] textLines;
    public boolean shadow;
    public boolean centered;

    private ScriptVec4 color;

    public ScriptUILabel(String text, double x, double y) {
        this(text, x, y, ((ExInGameHud) Minecraft.instance.gui).getScriptUI());
    }

    public ScriptUILabel(String text, double x, double y, ScriptUIContainer container) {
        super(x, y);
        this.shadow = true;
        this.centered = false;
        this.color = new ScriptVec4(1.0);
        this.text = text;
        this.textLines = text.split("\n");
        if (container != null) {
            container.add(this);
        }
    }

    @Override
    public void render(Font font, Textures textures, float deltaTime) {
        ScriptVec4 color = this.getColor();
        int alpha = (int) MathF.clamp((color.getA() * 255.0F), 0, 255);
        if (alpha == 0) {
            return;
        }

        int red = (int) MathF.clamp((color.getR() * 255.0F), 0, 255);
        int green = (int) MathF.clamp((color.getG() * 255.0F), 0, 255);
        int blue = (int) MathF.clamp((color.getB() * 255.0F), 0, 255);
        int rgba = Rgba.fromRgba8(red, green, blue, alpha);

        double x = this.getXAtTime(deltaTime);
        double y = this.getYAtTime(deltaTime);
        String[] lines = this.textLines;
        int shadowColor = this.shadow ? ExTextRenderer.getShadowColor(rgba) : 0;

        TextRendererState state = ((ExTextRenderer) font).createState();
        state.setShadowOffset(1, 1);

        state.setColor(rgba);
        state.setShadow(shadowColor);

        state.begin(Tesselator.instance);
        for (String line : lines) {
            double lineX = x;
            if (this.centered) {
                lineX = x - (state.measureText(line).width() / 2.0);
            }

            state.drawText(line, (float) lineX, (float) y);
            state.resetFormat();

            y += 9.0F;
        }
        state.end();
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
        this.textLines = text.split("\n");
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
