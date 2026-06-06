package dev.adventurecraft.awakening.util;

import java.util.function.IntFunction;
import java.util.function.Supplier;

public final class ArrayUtil {

    public static <T> T[] fill(T[] array, Supplier<T> supplier) {
        for (int i = 0; i < array.length; i++) {
            array[i] = supplier.get();
        }
        return array;
    }

    public static <T> T[] fill(T[] array, IntFunction<T> function) {
        for (int i = 0; i < array.length; i++) {
            array[i] = function.apply(i);
        }
        return array;
    }
}
