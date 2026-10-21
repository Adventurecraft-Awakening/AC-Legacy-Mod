package dev.adventurecraft.awakening.collections.bytes;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.RandomAccess;

/** Small primitive-backed list used by the NBT byte-list adapter. */
public final class ByteArrayList extends AbstractList<Byte> implements RandomAccess {
    private byte[] values;
    private int size;

    public ByteArrayList() { this.values = new byte[0]; }

    public ByteArrayList(Collection<? extends Byte> source) {
        this.values = new byte[source.size()];
        for (byte value : source) this.values[this.size++] = value;
    }

    public ByteArrayList(ByteArrayList source) {
        this.values = Arrays.copyOf(source.values, source.size);
        this.size = source.size;
    }

    @Override public int size() { return this.size; }
    public byte getByte(int index) { return this.values[java.util.Objects.checkIndex(index, this.size)]; }
    @Override public Byte get(int index) { return this.getByte(index); }

    @Override public Byte set(int index, Byte value) {
        byte previous = this.getByte(index);
        this.values[index] = value;
        return previous;
    }

    @Override public void add(int index, Byte value) {
        if (index < 0 || index > this.size) throw new IndexOutOfBoundsException(index);
        byte primitive = value;
        if (this.size == this.values.length) this.values = Arrays.copyOf(this.values, Math.max(8, this.size + this.size / 2 + 1));
        System.arraycopy(this.values, index, this.values, index + 1, this.size - index);
        this.values[index] = primitive;
        this.size++;
        this.modCount++;
    }

    public boolean add(byte value) { this.add(this.size, value); return true; }

    @Override public Byte remove(int index) {
        byte previous = this.getByte(index);
        System.arraycopy(this.values, index + 1, this.values, index, this.size - index - 1);
        this.size--;
        this.modCount++;
        return previous;
    }

    @Override public void clear() { if (this.size != 0) { this.size = 0; this.modCount++; } }
}
