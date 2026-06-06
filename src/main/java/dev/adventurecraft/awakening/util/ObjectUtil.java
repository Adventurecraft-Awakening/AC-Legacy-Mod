package dev.adventurecraft.awakening.util;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ObjectUtil {

    public static <T> T make(Supplier<T> supplier) {
        return supplier.get();
    }

    public static <T> T make(T object, Consumer<? super T> consumer) {
        consumer.accept(object);
        return object;
    }

    public static <K extends Enum<K>, V> Map<K, V> makeEnumMap(Class<K> type, Function<K, V> mapper) {
        var map = new EnumMap<K, V>(type);
        for (K key : type.getEnumConstants()) {
            map.put(key, mapper.apply(key));
        }
        return map;
    }
}
