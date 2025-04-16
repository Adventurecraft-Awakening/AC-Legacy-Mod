package dev.adventurecraft.awakening.util;

/**
 * @see <a href="https://xoshiro.di.unimi.it/splitmix64.c">splitmix64.c</a>
 */
public final class SplitMix64 {

    public static final long PHI = 0x9e3779b97f4a7c15L;

    private SplitMix64() {
    }

    public static long next(long x) {
        long z = (x + 0x9e3779b97f4a7c15L);
        z = (z ^ (z >>> 30)) * 0xbf58476d1ce4e5b9L;
        z = (z ^ (z >>> 27)) * 0x94d049bb133111ebL;
        return z ^ (z >>> 31);
    }
}
