package dev.adventurecraft.awakening.text;

import dev.adventurecraft.awakening.codec.Codec;
import dev.adventurecraft.awakening.codec.DataResult;
import dev.adventurecraft.awakening.codec.DynamicOps;
import dev.adventurecraft.awakening.codec.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;

public interface NamedEnum {

    @NotNull String getName();

    static <E extends NamedEnum> EnumCodec<E> codec(E[] values) {
        Map<String, E> map = values.length <= 8
            ? new Object2ObjectArrayMap<>(values.length)
            : new Object2ObjectOpenHashMap<>(values.length);
        for (E value : values) {
            map.put(value.getName(), value);
        }
        return new EnumCodec<>(map);
    }

    record EnumCodec<E extends NamedEnum>(Map<String, E> values) implements Codec<E> {

        public @Override <O> DataResult<O> encode(E value, DynamicOps<O> ops) {
            return EnumCodec.STRING.encode(value.getName(), ops);
        }

        public @Override <I> DataResult<Pair<E, I>> decode(DynamicOps<I> ops, I input) {
            return EnumCodec.STRING.decode(ops, input).flatMap(p -> p.remapFirst(this::parseName));
        }

        public Optional<E> byName(String name) {
            return Optional.ofNullable(this.values.getOrDefault(name, null));
        }

        public DataResult<E> parseName(String name) {
            return DataResult.okOr(this.byName(name), name);
        }
    }
}
