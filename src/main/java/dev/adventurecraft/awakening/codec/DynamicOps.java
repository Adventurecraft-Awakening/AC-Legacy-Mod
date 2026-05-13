package dev.adventurecraft.awakening.codec;

public interface DynamicOps<T> {

    T empty();

    T of(String value);

    T ofNumber(Number value);

    DataResult<Number> getNumber(T input);

    DataResult<String> getString(T input);

    default T of(byte value) {
        return this.ofNumber(value);
    }

    default T of(short value) {
        return this.ofNumber(value);
    }

    default T of(int value) {
        return this.ofNumber(value);
    }

    default T of(long value) {
        return this.ofNumber(value);
    }

    default T of(float value) {
        return this.ofNumber(value);
    }

    default T of(double value) {
        return this.ofNumber(value);
    }

    default T of(boolean value) {
        return this.of((byte) (value ? 1 : 0));
    }

    default DataResult<Boolean> getBool(T input) {
        return this.getNumber(input).map(n -> n.byteValue() != 0);
    }
}
