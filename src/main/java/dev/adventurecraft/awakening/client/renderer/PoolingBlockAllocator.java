package dev.adventurecraft.awakening.client.renderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayDeque;
import java.util.Deque;

public class PoolingBlockAllocator implements BlockAllocator {

    private final Deque<ByteBuffer> deque;
    private final int capacity;
    private final int blockSize;

    public PoolingBlockAllocator(int blockSize, int capacity) {
        this.blockSize = blockSize;
        this.capacity = capacity;
        this.deque = new ArrayDeque<>(capacity);
    }

    @Override
    public int blockSize() {
        return this.blockSize;
    }

    @Override
    public ByteBuffer newBlock() {
        ByteBuffer buffer = this.deque.pollFirst();
        if (buffer != null) {
            return buffer;
        }
        return ByteBuffer.allocateDirect(this.blockSize).order(ByteOrder.nativeOrder());
    }

    @Override
    public void returnBlock(ByteBuffer buffer) {
        if (buffer.capacity() != this.blockSize) {
            return;
        }
        if (this.deque.size() >= this.capacity) {
            return;
        }
        buffer.clear();
        this.deque.push(buffer);
    }
}
