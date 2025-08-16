package dev.adventurecraft.awakening.util;

import java.nio.*;

public final class BufferUtil {

    public static int bytesPerElement(Buffer buffer) {
        if (buffer instanceof ByteBuffer) {
            return Byte.BYTES;
        } else if (buffer instanceof CharBuffer) {
            return Character.BYTES;
        } else if (buffer instanceof ShortBuffer) {
            return Short.BYTES;
        } else if (buffer instanceof IntBuffer) {
            return Integer.BYTES;
        } else if (buffer instanceof FloatBuffer) {
            return Float.BYTES;
        } else if (buffer instanceof LongBuffer) {
            return Long.BYTES;
        } else if (buffer instanceof DoubleBuffer) {
            return Double.BYTES;
        } else {
            throw new IllegalArgumentException("Unsupported buffer type");
        }
    }

    public static void checkBuffer(int bufferSize, int minimumSize) {
        if (bufferSize < minimumSize) {
            throwIAE(bufferSize, minimumSize);
        }
    }

    public static void repeat(ByteBuffer buffer, byte value, int length) {
        int start = buffer.position();
        int end = start + length;
        if (start < 0 | (end > buffer.limit() | end < 0)) {
            throw new BufferOverflowException();
        } else {
            for (int i = start; i < end; i++) {
                buffer.put(i, value);
            }
            buffer.position(end);
        }
    }

    private static void throwIAE(int bufferSize, int minimumSize) {
        throw new IllegalArgumentException(
            "Number of remaining elements is " + bufferSize + ", must be at least " + minimumSize);
    }
}
