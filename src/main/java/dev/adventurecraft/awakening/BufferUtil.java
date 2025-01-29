package dev.adventurecraft.awakening;

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

    private static void throwIAE(int bufferSize, int minimumSize) {
        throw new IllegalArgumentException(
            "Number of remaining elements is " + bufferSize + ", must be at least " + minimumSize);
    }
}
