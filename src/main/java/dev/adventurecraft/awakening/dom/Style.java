package dev.adventurecraft.awakening.dom;

import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;

public class Style {

    public static final Style EMPTY = new Style(null, null, null, null, null, null, null, null);

    // TODO: collapse into style stack
    public final @Nullable Integer color;
    public final @Nullable Integer shadowColor;
    public final @Nullable Boolean bold;
    public final @Nullable Boolean italic;
    public final @Nullable Boolean underlined;
    public final @Nullable Boolean strikethrough;
    public final @Nullable Boolean obfuscated;
    public final @Nullable NumberFormat numberFormat;

    public Style(
        @Nullable Integer color,
        @Nullable Integer shadowColor,
        @Nullable Boolean bold,
        @Nullable Boolean italic,
        @Nullable Boolean underlined,
        @Nullable Boolean strikethrough,
        @Nullable Boolean obfuscated,
        @Nullable NumberFormat numberFormat
    ) {
        this.color = color;
        this.shadowColor = shadowColor;
        this.bold = bold;
        this.italic = italic;
        this.underlined = underlined;
        this.strikethrough = strikethrough;
        this.obfuscated = obfuscated;
        this.numberFormat = numberFormat;
    }

    public @Nullable Integer getColor() {
        return this.color;
    }

    public @Nullable Integer getShadowColor() {
        return this.shadowColor;
    }

    public @Nullable NumberFormat getNumberFormat() {
        return this.numberFormat;
    }

    public boolean isBold() {
        return Boolean.TRUE.equals(this.bold);
    }

    public boolean isItalic() {
        return Boolean.TRUE.equals(this.italic);
    }

    public boolean isStrikethrough() {
        return Boolean.TRUE.equals(this.strikethrough);
    }

    public boolean isUnderlined() {
        return Boolean.TRUE.equals(this.underlined);
    }

    public boolean isObfuscated() {
        return Boolean.TRUE.equals(this.obfuscated);
    }

    public boolean isEmpty() {
        return this == EMPTY;
    }

    public Style applyTo(Style style) {
        if (this == EMPTY) {
            return style;
        }
        if (style == EMPTY) {
            return this;
        }
        return new Style(
            this.color != null ? this.color : style.color,
            this.shadowColor != null ? this.shadowColor : style.shadowColor,
            this.bold != null ? this.bold : style.bold,
            this.italic != null ? this.italic : style.italic,
            this.underlined != null ? this.underlined : style.underlined,
            this.strikethrough != null ? this.strikethrough : style.strikethrough,
            this.obfuscated != null ? this.obfuscated : style.obfuscated,
            this.numberFormat != null ? this.numberFormat : style.numberFormat
        );
    }
}
