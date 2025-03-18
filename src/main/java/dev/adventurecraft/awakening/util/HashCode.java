package dev.adventurecraft.awakening.util;

// xxHash32 is used for the hash code.
// https://github.com/Cyan4973/xxHash

public final class HashCode {

    private static final int Prime2 = 0x85EBCA77;
    private static final int Prime3 = 0xC2B2AE3D;
    private static final int Prime4 = 0x27D4EB2F;
    private static final int Prime5 = 0x165667B1;

    private static final int seed = (int) RandomUtil.secureNextInt64();

    private static int mixEmptyState() {
        return seed + Prime5;
    }

    private static int queueRound(int hash, int queuedValue) {
        return Integer.rotateLeft(hash + queuedValue * Prime3, 17) * Prime4;
    }

    private static int mixFinal(int hash) {
        hash ^= hash >>> 15;
        hash *= Prime2;
        hash ^= hash >>> 13;
        hash *= Prime3;
        hash ^= hash >>> 16;
        return hash;
    }

    public static int combine(int h1, int h2, int h3) {
        int hash = mixEmptyState();
        hash += 12;

        hash = queueRound(hash, h1);
        hash = queueRound(hash, h2);
        hash = queueRound(hash, h3);

        hash = mixFinal(hash);
        return hash;
    }
}
