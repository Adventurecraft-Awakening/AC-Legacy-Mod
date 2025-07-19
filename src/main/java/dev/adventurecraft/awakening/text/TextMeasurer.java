package dev.adventurecraft.awakening.text;

import dev.adventurecraft.awakening.common.TextRect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface TextMeasurer {

    @NotNull TextRect measureText(@Nullable CharSequence text, int start, int end, long maxWidth, boolean newLines);

    default @NotNull TextRect measureText(@Nullable CharSequence text, int start, int end) {
        return this.measureText(text, start, end, Long.MAX_VALUE, false);
    }

    default @NotNull TextRect measureText(@Nullable CharSequence text, int start) {
        if (text == null) {
            return TextRect.empty;
        }
        return this.measureText(text, start, text.length());
    }

    default @NotNull TextRect measureText(@Nullable CharSequence text) {
        return this.measureText(text, 0);
    }
}