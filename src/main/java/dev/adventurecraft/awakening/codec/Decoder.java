package dev.adventurecraft.awakening.codec;

import java.util.function.Function;

public interface Decoder<T> {

    <I> DataResult<Pair<T, I>> decode(DynamicOps<I> ops, I input);

    default <I> DataResult<T> read(DynamicOps<I> ops, I input) {
        return this.decode(ops, input).map(Pair::first);
    }

    default <U> Decoder<U> map(Function<? super T, ? extends U> mapper) {
        return new Decoder<>() {
            public @Override <I> DataResult<Pair<U, I>> decode(DynamicOps<I> ops, I input) {
                return Decoder.this.decode(ops, input).map(p -> p.mapFirst(mapper));
            }

            public @Override String toString() {
                return Decoder.this + "[map]";
            }
        };
    }

    default <U> Decoder<U> flatMap(Function<? super T, ? extends DataResult<? extends U>> mapper) {
        return new Decoder<>() {
            public @Override <I> DataResult<Pair<U, I>> decode(DynamicOps<I> ops, I input) {
                return Decoder.this.decode(ops, input).flatMap(p -> p.remapFirst(mapper));
            }

            public @Override String toString() {
                return Decoder.this + "[flatMap]";
            }
        };
    }
}
