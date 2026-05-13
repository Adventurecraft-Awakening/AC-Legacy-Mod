package dev.adventurecraft.awakening.nbt;

import java.util.Optional;

public interface NumericTag extends PrimitiveTag {

    Number box();

    byte byteValue();

    short shortValue();

    int intValue();

    long longValue();

    float floatValue();

    double doubleValue();

    default @Override Optional<Number> asNumber() {
        return Optional.of(this.box());
    }

    default @Override Optional<Byte> asByte() {
        return Optional.of(this.byteValue());
    }

    default @Override Optional<Short> asShort() {
        return Optional.of(this.shortValue());
    }

    default @Override Optional<Integer> asInt() {
        return Optional.of(this.intValue());
    }

    default @Override Optional<Long> asLong() {
        return Optional.of(this.longValue());
    }

    default @Override Optional<Float> asFloat() {
        return Optional.of(this.floatValue());
    }

    default @Override Optional<Double> asDouble() {
        return Optional.of(this.doubleValue());
    }

    default @Override Optional<Boolean> asBool() {
        return Optional.of(this.byteValue() != 0);
    }
}
