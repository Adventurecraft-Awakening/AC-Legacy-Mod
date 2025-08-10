package dev.adventurecraft.awakening.client.renderer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class MemoryMesh {

    public List<ByteBuffer> vertexBlocks = new ArrayList<>();

    public long sizeInBytes() {
        long size = 0;
        for (var buffer : this.vertexBlocks) {
            size += buffer.remaining();
        }
        return size;
    }
}
