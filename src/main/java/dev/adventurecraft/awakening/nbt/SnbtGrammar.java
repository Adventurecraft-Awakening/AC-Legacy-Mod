package dev.adventurecraft.awakening.nbt;

import dev.adventurecraft.awakening.text.JsonEscape;
import dev.adventurecraft.awakening.text.TextBuffer;

import java.util.regex.Pattern;

public final class SnbtGrammar {

    public static final Pattern SIMPLE_VALUE = Pattern.compile("[A-Za-z0-9._+-]+");
    public static final String DEFAULT_INDENT = "  ";

    public static final char ITEM_SPACE = ' ';
    public static final char NEWLINE = '\n';
    public static final char QUOTE = '"';

    public static final char KEY_VALUE_SEPARATOR = ':';
    public static final char ITEM_SEPARATOR = ',';
    public static final char LIST_OPEN = '[';
    public static final char LIST_CLOSE = ']';
    public static final char LIST_TYPE_SEPARATOR = ';';
    public static final char STRUCT_OPEN = '{';
    public static final char STRUCT_CLOSE = '}';

    public static final char TYPE_BYTE = 'b';
    public static final char TYPE_SHORT = 's';
    public static final char TYPE_INT = 'I';
    public static final char TYPE_LONG = 'L';
    public static final char TYPE_FLOAT = 'f';
    public static final char TYPE_DOUBLE = 'd';

    public static String escapeString(CharSequence value) {
        return JsonEscape.DEFAULT.translate(value).toString();
    }

    public static void escapeString(CharSequence value, StringBuilder output) {
        JsonEscape.DEFAULT.translate(value, 0, value.length(), TextBuffer.of(output));
    }
}
