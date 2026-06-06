package dev.adventurecraft.awakening.extension.nbt;

import dev.adventurecraft.awakening.nbt.TagVisitor;
import net.minecraft.nbt.Tag;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Optional;

public interface ExTag {

    default void invokeWrite(DataOutput output)
        throws IOException {
        throw new AssertionError();
    }

    default void invokeRead(DataInput input)
        throws IOException {
        throw new AssertionError();
    }

    void accept(TagVisitor visitor);

    Tag copy();

    default Optional<String> asString() {
        return Optional.empty();
    }

    default Optional<Number> asNumber() {
        return Optional.empty();
    }

    default Optional<Byte> asByte() {
        return this.asNumber().map(Number::byteValue);
    }

    default Optional<Short> asShort() {
        return this.asNumber().map(Number::shortValue);
    }

    default Optional<Integer> asInt() {
        return this.asNumber().map(Number::intValue);
    }

    default Optional<Long> asLong() {
        return this.asNumber().map(Number::longValue);
    }

    default Optional<Float> asFloat() {
        return this.asNumber().map(Number::floatValue);
    }

    default Optional<Double> asDouble() {
        return this.asNumber().map(Number::doubleValue);
    }

    default Optional<Boolean> asBool() {
        return this.asByte().map(b -> b != 0);
    }
}
