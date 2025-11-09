package dev.adventurecraft.awakening.extension.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Optional;

public interface ExTag {

    void invokeWrite(DataOutput output)
        throws IOException;

    void invokeRead(DataInput input)
        throws IOException;

    default Optional<String> getString() {
        return Optional.empty();
    }
}
