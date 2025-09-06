package dev.adventurecraft.awakening.extension.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public interface ExTag {

    void invokeWrite(DataOutput output)
        throws IOException;

    void invokeRead(DataInput input)
        throws IOException;
}
