package dev.adventurecraft.awakening.codec;

import java.util.function.Function;

public interface Codec<T> extends Encoder<T>, Decoder<T> {

    default <U> Codec<U> xmap(Function<? super T, ? extends U> to, Function<? super U, ? extends T> from) {
        return Codec.of(comap(from), map(to), this + "[xmap]");
    }

    default <U, E> Codec<U> comapFlatMap(
        Function<? super T, DataResult<? extends U>> to,
        Function<? super U, ? extends T> from
    ) {
        return Codec.of(comap(from), flatMap(to), this + "[comapFlatMap]");
    }

    static <U> Codec<U> of(Encoder<U> encoder, Decoder<U> decoder, String name) {
        return new Codec<>() {
            public @Override <I> DataResult<Pair<U, I>> decode(DynamicOps<I> ops, I input) {
                return decoder.decode(ops, input);
            }

            public @Override <O> DataResult<O> encode(U value, DynamicOps<O> ops) {
                return encoder.encode(value, ops);
            }

            public @Override String toString() {
                return name;
            }
        };
    }

    PrimitiveCodec<String> STRING = new PrimitiveCodec<>() {
        public @Override <I> DataResult<String> read(DynamicOps<I> ops, I input) {
            return ops.getString(input);
        }

        public @Override <O> DataResult<O> encode(String value, DynamicOps<O> ops) {
            return DataResult.ok(ops.of(value));
        }

        public @Override String toString() {
            return "String";
        }
    };
}
