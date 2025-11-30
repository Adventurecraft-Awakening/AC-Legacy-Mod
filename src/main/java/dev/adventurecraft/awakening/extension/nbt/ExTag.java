package dev.adventurecraft.awakening.extension.nbt;

import net.minecraft.nbt.Tag;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Optional;

public interface ExTag {

    void invokeWrite(DataOutput output)
        throws IOException;

    void invokeRead(DataInput input)
        throws IOException;

    Tag copy();

    default Optional<String> getString() {
        return Optional.empty();
    }

    default Optional<Integer> getInt() {
        return Optional.empty();
    }

    default Optional<Long> getLong() {
        return this.getInt().map(i -> (long) i);
    }

    default Optional<Double> getDouble() {
        return this.getLong().map(l -> (double) l);
    }
}
