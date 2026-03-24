package dev.adventurecraft.awakening.chat;

import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public enum ChatFormat {
    BLACK('0', 0),
    DARK_BLUE('1', 0xaa0000),
    DARK_GREEN('2', 0x00aa00),
    DARK_AQUA('3', 0xaaaa00),
    DARK_RED('4', 0x0000aa),
    DARK_PURPLE('5', 0xaa00aa),
    GOLD('6', 0x00aaff),
    GRAY('7', 0xaaaaaa),
    DARK_GRAY('8', 0x555555),
    BLUE('9', 0xff5555),
    GREEN('a', 0x55ff55),
    AQUA('b', 0xffff55),
    RED('c', 0x5555ff),
    LIGHT_PURPLE('d', 0xff55ff),
    YELLOW('e', 0x55ffff),
    WHITE('f', 0xffffff),

    OBFUSCATED('k'),
    BOLD('l'),
    STRIKETHROUGH('m'),
    UNDERLINE('n'),
    ITALIC('o'),
    RESET('r', null);

    private static final ChatFormat[] CODE_LOOKUP = {
        BLACK, DARK_BLUE, DARK_GREEN, DARK_AQUA, DARK_RED, DARK_PURPLE, GOLD, GRAY, // [0..=7]
        DARK_GRAY, BLUE, GREEN, AQUA, RED, LIGHT_PURPLE, YELLOW, WHITE,             // [8..=f]
        null, null, null, null, OBFUSCATED, BOLD, STRIKETHROUGH, UNDERLINE,         // [g,h,i,j, k,l,m,n]
        ITALIC, null, null, RESET, null, null, null, null,                          // [o,p,q,r, s,t,u,v]
    };

    private final char code;
    private final boolean isFormat;
    private final @Nullable Integer color;
    private final String toString;

    ChatFormat(char code, boolean isFormat, @Nullable Integer color) {
        this.code = code;
        this.isFormat = isFormat;
        this.color = color;
        this.toString = "§" + code;
    }

    ChatFormat(char code, @Nullable Integer color) {
        this(code, false, color);
    }

    ChatFormat(char code) {
        this(code, true, null);
    }

    public char code() {
        return this.code;
    }

    public boolean isFormat() {
        return this.isFormat;
    }

    public boolean isColor() {
        return !this.isFormat && this.code() != RESET.code();
    }

    public @Nullable Integer color() {
        return this.color;
    }

    public String getName() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    public @Override String toString() {
        return this.toString;
    }

    public static @Nullable ChatFormat getByCode(char c) {
        int i = Character.toLowerCase(c) - '0';
        if (i < 0 || i >= CODE_LOOKUP.length) {
            return null;
        }
        return CODE_LOOKUP[i];
    }
}
