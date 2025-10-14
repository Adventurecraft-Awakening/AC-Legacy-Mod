package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.common.TextRendererState;
import dev.adventurecraft.awakening.extension.client.gui.ExInGameHud;
import dev.adventurecraft.awakening.extension.client.render.ExTextRenderer;
import dev.adventurecraft.awakening.image.Rgba;
import dev.adventurecraft.awakening.primitives.IntRange;
import dev.adventurecraft.awakening.text.LinesSpliterator;
import dev.adventurecraft.awakening.util.MathF;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.Tesselator;
import net.minecraft.client.renderer.Textures;

import java.util.ArrayList;

@SuppressWarnings("unused")
public class ScriptUILabel extends UIElement implements ScriptColor {

    private String text;
    private ArrayList<IntRange> lines;
    public boolean shadow;
    public boolean centered;

    private ScriptVec4 color;
    private boolean dirty;

    public ScriptUILabel(String text, double x, double y) {
        this(text, x, y, ((ExInGameHud) Minecraft.instance.gui).getScriptUI());
    }

    public ScriptUILabel(String text, double x, double y, ScriptUIContainer container) {
        super(x, y);
        this.shadow = true;
        this.centered = false;
        this.color = new ScriptVec4(1.0);
        this.lines = new ArrayList<>();
        this.setText(text);
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

        if (this.dirty) {
            this.rebuild();
            this.dirty = false;
        }

        int red = (int) MathF.clamp((color.getR() * 255.0F), 0, 255);
        int green = (int) MathF.clamp((color.getG() * 255.0F), 0, 255);
        int blue = (int) MathF.clamp((color.getB() * 255.0F), 0, 255);
        int rgba = Rgba.fromRgba8(red, green, blue, alpha);

        double x = this.getXAtTime(deltaTime);
        double y = this.getYAtTime(deltaTime);
        int shadowColor = this.shadow ? ExTextRenderer.getShadowColor(rgba) : 0;

        TextRendererState state = ((ExTextRenderer) font).createState();
        state.setShadowOffset(1, 1);

        state.setColor(rgba);
        state.setShadow(shadowColor);

        state.begin(Tesselator.instance);
        for (IntRange line : this.lines) {
            double lineX = x;
            if (this.centered) {
                lineX = x - (state.measureText(this.text, line.start(), line.end()).width() / 2.0);
            }

            state.drawText(this.text, line.start(), line.end(), (float) lineX, (float) y);
            state.resetFormat();

            y += 9.0F;
        }
        state.end();
    }

    public String getText() {
        // TODO: rebuild here if it ever becomes needed
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
        this.dirty = true;
    }

    private void rebuild() {
        this.lines.clear();
        new LinesSpliterator(text).forEachRemaining(this.lines::add);
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
