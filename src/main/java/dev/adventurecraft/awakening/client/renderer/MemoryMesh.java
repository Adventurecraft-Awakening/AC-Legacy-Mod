package dev.adventurecraft.awakening.client.renderer;

import dev.adventurecraft.awakening.client.rendering.MemoryTesselator;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public final class MemoryMesh {

    public final List<ByteBuffer> vertexBlocks = new ArrayList<>();

    public MemoryMesh() {
    }

    public int vertexStride() {
        return MemoryTesselator.BYTE_STRIDE;
    }

    public long getSizeInBytes() {
        long size = 0;
        for (var buffer : this.vertexBlocks) {
            size += buffer.remaining();
        }
        return size;
    }

    public long getVertexCount() {
        return this.getSizeInBytes() / this.vertexStride();
    }
}
