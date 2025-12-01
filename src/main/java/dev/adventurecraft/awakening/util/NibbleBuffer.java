package dev.adventurecraft.awakening.util;

import java.nio.*;

public abstract sealed class NibbleBuffer permits HeapNibbleBuffer {

    int mark = -1;
    int position = 0;
    int limit;
    final int capacity;

    NibbleBuffer(int mark, int pos, int lim, int cap) {
        if (cap < 0) {
            throw createCapacityException(cap);
        }
        this.capacity = cap;
        this.limit(lim);
        this.position(pos);
        if (mark >= 0) {
            if (mark > pos) {
                throw new IllegalArgumentException("mark > position: (" + mark + " > " + pos + ")");
            }
            this.mark = mark;
        }
    }

    static IllegalArgumentException createCapacityException(int capacity) {
        return new IllegalArgumentException("capacity < 0: (" + capacity + " < 0)");
    }

    public static NibbleBuffer allocate(int capacity) {
        if (capacity < 0) {
            throw createCapacityException(capacity);
        }
        byte[] array = new byte[(capacity + 1) << 1];
        return new HeapNibbleBuffer(-1, 0, capacity, capacity, array, 0);
    }

    public static NibbleBuffer wrap(byte[] array, int offset, int length) {
        return new HeapNibbleBuffer(-1, offset, offset + length, array.length << 1, array, 0);
    }

    public static NibbleBuffer wrap(byte[] array) {
        return wrap(array, 0, array.length << 1);
    }

    public final int capacity() {
        return this.capacity;
    }

    public final int position() {
        return this.position;
    }

    public final int limit() {
        return this.limit;
    }

    public final NibbleBuffer position(int newPosition) {
        if (newPosition > this.limit | newPosition < 0) {
            throw this.createPositionException(newPosition);
        }
        if (this.mark > newPosition) {
            this.mark = -1;
        }
        this.position = newPosition;
        return this;
    }

    private IllegalArgumentException createPositionException(int newPosition) {
        String msg = newPosition > this.limit
            ? "newPosition > limit: (" + newPosition + " > " + this.limit + ")"
            : "newPosition < 0: (" + newPosition + " < 0)";
        return new IllegalArgumentException(msg);
    }

    public final NibbleBuffer limit(int newLimit) {
        if (newLimit > this.capacity | newLimit < 0) {
            throw this.createLimitException(newLimit);
        }
        this.limit = newLimit;
        this.position = Math.min(this.position, newLimit);
        if (this.mark > newLimit) {
            this.mark = -1;
        }
        return this;
    }

    private IllegalArgumentException createLimitException(int newLimit) {
        String msg = newLimit > this.capacity
            ? "newLimit > capacity: (" + newLimit + " > " + this.capacity + ")"
            : "newLimit < 0: (" + newLimit + " < 0)";
        return new IllegalArgumentException(msg);
    }

    public final NibbleBuffer mark() {
        this.mark = this.position;
        return this;
    }

    public final NibbleBuffer reset() {
        int m = this.mark;
        if (m < 0) {
            throw new InvalidMarkException();
        }
        this.position = m;
        return this;
    }

    public final NibbleBuffer clear() {
        this.position = 0;
        this.limit = this.capacity;
        this.mark = -1;
        return this;
    }

    public final NibbleBuffer flip() {
        this.limit = this.position;
        this.position = 0;
        this.mark = -1;
        return this;
    }

    public final NibbleBuffer rewind() {
        this.position = 0;
        this.mark = -1;
        return this;
    }

    public final int remaining() {
        int rem = this.limit - this.position;
        return Math.max(rem, 0);
    }

    public final boolean hasRemaining() {
        return this.position < this.limit;
    }

    final int nextGetIndex() {
        int p = this.position;
        if (p >= this.limit) {
            throw new BufferUnderflowException();
        }
        this.position = p + 1;
        return p;
    }

    final int nextPutIndex() {
        int p = this.position;
        if (p >= this.limit) {
            throw new BufferOverflowException();
        }
        this.position = p + 1;
        return p;
    }

    static int checkIndex(int index, int length) {
        if (index >= 0 && index < length) {
            return index;
        }
        throw outOfBoundsCheckIndex(index, length);
    }

    private static IndexOutOfBoundsException outOfBoundsCheckIndex(int index, int length) {
        return new IndexOutOfBoundsException(String.format("Index %s out of bounds for length %s", index, length));
    }

    public byte[] array() {
        return null;
    }

    public abstract NibbleBuffer slice(int index, int length);

    public abstract int get();

    public abstract int get(int index);

    public abstract NibbleBuffer put(int value);

    public abstract NibbleBuffer put(int index, int value);

    public abstract NibbleBuffer put(byte[] nibbles, int offset, int length);

    public abstract NibbleBuffer repeat(int value, int amount);
}
