package dev.adventurecraft.awakening.codec;

import java.util.function.Function;

public interface Encoder<T> {

    <O> DataResult<O> encode(T value, DynamicOps<O> ops);

    default <U> Encoder<U> comap(Function<? super U, ? extends T> mapper) {
        return new Encoder<>() {
            public @Override <O> DataResult<O> encode(U value, DynamicOps<O> ops) {
                return Encoder.this.encode(mapper.apply(value), ops);
            }

            public @Override String toString() {
                return Encoder.this + "[comap]";
            }
        };
    }
}
