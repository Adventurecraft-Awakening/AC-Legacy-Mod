package dev.adventurecraft.awakening.codec;

import java.util.Optional;
import java.util.function.Function;

public interface Either<L, R> {

    Optional<L> left();

    Optional<R> right();

    <T> T map(Function<? super L, ? extends T> l, Function<? super R, ? extends T> r);

    static <L, R> Either<L, R> left(L value) {
        return new Left<>(value);
    }

    static <L, R> Either<L, R> right(R value) {
        return new Right<>(value);
    }

    default Either<R, L> swap() {
        return map(Either::right, Either::left);
    }

    default <L2> Either<L2, R> flatMap(Function<L, Either<L2, R>> function) {
        return map(function, Either::right);
    }

    static <T> T unwrap(Either<? extends T, ? extends T> either) {
        return either.map(Function.identity(), Function.identity());
    }

    record Left<L, R>(L value) implements Either<L, R> {
        public @Override Optional<L> left() {
            return Optional.of(this.value);
        }

        public @Override Optional<R> right() {
            return Optional.empty();
        }

        public @Override <T> T map(Function<? super L, ? extends T> l, Function<? super R, ? extends T> r) {
            return l.apply(this.value);
        }
    }

    record Right<L, R>(R value) implements Either<L, R> {
        public @Override Optional<L> left() {
            return Optional.empty();
        }

        public @Override Optional<R> right() {
            return Optional.of(this.value);
        }

        public @Override <T> T map(Function<? super L, ? extends T> l, Function<? super R, ? extends T> r) {
            return r.apply(this.value);
        }
    }
}
