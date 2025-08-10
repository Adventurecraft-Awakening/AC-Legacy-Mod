package dev.adventurecraft.awakening.client.renderer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class MemoryMesh {

    public final List<ByteBuffer> vertexBlocks = new ArrayList<>();

    public MemoryMesh() {
    }

    public long sizeInBytes() {
        long size = 0;
        for (var buffer : this.vertexBlocks) {
            size += buffer.remaining();
        }
        return size;
    }
}
