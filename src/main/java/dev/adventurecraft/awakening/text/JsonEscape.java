package dev.adventurecraft.awakening.text;

import dev.adventurecraft.awakening.util.HexConvert;

public class JsonEscape {

    public static final JsonEscape DEFAULT = new JsonEscape();

    // Licensed to the .NET Foundation under one or more agreements.

    // Only allow ASCII characters between ' ' (0x20) and '~' (0x7E), inclusively,
    // but exclude characters that need to be escaped as hex: '"', '\'', '&', '+', '<', '>', '`'
    // and exclude characters that need to be escaped by adding a backslash: '\n', '\r', '\t', '\\', '\b', '\f'
    //
    // non-zero = allowed, 0 = disallowed
    public static final int LastAsciiCharacter = 0x7F;
    private static final byte[] AllowList = {
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // U+0000..U+000F
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // U+0010..U+001F
        1, 1, 0, 1, 1, 1, 0, 0, 1, 1, 1, 0, 1, 1, 1, 1, // U+0020..U+002F
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 0, 1, // U+0030..U+003F
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, // U+0040..U+004F
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, // U+0050..U+005F
        0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, // U+0060..U+006F
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, // U+0070..U+007F

        // Also include the ranges from U+0080 to U+00FF for performance to avoid UTF8 code from checking boundary.
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, //
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, //
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, //
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, //
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, //
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, //
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, //
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // U+00F0..U+00FF
    };

    private static boolean needsEscaping(char value) {
        return AllowList[value & 0xff] == 0;
    }

    private static boolean isAsciiValue(char value) {
        return (value & 0xffff) <= LastAsciiCharacter;
    }

    public final CharSequence translate(CharSequence value) {
        int len = value.length();
        int i = this.indexOfEscape(value, 0, len);
        if (i == len) {
            return value;
        }

        var builder = new StringBuilder();
        builder.append(value, 0, i);
        this.translateCore(value, i, len, TextBuffer.of(builder));
        return builder;
    }

    public int indexOfEscape(CharSequence value, int start, int end) {
        int i = start;
        while (i < end) {
            char c1 = value.charAt(i);
            if (!isAsciiValue(c1)) {
                break;
            }
            if (needsEscaping(c1)) {
                break;
            }
            i++;
        }
        return i;
    }

    public int translate(CharSequence value, int start, int end, TextBuffer output) {
        int i = this.indexOfEscape(value, start, end);
        if (i != start) {
            output.append(value, start, i);
        }
        return this.translateCore(value, i, end, output);
    }

    private int translateCore(CharSequence value, int start, int end, TextBuffer output) {
        int i = start;
        while (i < end) {
            char c1 = value.charAt(i);
            i++;
            if (isAsciiValue(c1)) {
                if (needsEscaping(c1)) {
                    this.escapeChar(c1, output);
                }
                else {
                    output.append(c1);
                }
            }
            else {
                this.appendUnicode(c1, output);
                if (Character.isHighSurrogate(c1) && i < end) {
                    char c2 = value.charAt(i);
                    if (Character.isLowSurrogate(c2)) {
                        this.appendUnicode(c2, output);
                        i++;
                    }
                }
            }
        }
        return i - start;
    }

    protected void escapeChar(char value, TextBuffer output) {
        switch (value) {
            case '"':
                output.append("\\\"");
                break;
            case 'n':
                output.append("\\n");
                break;
            case 'r':
                output.append("\\r");
                break;
            case 't':
                output.append("\\t");
                break;
            case '\\':
                output.append("\\\\");
                break;
            case 'b':
                output.append("\\b");
                break;
            case 'f':
                output.append("\\f");
                break;
            default:
                this.appendUnicode(value, output);
                break;
        }
    }

    protected void appendUnicode(char value, TextBuffer output) {
        output.append("\\u");
        HexConvert.appendHex4((short) value, output, HexConvert.CASING_LOWER);
    }
}
