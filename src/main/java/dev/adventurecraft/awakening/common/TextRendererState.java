package dev.adventurecraft.awakening.common;

import net.minecraft.client.render.Tessellator;
import net.minecraft.util.CharacterUtils;

public class TextRendererState {

    private final float[] colorPalette;
    private final int[] widthLookup;
    private Tessellator ts;

    private float r, g, b, a;
    private float sR, sG, sB, sA;
    private boolean hasShadow;
    private float shadowOffsetX, shadowOffsetY;

    public TextRendererState(float[] colorPalette, int[] widthLookup) {
        this.colorPalette = colorPalette;
        this.widthLookup = widthLookup;
    }

    public void begin(Tessellator tessallator) {
        if (tessallator == null) {
            throw new IllegalArgumentException();
        }
        this.ts = tessallator;

        ts.start();
        ts.color(r, g, b, a);
    }

    public void end() {
        this.ts.tessellate();
    }

    public void drawText(CharSequence text, int start, int end, float x, float y) {
        if (text == null) {
            return;
        }
        validateCharSequence(text, start, end);
        if (end - start == 0) {
            return;
        }

        float xOff = 0;

        for (int i = start; i < end; ++i) {
            char c = text.charAt(i);
            if (end > i + 1 && c == '§') {
                int colorIndex = "0123456789abcdef".indexOf(Character.toLowerCase(text.charAt(i + 1)));
                if (colorIndex < 0 || colorIndex > 15) {
                    colorIndex = 15;
                }

                int rgbIndex = colorIndex * 3;
                r = this.colorPalette[rgbIndex + 0];
                g = this.colorPalette[rgbIndex + 1];
                b = this.colorPalette[rgbIndex + 2];

                if (this.hasShadow) {
                    int shadowRgbIndex = (colorIndex + 16) * 3;
                    sR = this.colorPalette[shadowRgbIndex + 0];
                    sG = this.colorPalette[shadowRgbIndex + 1];
                    sB = this.colorPalette[shadowRgbIndex + 2];
                } else {
                    ts.color(r, g, b, a);
                }

                i++;
                continue;
            }

            int charIndex = CharacterUtils.validCharacters.indexOf(c);
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
            if (this.hasShadow) {
                ts.color(sR, sG, sB, sA);
                drawChar(ts, column, row, xOff + x + this.shadowOffsetX, y + this.shadowOffsetY);
                ts.color(r, g, b, a);
            }
            drawChar(ts, column, row, xOff + x, y);
            xOff += this.widthLookup[ch];
        }
    }

    public void setColor(int color) {
        this.r = (float) (color >> 16 & 255) / 255.0F;
        this.g = (float) (color >> 8 & 255) / 255.0F;
        this.b = (float) (color & 255) / 255.0F;
        this.a = (float) (color >> 24 & 255) / 255.0F;
        if (this.a == 0.0F) {
            this.a = 1.0F;
        }
    }

    public void setShadowColor(int shadowColor) {
        this.sR = (float) (shadowColor >> 16 & 255) / 255.0F;
        this.sG = (float) (shadowColor >> 8 & 255) / 255.0F;
        this.sB = (float) (shadowColor & 255) / 255.0F;
        this.sA = (float) (shadowColor >> 24 & 255) / 255.0F;
        if (this.sA == 0.0F) {
            this.sA = 1.0F;
        }
    }

    public void setShadow(boolean enable) {
        this.hasShadow = enable;
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

    private static void drawChar(Tessellator ts, int column, int row, float x, float y) {
        float f = 7.99f;
        float f2 = 0.0f;
        float f3 = 0.0f;
        ts.vertex(x, y + f, 0.0, (float) column / 128.0f + f2, ((float) row + f) / 128.0f + f3);
        ts.vertex(x + f, y + f, 0.0, ((float) column + f) / 128.0f + f2, ((float) row + f) / 128.0f + f3);
        ts.vertex(x + f, y, 0.0, ((float) column + f) / 128.0f + f2, (float) row / 128.0f + f3);
        ts.vertex(x, y, 0.0, (float) column / 128.0f + f2, (float) row / 128.0f + f3);
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
