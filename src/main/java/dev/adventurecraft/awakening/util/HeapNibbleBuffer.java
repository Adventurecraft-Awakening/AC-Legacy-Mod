package dev.adventurecraft.awakening.util;

import java.nio.BufferOverflowException;
import java.util.Arrays;
import java.util.Objects;

public non-sealed class HeapNibbleBuffer extends NibbleBuffer {

    private final byte[] buffer;
    private final int offset;

    HeapNibbleBuffer(int mark, int pos, int lim, int cap, byte[] buffer, int offset) {
        super(mark, pos, lim, cap);
        this.buffer = buffer;
        this.offset = offset;
    }

    final int ix(int i) {
        return i + this.offset;
    }

    public @Override byte[] array() {
        return this.buffer;
    }

    public @Override NibbleBuffer slice(int index, int length) {
        Objects.checkFromIndexSize(index, length, this.limit());
        return new HeapNibbleBuffer(-1, 0, length, length, this.buffer, index + this.offset);
    }

    public @Override int get() {
        return getNibble(this.buffer, this.ix(this.nextGetIndex()));
    }

    public @Override int get(int index) {
        return getNibble(this.buffer, this.ix(checkIndex(index, this.limit)));
    }

    public @Override NibbleBuffer put(int value) {
        putNibble(this.buffer, this.ix(this.nextPutIndex()), value);
        return this;
    }

    public @Override NibbleBuffer put(int index, int value) {
        putNibble(this.buffer, this.ix(checkIndex(index, this.limit)), value);
        return this;
    }

    public @Override NibbleBuffer put(byte[] nibbles, int offset, int length) {
        int pos = this.position();
        if (length > this.limit() - pos) {
            throw new BufferOverflowException();
        }

        int dstIdx = this.ix(pos);
        if ((dstIdx & 1) == 0 && (offset & 1) == 0) {
            // Offsets are aligned; no need to deal with leading nibble.
            System.arraycopy(nibbles, offset >> 1, this.buffer, dstIdx >> 1, length >> 1);

            // Deal with trailing nibble.
            if ((length & 1) != 0) {
                int trail = getNibble(nibbles, offset + length - 1);
                putNibble(this.buffer, dstIdx + length - 1, trail);
            }
        }
        /* FIXME: fall through to slow copy until proper impl
        else if ((dstIdx & 1) == 1 && (offset & 1) == 1) {
            // Offsets are mis-aligned, but body can still be copied in bulk.

            // TODO: head => bulk-copy aligned body => tail
            throw new NotImplementedException();
        }
        */
        else {
            // Offsets are mis-aligned; the body must be shifted.

            // TODO: load long => shift 4 => store long
            for (int i = 0; i < length; i++) {
                int nibble = getNibble(nibbles, offset + i);
                putNibble(this.buffer, dstIdx + i, nibble);
            }
        }

        this.position(pos + length);
        return this;
    }

    public @Override NibbleBuffer repeat(int value, int amount) {
        int pos = this.position();
        if (amount > this.limit() - pos) {
            throw new BufferOverflowException();
        }

        int idx = this.ix(pos);
        // Deal with leading nibble, which also aligns the write offset.
        if ((idx & 1) != 0) {
            putNibble(this.buffer, idx, value);
            idx += 1;
            amount -= 1;
        }

        // Write middle in bulk since we don't need to merge with destination nibbles.
        // Don't bother dealing with amount<=1 here; it will just fall through.
        int from = idx >> 1;
        int to = from + (amount >> 1);
        byte pair = (byte) (((value << 4) & 0xF0) | (value & 0x0F));
        Arrays.fill(this.buffer, from, to, pair);

        // Deal with trailing nibble.
        if ((amount & 1) != 0) {
            putNibble(this.buffer, idx + amount - 1, value);
        }

        this.position(pos + amount);
        return this;
    }

    private static int getNibble(byte[] buffer, int idx) {
        int octet = buffer[idx >> 1];
        int shift = (idx & 1) << 2;
        return (octet >>> shift) & 0xF;
    }

    private static void putNibble(byte[] buffer, int idx, int value) {
        int i = idx >> 1;
        int octet = buffer[i];
        int shift = (idx & 1) << 2;
        int val = (octet & (0xF << (4 - shift))) | ((value & 0xF) << shift);
        buffer[i] = (byte) val;
    }
}
