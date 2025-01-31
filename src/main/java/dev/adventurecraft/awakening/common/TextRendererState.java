package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.client.render.ExTextRenderer;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.Tesselator;
import org.lwjgl.opengl.GL11;

public class TextRendererState {

    private final Font font;

    private byte r, g, b, a;
    private byte sR, sG, sB, sA;
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

        if (this.a == 0) {
            return;
        }
        putColor(ts, r, g, b, a);

        var colorPalette = ((ExTextRenderer) this.font).getColorPalette();
        var widthLookup = ((ExTextRenderer) this.font).getCharWidths();

        float xOff = 0;

        for (int i = start; i < end; ++i) {
            char c = text.charAt(i);
            if (end > i + 1 && c == '§') {
                int colorIndex = "0123456789abcdef".indexOf(Character.toLowerCase(text.charAt(i + 1)));
                if (colorIndex < 0 || colorIndex > 15) {
                    colorIndex = 15;
                }

                int rgbIndex = colorIndex * 3;
                r = colorPalette[rgbIndex + 0];
                g = colorPalette[rgbIndex + 1];
                b = colorPalette[rgbIndex + 2];

                if (this.hasShadow()) {
                    int shadowRgbIndex = (colorIndex + 16) * 3;
                    sR = colorPalette[shadowRgbIndex + 0];
                    sG = colorPalette[shadowRgbIndex + 1];
                    sB = colorPalette[shadowRgbIndex + 2];
                } else {
                    putColor(ts, r, g, b, a);
                }

                i++;
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
                putColor(ts, sR, sG, sB, sA);
                drawChar(ts, column, row, xOff + x + this.shadowOffsetX, y + this.shadowOffsetY);
                putColor(ts, r, g, b, a);
            }
            drawChar(ts, column, row, xOff + x, y);
            xOff += widthLookup[ch];
        }
    }

    public void setColor(int color) {
        this.r = (byte) (color >>> 16 & 255);
        this.g = (byte) (color >>> 8 & 255);
        this.b = (byte) (color & 255);
        this.a = (byte) (color >>> 24);
    }

    public void setShadow(int color) {
        this.sR = (byte) (color >>> 16 & 255);
        this.sG = (byte) (color >>> 8 & 255);
        this.sB = (byte) (color & 255);
        this.sA = (byte) (color >>> 24);
    }

    public boolean hasShadow() {
        return this.sA != 0;
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

    private static void putColor(Tesselator ts, byte r, byte g, byte b, byte a) {
        ts.color(r & 0xff, g & 0xff, b & 0xff, a & 0xff);
    }

    private static void drawChar(Tesselator ts, int column, int row, float x, float y) {
        float f = 7.99f;
        float f2 = 0.0f;
        float f3 = 0.0f;
        ts.vertexUV(x, y + f, 0.0, (float) column / 128.0f + f2, ((float) row + f) / 128.0f + f3);
        ts.vertexUV(x + f, y + f, 0.0, ((float) column + f) / 128.0f + f2, ((float) row + f) / 128.0f + f3);
        ts.vertexUV(x + f, y, 0.0, ((float) column + f) / 128.0f + f2, (float) row / 128.0f + f3);
        ts.vertexUV(x, y, 0.0, (float) column / 128.0f + f2, (float) row / 128.0f + f3);
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
