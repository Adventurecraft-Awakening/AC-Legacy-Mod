package dev.adventurecraft.awakening.util;

import it.unimi.dsi.fastutil.chars.CharPredicate;

import javax.annotation.Nullable;

public final class StringUtil {

    public static boolean isNullOrEmpty(@Nullable CharSequence value) {
        return value == null || value.isEmpty();
    }

    public static int indexOf(CharSequence value, CharPredicate predicate) {
        int len = value.length();
        for (int i = 0; i < len; i++) {
            if (predicate.test(value.charAt(i))) {
                return i;
            }
        }
        return -1;
    }
}
