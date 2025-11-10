package dev.adventurecraft.awakening.util;

import java.nio.*;

public final class BufferUtil {

    public static int bytesPerElement(Buffer buffer) {
        return switch (buffer) {
            case ByteBuffer ignored -> Byte.BYTES;
            case CharBuffer ignored -> Character.BYTES;
            case ShortBuffer ignored -> Short.BYTES;
            case IntBuffer ignored -> Integer.BYTES;
            case FloatBuffer ignored -> Float.BYTES;
            case LongBuffer ignored -> Long.BYTES;
            case DoubleBuffer ignored -> Double.BYTES;
            case null, default -> throw new IllegalArgumentException("Unsupported buffer type");
        };
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
