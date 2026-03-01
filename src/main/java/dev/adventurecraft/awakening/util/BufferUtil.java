package dev.adventurecraft.awakening.util;

import java.nio.*;

public final class BufferUtil {

    private static final ByteBuffer ZERO_BUFFER = ByteBuffer
        .allocateDirect(256)
        .order(ByteOrder.nativeOrder())
        .asReadOnlyBuffer();

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

    public static void repeatZero(ByteBuffer buffer, int amount) {
        int pos = buffer.position();
        do {
            int n = Math.min(amount, ZERO_BUFFER.limit());
            buffer.put(pos, buffer, 0, n);
            pos += n;
            amount -= n;
        }
        while (amount > 0);
        buffer.position(pos);
    }

    public static void repeat(ByteBuffer buffer, byte value, int amount) {
        int pos = buffer.position();
        if (amount > buffer.limit() - pos) {
            throw new BufferOverflowException();
        }

        if (amount >= 4) {
            int pair = (value << 8) | value;
            int quad = (pair << 16) | pair;
            do {
                buffer.putInt(pos, quad);
                pos += 4;
                amount -= 4;
            }
            while (amount >= 4);
        }
        while (amount > 0) {
            buffer.put(pos, value);
            pos += 1;
            amount -= 1;
        }
        buffer.position(pos);
    }

    private static void throwIAE(int bufferSize, int minimumSize) {
        throw new IllegalArgumentException(
            "Number of remaining elements is " + bufferSize + ", must be at least " + minimumSize);
    }
}
