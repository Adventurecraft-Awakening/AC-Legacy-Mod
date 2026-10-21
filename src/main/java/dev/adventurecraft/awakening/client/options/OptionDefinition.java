package dev.adventurecraft.awakening.client.options;

import net.minecraft.client.Option;
import net.minecraft.locale.I18n;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/** Immutable behavior shared by persistence, legacy option buttons and sliders. */
public final class OptionDefinition<T> {

    public enum ChangeSource { LOAD, INTERACTION }

    public interface Codec<T> {
        T decode(String value);

        String encode(T value);
    }

    public interface Slider<T> {
        T value(float progress);

        float progress(T value);
    }

    @FunctionalInterface
    public interface Cycle<T> {
        T next(T current, int direction);
    }

    @FunctionalInterface
    public interface Effect<T> {
        void apply(OptionEffects effects, T value, ChangeSource source);
    }

    private final String key;
    private final T defaultValue;
    private final Codec<T> codec;
    private final UnaryOperator<T> normalize;
    private final Supplier<Option> legacyOption;
    private final Cycle<T> cycle;
    private final Slider<T> slider;
    private final BiFunction<T, I18n, String> label;
    private final Function<T, Boolean> booleanValue;
    private final Effect<T> effect;

    private OptionDefinition(Builder<T> builder) {
        this.key = builder.key;
        this.defaultValue = builder.defaultValue;
        this.codec = builder.codec;
        this.normalize = builder.normalize;
        this.legacyOption = builder.legacyOption;
        this.cycle = builder.cycle;
        this.slider = builder.slider;
        this.label = builder.label;
        this.booleanValue = builder.booleanValue;
        this.effect = builder.effect;
    }

    public String key() {
        return this.key;
    }

    public T defaultValue() {
        return this.defaultValue;
    }

    public T normalize(T value) {
        return this.normalize.apply(value);
    }

    public T decode(String value) {
        return this.normalize(this.codec.decode(value));
    }

    public String encode(T value) {
        return this.codec.encode(value);
    }

    public boolean matches(Option option) {
        return this.legacyOption != null && this.legacyOption.get() == option;
    }

    boolean canCycle() {
        return this.cycle != null;
    }

    T next(T current, int direction) {
        return this.cycle.next(current, direction);
    }

    Slider<T> slider() {
        return this.slider;
    }

    String label(T value, I18n translations) {
        return this.label == null ? null : this.label.apply(value, translations);
    }

    Boolean booleanValue(T value) {
        return this.booleanValue == null ? null : this.booleanValue.apply(value);
    }

    void apply(OptionEffects effects, T value, ChangeSource source) {
        this.effect.apply(effects, value, source);
    }

    public static <T> Builder<T> builder(String key, T defaultValue, Codec<T> codec) {
        return new Builder<>(key, defaultValue, codec);
    }

    public static <T> Codec<T> codec(Function<String, T> decode) {
        return new Codec<>() {
            @Override
            public T decode(String value) {
                return decode.apply(value);
            }

            @Override
            public String encode(T value) {
                return value.toString();
            }
        };
    }

    public static final class Builder<T> {

        private final String key;
        private final T defaultValue;
        private final Codec<T> codec;
        private UnaryOperator<T> normalize = UnaryOperator.identity();
        private Supplier<Option> legacyOption;
        private Cycle<T> cycle;
        private Slider<T> slider;
        private BiFunction<T, I18n, String> label;
        private Function<T, Boolean> booleanValue;
        private Effect<T> effect = (effects, value, source) -> {};

        private Builder(String key, T defaultValue, Codec<T> codec) {
            this.key = Objects.requireNonNull(key);
            this.defaultValue = Objects.requireNonNull(defaultValue);
            this.codec = Objects.requireNonNull(codec);
        }

        public Builder<T> normalize(UnaryOperator<T> normalize) {
            this.normalize = normalize;
            return this;
        }

        /** Delayed resolution keeps definition defaults usable before enum extension finishes. */
        public Builder<T> option(Supplier<Option> legacyOption) {
            this.legacyOption = legacyOption;
            return this;
        }

        public Builder<T> cycle(UnaryOperator<T> cycle) {
            return this.cycle((value, direction) -> cycle.apply(value));
        }

        public Builder<T> cycle(Cycle<T> cycle) {
            this.cycle = cycle;
            return this;
        }

        public Builder<T> slider(Slider<T> slider) {
            this.slider = slider;
            return this;
        }

        public Builder<T> label(Function<T, String> label) {
            return this.label((value, translations) -> label.apply(value));
        }

        public Builder<T> label(BiFunction<T, I18n, String> label) {
            this.label = label;
            return this;
        }

        public Builder<T> booleanValue(Function<T, Boolean> booleanValue) {
            this.booleanValue = booleanValue;
            return this;
        }

        public Builder<T> effect(Effect<T> effect) {
            this.effect = effect;
            return this;
        }

        public OptionDefinition<T> build() {
            return new OptionDefinition<>(this);
        }
    }
}
