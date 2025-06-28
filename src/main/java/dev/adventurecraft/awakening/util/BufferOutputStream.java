package dev.adventurecraft.awakening.util;

import org.jetbrains.annotations.NotNull;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public final class BufferOutputStream extends FilterOutputStream {

    private byte[] buffer;
    private int pos;

    public BufferOutputStream(OutputStream out, int size) {
        super(out);
        this.buffer = new byte[size];
    }

    private int avail() {
        return this.buffer.length - this.pos;
    }

    private void dumpBuffer(boolean ifFull)
        throws IOException {
        if (pos == 0) {
            return;
        }
        if (!ifFull || this.avail() == 0) {
            out.write(buffer, 0, pos);
            pos = 0;
        }
    }

    public @Override void write(final int b)
        throws IOException {
        buffer[pos++] = (byte) b;
        dumpBuffer(true);
    }

    public @Override void write(byte @NotNull [] b, int offset, int length)
        throws IOException {
        if (length >= buffer.length) {
            dumpBuffer(false);
            out.write(b, offset, length);
            return;
        }

        if (length <= this.avail()) {
            System.arraycopy(b, offset, buffer, pos, length);
            pos += length;
            dumpBuffer(true);
            return;
        }

        dumpBuffer(false);
        System.arraycopy(b, offset, buffer, 0, length);
        pos = length;
    }

    public @Override void flush()
        throws IOException {
        dumpBuffer(false);
        out.flush();
    }

    public @Override void close()
        throws IOException {
        if (out == null) {
            return;
        }
        flush();
        out = null;
        buffer = null;
    }
}
