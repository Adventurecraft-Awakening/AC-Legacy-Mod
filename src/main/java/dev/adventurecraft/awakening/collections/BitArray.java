package dev.adventurecraft.awakening.collections;

import java.util.Arrays;

public record BitArray(long[] words) {

    public BitArray(long words) {
        this(new long[Math.toIntExact(Math.addExact(words, 63) / 64)]);
    }

    public boolean get(int bitIndex) {
        int wordIndex = wordIndex(bitIndex);
        return (this.words[wordIndex] & (1L << bitIndex)) != 0L;
    }

    public void set(int bitIndex, boolean value) {
        int wordIndex = wordIndex(bitIndex);
        long mask = 1L << bitIndex;
        long word = this.words[wordIndex];
        this.words[wordIndex] = (word & ~mask) | ((value ? 1L : 0) << bitIndex);
    }

    public void set(int bitIndex) {
        int wordIndex = wordIndex(bitIndex);
        this.words[wordIndex] |= 1L << bitIndex;
    }

    public void clear(int bitIndex) {
        int wordIndex = wordIndex(bitIndex);
        long mask = 1L << bitIndex;
        long word = this.words[wordIndex];
        this.words[wordIndex] = word & ~mask;
    }

    public void clear() {
        Arrays.fill(this.words, 0);
    }

    private static int wordIndex(int bitIndex) {
        return bitIndex >> 6;
    }
}
