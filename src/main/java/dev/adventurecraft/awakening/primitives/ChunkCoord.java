package dev.adventurecraft.awakening.primitives;

public final class ChunkCoord {

    public static long pack(int x, int z) {
        return (Integer.toUnsignedLong(z) << 32) | Integer.toUnsignedLong(x);
    }

    public static int unpackX(long key) {
        return (int) key;
    }

    public static int unpackZ(long key) {
        return (int) (key >>> 32);
    }
}
