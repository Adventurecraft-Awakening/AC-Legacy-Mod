package dev.adventurecraft.awakening.client.renderer;

import java.nio.ByteBuffer;

public interface BlockAllocator {

    int blockSize();

    ByteBuffer newBlock();

    void returnBlock(ByteBuffer block);
}
