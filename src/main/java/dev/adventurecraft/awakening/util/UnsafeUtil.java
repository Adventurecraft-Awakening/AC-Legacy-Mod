package dev.adventurecraft.awakening.util;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public final class UnsafeUtil {

    public static final Unsafe UNSAFE;

    public static <T> T allocateInstance(Class<T> type) {
        try {
            //noinspection unchecked
            return (T) UNSAFE.allocateInstance(type);
        }
        catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    static {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            UNSAFE = (Unsafe) field.get(null);
        }
        catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Couldn't obtain reference to sun.misc.Unsafe", e);
        }
    }
}
