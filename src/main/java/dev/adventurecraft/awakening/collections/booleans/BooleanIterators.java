package dev.adventurecraft.awakening.collections.booleans;

import it.unimi.dsi.fastutil.booleans.BooleanIterator;

public final class BooleanIterators {

    public static int unwrap(BooleanIterator i, boolean[] array, int offset, final int max) {
        if (max < 0) {
            throw new IllegalArgumentException("The maximum number of elements (" + max + ") is negative");
        }
        if (offset < 0 || offset + max > array.length) {
            throw new IllegalArgumentException();
        }
        int j = max;
        while (j-- != 0 && i.hasNext()) {
            array[offset++] = i.nextBoolean();
        }
        return max - j - 1;
    }

    public static int unwrap(BooleanIterator i, boolean[] array) {
        return unwrap(i, array, 0, array.length);
    }
}
