package dev.adventurecraft.awakening.primitives;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Property<T> extends Supplier<T>, Consumer<T> {

    default void set(T value) {
        this.accept(value);
    }

    default <U> Property<U> map(Function<T, U> getter, Function<U, T> setter) {
        return new ProxyProperty<>(() -> getter.apply(this.get()), (value) -> this.set(setter.apply(value)));
    }

    static <T> Property<T> of(Supplier<T> getter, Consumer<T> setter) {
        return new ProxyProperty<>(getter, setter);
    }
}

record ProxyProperty<T>(Supplier<T> getter, Consumer<T> setter) implements Property<T> {

    public @Override T get() {
        return this.getter.get();
    }

    public @Override void accept(T value) {
        this.setter.accept(value);
    }
}