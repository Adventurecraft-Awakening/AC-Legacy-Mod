package dev.adventurecraft.awakening.codec;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public sealed interface DataResult<T> permits DataResult.Ok, DataResult.Err {

    Optional<T> ok();

    Optional<T> partial();

    Optional<Object> error();

    default T unwrap() {
        return this.ok().orElseThrow();
    }

    DataResult<T> withPartial(T value);

    <U> DataResult<U> map(Function<? super T, ? extends U> mapper);

    <U> DataResult<U> flatMap(Function<? super T, ? extends DataResult<U>> mapper);

    static <T> DataResult<T> ok(T value) {
        return new Ok<>(value);
    }

    static <T> DataResult<T> partial(T partial, Object error) {
        return new Err<>(Optional.ofNullable(partial), error);
    }

    static <T> DataResult<T> error(Object error) {
        return new Err<>(Optional.empty(), error);
    }

    static <T> DataResult<T> error(Supplier<?> supplier) {
        return new Err<>(Optional.empty(), supplier);
    }

    static <T> DataResult<T> okOr(
        @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<T> value,
        Object error
    ) {
        return value.isEmpty() ? DataResult.error(error) : new Ok<>(value.get());
    }

    static <T> DataResult<T> okOrElse(
        @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<T> value,
        Supplier<Object> error
    ) {
        return value.isEmpty() ? DataResult.error(error.get()) : new Ok<>(value.get());
    }

    record Ok<T>(T value) implements DataResult<T> {

        public @Override Optional<T> ok() {
            return Optional.of(value);
        }

        public @Override Optional<T> partial() {
            return this.ok();
        }

        public @Override Optional<Object> error() {
            return Optional.empty();
        }

        public @Override T unwrap() {
            return value;
        }

        public @Override Ok<T> withPartial(T value) {
            return this;
        }

        public @Override <U> DataResult<U> map(Function<? super T, ? extends U> mapper) {
            return new Ok<>(mapper.apply(value));
        }

        public @Override <U> DataResult<U> flatMap(Function<? super T, ? extends DataResult<U>> mapper) {
            return mapper.apply(value);
        }
    }

    record Err<T, E>(Optional<T> partialVal, E errVal) implements DataResult<T> {

        public @Override Optional<T> ok() {
            return Optional.empty();
        }

        public @Override Optional<T> partial() {
            return partialVal;
        }

        public @Override Optional<Object> error() {
            return Optional.of(errVal);
        }

        public @Override Err<T, E> withPartial(T value) {
            return new Err<>(Optional.of(value), errVal);
        }

        public @Override <U> DataResult<U> map(Function<? super T, ? extends U> mapper) {
            return new Err<>(partialVal.map(mapper), errVal);
        }

        public @Override <U> DataResult<U> flatMap(Function<? super T, ? extends DataResult<U>> mapper) {
            if (partialVal.isEmpty()) {
                return (DataResult<U>) this;
            }
            return mapper.apply(partialVal.get());
        }
    }
}
