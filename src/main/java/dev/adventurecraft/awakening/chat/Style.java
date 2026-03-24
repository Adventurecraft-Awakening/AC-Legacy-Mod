package dev.adventurecraft.awakening.chat;

import org.jetbrains.annotations.Nullable;

public class Style {

    public static final Style EMPTY = new Style(null, null, null, null, null, null, null);

    final @Nullable Integer color;
    final @Nullable Integer shadowColor;
    final @Nullable Boolean bold;
    final @Nullable Boolean italic;
    final @Nullable Boolean underlined;
    final @Nullable Boolean strikethrough;
    final @Nullable Boolean obfuscated;

    private Style(
        @Nullable Integer color,
        @Nullable Integer shadowColor,
        @Nullable Boolean bold,
        @Nullable Boolean italic,
        @Nullable Boolean underlined,
        @Nullable Boolean strikethrough,
        @Nullable Boolean obfuscated
    ) {
        this.color = color;
        this.shadowColor = shadowColor;
        this.bold = bold;
        this.italic = italic;
        this.underlined = underlined;
        this.strikethrough = strikethrough;
        this.obfuscated = obfuscated;
    }

    public @Nullable Integer getColor() {
        return this.color;
    }

    public @Nullable Integer getShadowColor() {
        return this.shadowColor;
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

    public Style applyFormat(ChatFormat format) {
        Integer color = this.color;
        Boolean bold = this.bold;
        Boolean italic = this.italic;
        Boolean strikethrough = this.strikethrough;
        Boolean underlined = this.underlined;
        Boolean obfuscated = this.obfuscated;
        switch (format) {
            case OBFUSCATED -> obfuscated = true;
            case BOLD -> bold = true;
            case STRIKETHROUGH -> strikethrough = true;
            case UNDERLINE -> underlined = true;
            case ITALIC -> italic = true;
            case RESET -> {
                return EMPTY;
            }
            default -> color = format.color();
        }
        return new Style(color, this.shadowColor, bold, italic, underlined, strikethrough, obfuscated);
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
            this.obfuscated != null ? this.obfuscated : style.obfuscated
        );
    }
}
