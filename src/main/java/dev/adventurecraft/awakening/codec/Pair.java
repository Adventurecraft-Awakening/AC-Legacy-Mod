package dev.adventurecraft.awakening.codec;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public record Pair<A, B>(A first, B second) {

    public static <A, B> Pair<A, B> of(A first, B second) {
        return new Pair<>(first, second);
    }

    public <A2> Pair<A2, B> mapFirst(Function<? super A, ? extends A2> function) {
        return new Pair<>(function.apply(this.first), this.second);
    }

    public <B2> Pair<A, B2> mapSecond(Function<? super B, ? extends B2> function) {
        return new Pair<>(this.first, function.apply(this.second));
    }

    public <U> DataResult<Pair<U, B>> remapFirst(
        Function<? super A, ? extends DataResult<? extends U>> mapper
    ) {
        return mapper.apply(this.first).map(r -> Pair.of(r, this.second));
    }

    public Pair<B, A> swap() {
        return new Pair<>(this.second, this.first);
    }

    public @Override @NotNull String toString() {
        return "(" + this.first + ", " + this.second + ")";
    }
}
