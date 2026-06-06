package dev.adventurecraft.awakening.dom;

import dev.adventurecraft.awakening.util.ObjectUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;

public interface NumberNode extends Node {

    Number asNumber();

    default long asLong() {
        return this.asNumber().longValue();
    }

    default double asDouble() {
        return this.asNumber().doubleValue();
    }

    default @Override <T> Optional<T> visit(NodeConsumer<T> consumer) {
        return consumer.accept(this.asNumber().toString());
    }

    default @Override <T> Optional<T> visit(StyledConsumer<T> consumer, Style style) {
        return consumer.accept(this.asNumber().toString(), style);
    }

    static I8 of(byte value) {
        return I8.cached(value);
    }

    static I16 of(short value) {
        if (value == 0) {
            return I16.ZERO;
        }
        return new I16(value);
    }

    static I32 of(int value) {
        if (value == 0) {
            return I32.ZERO;
        }
        return new I32(value);
    }

    static I64 of(long value) {
        if (value == 0) {
            return I64.ZERO;
        }
        return new I64(value);
    }

    static F32 of(float value) {
        if (Float.floatToRawIntBits(value) == 0) {
            return F32.ZERO;
        }
        return new F32(value);
    }

    static F64 of(double value) {
        if (Double.doubleToRawLongBits(value) == 0) {
            return F64.ZERO;
        }
        return new F64(value);
    }

    static Box of(Number value) {
        return new Box(value);
    }

    static NumberNode ofUnbox(Number value) {
        return switch (value) {
            case Byte i8 -> of(i8);
            case Short i16 -> of(i16);
            case Integer i32 -> of(i32);
            case Long i64 -> of(i64);
            case Float f32 -> of(f32);
            case Double f64 -> of(f64);
            default -> of(value);
        };
    }

    interface IntNumber extends NumberNode {}

    interface FloatNumber extends NumberNode {}

    record I8(byte value) implements IntNumber {

        private static final I8[] CACHE = ObjectUtil.make(() -> {
            var cache = new I8[256];
            for (int i = 0; i < cache.length; i++) {
                cache[i] = new I8((byte) i);
            }
            return cache;
        });

        private static final String[] STRING_CACHE = Arrays
            .stream(CACHE)
            .map(i -> "i8(" + i.value + ")")
            .toArray(String[]::new);

        public static final I8 ZERO = cached((byte) 0);

        public @Override Number asNumber() {
            return this.value;
        }

        public @Override @NotNull String toString() {
            return STRING_CACHE[this.value & 0xff];
        }

        public static I8 cached(byte value) {
            return CACHE[value & 0xff];
        }
    }

    record I16(short value) implements IntNumber {

        public static final I16 ZERO = new I16((short) 0);

        public @Override Number asNumber() {
            return this.value;
        }

        public @Override @NotNull String toString() {
            return "i16(" + this.value + ")";
        }
    }

    record I32(int value) implements IntNumber {

        public static final I32 ZERO = new I32(0);

        public @Override Number asNumber() {
            return this.value;
        }

        public @Override @NotNull String toString() {
            return "i32(" + this.value + ")";
        }
    }

    record I64(long value) implements IntNumber {

        public static final I64 ZERO = new I64(0);

        public @Override Number asNumber() {
            return this.value;
        }

        public @Override @NotNull String toString() {
            return "i64(" + this.value + ")";
        }
    }

    record F32(float value) implements FloatNumber {

        public static final F32 ZERO = new F32(0f);

        public @Override Number asNumber() {
            return this.value;
        }

        public @Override double asDouble() {
            return this.value;
        }

        public @Override @NotNull String toString() {
            return "f32(" + this.value + ")";
        }
    }

    record F64(double value) implements FloatNumber {

        public static final F64 ZERO = new F64(0.);

        public @Override Number asNumber() {
            return this.value;
        }

        public @Override double asDouble() {
            return this.value;
        }

        public @Override @NotNull String toString() {
            return "f64(" + this.value + ")";
        }
    }

    record Box(Number value) implements NumberNode {

        public @Override Number asNumber() {
            return this.value;
        }

        public @Override @NotNull String toString() {
            return "number(" + this.value.toString() + ")";
        }
    }
}
