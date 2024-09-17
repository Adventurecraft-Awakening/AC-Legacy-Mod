package dev.adventurecraft.awakening.extension.client.render;

import dev.adventurecraft.awakening.common.TextRect;
import dev.adventurecraft.awakening.common.TextRendererState;
import net.minecraft.client.Options;
import net.minecraft.client.renderer.Textures;
import org.jetbrains.annotations.NotNull;

public interface ExTextRenderer {

    void init(Options var1, String var2, Textures var3);

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
            x, y, color, shadow, 1.0F, 1.0F, getShadowColor(color));
    }

    default void drawString(CharSequence text, float x, float y, int color, boolean shadow) {
        this.drawString(text, 0, text.length(), x, y, color, shadow);
    }

    TextRendererState createState();

    @NotNull
    TextRect getTextWidth(CharSequence text, int start, int end, long maxWidth, boolean newLines);

    @NotNull
    default TextRect getTextWidth(CharSequence text, int start, int end) {
        return this.getTextWidth(text, start, end, Long.MAX_VALUE, false);
    }

    @NotNull
    default TextRect getTextWidth(CharSequence text, int start) {
        if (text == null) {
            return TextRect.empty;
        }
        return this.getTextWidth(text, start, text.length());
    }
}
