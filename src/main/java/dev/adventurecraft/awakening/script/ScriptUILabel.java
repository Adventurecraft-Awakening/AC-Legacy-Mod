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
public class ScriptUILabel extends UIElement {

    private String text;
    private String[] textLines;
    public boolean shadow;
    public boolean centered;
    public float red;
    public float green;
    public float blue;
    public float alpha;

    public ScriptUILabel(String text, float x, float y) {
        this(text, x, y, ((ExInGameHud) Minecraft.instance.gui).getScriptUI());
    }

    public ScriptUILabel(String text, float x, float y, ScriptUIContainer container) {
        this.shadow = true;
        this.centered = false;
        this.red = 1.0F;
        this.green = 1.0F;
        this.blue = 1.0F;
        this.alpha = 1.0F;
        this.text = text;
        this.textLines = text.split("\n");
        this.prevX = this.curX = x;
        this.prevY = this.curY = y;
        if (container != null) {
            container.add(this);
        }
    }

    @Override
    public void render(Font font, Textures textures, float deltaTime) {
        int alpha = MathF.clamp((int) (this.alpha * 255.0F), 0, 255);
        if (alpha == 0) {
            return;
        }

        int red = MathF.clamp((int) (this.red * 255.0F), 0, 255);
        int green = MathF.clamp((int) (this.green * 255.0F), 0, 255);
        int blue = MathF.clamp((int) (this.blue * 255.0F), 0, 255);
        int color = Rgba.fromRgba8(red, green, blue, alpha);

        float x = this.getXAtTime(deltaTime);
        float y = this.getYAtTime(deltaTime);
        String[] lines = this.textLines;
        int shadowColor = this.shadow ? ExTextRenderer.getShadowColor(color) : 0;

        TextRendererState state = ((ExTextRenderer) font).createState();
        state.setShadowOffset(1, 1);

        state.setColor(color);
        state.setShadow(shadowColor);

        state.begin(Tesselator.instance);
        for (String line : lines) {
            float lineX = x;
            if (this.centered) {
                lineX = x - (float) (state.measureText(line).width() / 2);
            }

            state.drawText(line, lineX, y);
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
}
