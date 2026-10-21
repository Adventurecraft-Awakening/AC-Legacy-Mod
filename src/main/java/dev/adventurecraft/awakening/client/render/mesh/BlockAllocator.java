package dev.adventurecraft.awakening.client.render.mesh;

import java.nio.ByteBuffer;

public interface BlockAllocator {

    int blockSize();

    ByteBuffer newBlock();

    void returnBlock(ByteBuffer block);
}
