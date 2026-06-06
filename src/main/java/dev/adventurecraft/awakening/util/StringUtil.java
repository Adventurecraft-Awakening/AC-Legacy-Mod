package dev.adventurecraft.awakening.util;

import it.unimi.dsi.fastutil.chars.CharPredicate;

import javax.annotation.Nullable;

public final class StringUtil {

    public static boolean isNullOrEmpty(@Nullable CharSequence value) {
        return value == null || value.isEmpty();
    }

    public static int indexOf(CharSequence value, int start, int end, CharPredicate predicate) {
        for (int i = start; i < end; i++) {
            if (predicate.test(value.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    public static int indexOf(CharSequence value, int start, CharPredicate predicate) {
        return indexOf(value, start, value.length(), predicate);
    }

    public static int indexOf(CharSequence value, CharPredicate predicate) {
        return indexOf(value, 0, predicate);
    }

    public static boolean startsWith(CharSequence value, CharSequence other) {
        int len = other.length();
        if (value.length() < len) {
            return false;
        }

        for (int i = 0; i < len; i++) {
            if (value.charAt(i) != other.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    public static String toString(CharSequence value, int start) {
        return value.subSequence(start, value.length()).toString();
    }

    public static int commonPrefix(CharSequence a, CharSequence b) {
        int limit = Math.min(a.length(), b.length());
        int i = 0;
        while (i < limit && a.charAt(i) == b.charAt(i)) {
            i++;
        }
        if (isSurrogatePairAt(a, i - 1) || isSurrogatePairAt(b, i - 1)) {
            i--;
        }
        return i;
    }

    public static boolean isSurrogatePairAt(CharSequence value, int index) {
        return index >= 0
            && index <= (value.length() - 2)
            && Character.isHighSurrogate(value.charAt(index))
            && Character.isLowSurrogate(value.charAt(index + 1));
    }
}
