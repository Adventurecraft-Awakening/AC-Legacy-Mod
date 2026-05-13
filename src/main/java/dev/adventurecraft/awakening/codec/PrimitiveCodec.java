package dev.adventurecraft.awakening.codec;

public interface PrimitiveCodec<T> extends Codec<T> {

    @Override
    <I> DataResult<T> read(DynamicOps<I> ops, I input);

    default @Override <I> DataResult<Pair<T, I>> decode(DynamicOps<I> ops, I input) {
        return this.read(ops, input).map(r -> Pair.of(r, ops.empty()));
    }
}