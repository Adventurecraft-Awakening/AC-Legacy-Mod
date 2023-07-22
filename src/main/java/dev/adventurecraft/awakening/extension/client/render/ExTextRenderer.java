package dev.adventurecraft.awakening.extension.client.render;

import dev.adventurecraft.awakening.common.TextRect;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.texture.TextureManager;

public interface ExTextRenderer {

    void init(GameOptions var1, String var2, TextureManager var3);

    static int getShadowColor(int color) {
        int tmp = color & 0xff000000;
        int shadowColor = (color & 0xfcfcfc) >> 2;
        return shadowColor + tmp;
    }

    void drawText(
        CharSequence text, int start, int end,
        float x, float y, int color, boolean shadow, float sX, float sY, int sColor);

    default void drawString(
        CharSequence text, int start, int end,
        float x, float y, int color, boolean shadow) {
        this.drawText(
            text, start, end,
            x, y, color, shadow, x + 1.0F, y + 1.0F, getShadowColor(color));
    }

    default void drawString(CharSequence text, float x, float y, int color, boolean shadow) {
        this.drawString(text, 0, text.length(), x, y, color, shadow);
    }

    TextRect getTextWidth(CharSequence text, int start, int end, long maxWidth);

    default TextRect getTextWidth(CharSequence text, int start, int end) {
        return this.getTextWidth(text, start, end, Long.MAX_VALUE);
    }

    default TextRect getTextWidth(CharSequence text, int start) {
        return this.getTextWidth(text, start, text.length());
    }
}
