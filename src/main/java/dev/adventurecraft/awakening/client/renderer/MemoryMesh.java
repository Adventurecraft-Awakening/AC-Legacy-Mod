package dev.adventurecraft.awakening.client.renderer;

import dev.adventurecraft.awakening.client.rendering.MemoryTesselator;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public final class MemoryMesh implements AutoCloseable {

    public final BlockAllocator allocator;
    public final List<ByteBuffer> vertexBlocks = new ArrayList<>();

    public MemoryMesh(BlockAllocator allocator) {
        this.allocator = allocator;
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

    public @Override void close() {
        this.vertexBlocks.forEach(this.allocator::returnBlock);
        this.vertexBlocks.clear();
    }
}
