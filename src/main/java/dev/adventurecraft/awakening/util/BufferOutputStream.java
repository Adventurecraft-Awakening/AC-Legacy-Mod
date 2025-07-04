package dev.adventurecraft.awakening.util;

import org.jetbrains.annotations.NotNull;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Buffered output stream with a fixed-size buffer. Writes are tightly packed
 * (performed as multiples of buffer size) when not explicitly flushing.
 */
public final class BufferOutputStream extends FilterOutputStream {

    private final byte[] buffer;
    private int pos;

    public BufferOutputStream(OutputStream out, int size) {
        super(out);
        this.buffer = new byte[size];
    }

    private int available() {
        return this.buffer.length - this.pos;
    }

    private void flushBuffer()
        throws IOException {
        if (this.pos == 0) {
            return;
        }
        this.out.write(this.buffer, 0, this.pos);
        this.pos = 0;
    }

    private void writeToBuffer(byte[] data, int offset, int length) {
        System.arraycopy(data, offset, this.buffer, this.pos, length);
        this.pos += length;
    }

    public @Override void write(int b)
        throws IOException {
        if (this.available() <= 0) {
            this.flushBuffer();
        }
        this.buffer[this.pos] = (byte) (b & 255);
        this.pos += 1;
    }

    public @Override void write(byte @NotNull [] data, int offset, int length)
        throws IOException {
        // Fill buffer as much as possible.
        int copyLen = Math.min(length, this.available());
        if (copyLen > 0) {
            this.writeToBuffer(data, offset, length);
            offset += copyLen;
            length -= copyLen;
        }

        if (length <= 0) {
            return;
        }
        // There is more data; buffer needs to be flushed.
        this.flushBuffer();

        // Write a large aligned slice, possibly leaving a remainder.
        int blocks = length / this.buffer.length;
        if (blocks > 0) {
            int blockLen = blocks * this.buffer.length;
            this.out.write(data, offset, blockLen);
            offset += blockLen;
            length -= blockLen;
        }

        // Store the remainder for later.
        if (length > 0) {
            this.writeToBuffer(data, offset, length);
        }
    }

    public @Override void flush()
        throws IOException {
        this.flushBuffer();
        this.out.flush();
    }

    public @Override void close()
        throws IOException {
        if (this.out == null) {
            return;
        }
        this.flush();
        this.out = null;
    }
}
