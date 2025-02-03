package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.client.render.ExTesselator;
import dev.adventurecraft.awakening.extension.client.render.ExTextRenderer;
import dev.adventurecraft.awakening.image.Rgba;
import dev.adventurecraft.awakening.util.HexConvert;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.Tesselator;
import org.lwjgl.opengl.GL11;

public class TextRendererState {

    private final Font font;

    private int color;
    private int shadow;
    private float shadowOffsetX, shadowOffsetY;

    public TextRendererState(Font font) {
        this.font = font;
    }

    public void bindTexture() {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.font.fontTexture);
    }

    public void begin(Tesselator tessallator) {
        tessallator.begin();
    }

    public void end(Tesselator tessallator) {
        tessallator.end();
    }

    public void drawText(Tesselator ts, CharSequence text, int start, int end, float x, float y) {
        if (text == null) {
            return;
        }
        if (end - start == 0) {
            return;
        }
        validateCharSequence(text, start, end);

        if (Rgba.getAlpha(this.color) == 0) {
            return;
        }
        var exTs = (ExTesselator) ts;
        exTs.ac$color(this.color);

        var colorPalette = ((ExTextRenderer) this.font).getColorPalette();
        var widthLookup = ((ExTextRenderer) this.font).getCharWidths();

        float xOff = 0;

        for (int i = start; i < end; ++i) {
            char c = text.charAt(i);
            if (end > i + 1 && c == '§') {
                int colorIndex = HexConvert.fromHexDigit(text.charAt(i + 1));
                if (colorIndex < 0 || colorIndex > 15) {
                    colorIndex = 15;
                }

                this.color = Rgba.withRgb(this.color, colorPalette[colorIndex]);
                if (this.hasShadow()) {
                    this.shadow = Rgba.withRgb(this.shadow, colorPalette[colorIndex + 16]);
                } else {
                    exTs.ac$color(this.color);
                }

                i++; // skip the digit
                continue;
            }

            int charIndex = SharedConstants.acceptableLetters.indexOf(c);
            int ch;
            if (charIndex >= 0 && c < 176) {
                ch = charIndex + 32;
            } else if (c < 256) {
                ch = c;
            } else {
                continue;
            }

            int column = ch % 16 * 8;
            int row = ch / 16 * 8;
            if (this.hasShadow()) {
                exTs.ac$color(this.shadow);
                drawChar(ts, column, row, xOff + x + this.shadowOffsetX, y + this.shadowOffsetY);
                exTs.ac$color(this.color);
            }
            drawChar(ts, column, row, xOff + x, y);
            xOff += widthLookup[ch];
        }
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setShadow(int color) {
        this.shadow = color;
    }

    public boolean hasShadow() {
        return Rgba.getAlpha(this.shadow) != 0;
    }

    public void setShadowOffsetX(float offsetX) {
        this.shadowOffsetX = offsetX;
    }

    public void setShadowOffsetY(float offsetY) {
        this.shadowOffsetY = offsetY;
    }

    public void setShadowOffset(float offsetX, float offsetY) {
        this.setShadowOffsetX(offsetX);
        this.setShadowOffsetY(offsetY);
    }

    private static void drawChar(Tesselator ts, int column, int row, float x, float y) {
        float u = column / 128f;
        float v = row / 128f;
        float f = 7.99f;
        float t = f / 128f;
        ts.vertexUV(x, y + f, 0.0, u, v + t);
        ts.vertexUV(x + f, y + f, 0.0, u + t, v + t);
        ts.vertexUV(x + f, y, 0.0, u + t, v);
        ts.vertexUV(x, y, 0.0, u, v);
    }

    public static void validateCharSequence(CharSequence text, int start, int end) {
        if (start > text.length()) {
            throw new IllegalArgumentException(
                String.format("start (%d) exceeds text length (%d).", start, text.length()));
        }
        if (end > text.length()) {
            throw new IllegalArgumentException(
                String.format("end (%d) exceeds text length (%d).", end, text.length()));
        }
        if ((end - start) > text.length()) {
            throw new IllegalArgumentException(
                String.format("(end (%d) - start (%d)) exceeds text length (%d).", end, start, text.length()));
        }
    }
}
