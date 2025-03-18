package dev.adventurecraft.awakening.util;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.random.RandomGenerator;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

/**
 * @see <a href="https://prng.di.unimi.it/xoroshiro128plusplus.c">xoroshiro128plusplus.c</a>
 */
public final class Xoshiro128PP implements RandomGenerator.LeapableGenerator {

    private static final AtomicLong globalSeed = new AtomicLong(RandomUtil.secureNextInt64());

    private long s0;
    private long s1;

    private Xoshiro128PP(long s0, long s1) {
        this.s0 = s0;
        this.s1 = s1;
    }

    public Xoshiro128PP(long seed) {
        this.setSeed(seed);
    }

    public Xoshiro128PP() {
        this(globalSeed.getAndAdd(SplitMix64.PHI));
    }

    public void setSeed(long seed) {
        this.s0 = SplitMix64.next(seed);
        this.s1 = SplitMix64.next(this.s0);
    }

    @Override
    public Xoshiro128PP copy() {
        return new Xoshiro128PP(s0, s1);
    }

    @Override
    public long nextLong() {
        final long s0 = this.s0;
        long s1 = this.s1;
        final long result = Long.rotateLeft(s0 + s1, 17) + s0;

        s1 ^= s0;
        this.s0 = Long.rotateLeft(s0, 49) ^ s1 ^ (s1 << 21); // a, b
        this.s1 = Long.rotateLeft(s1, 28); // c

        return result;
    }

    @Override
    public double jumpDistance() {
        return 0x1.0p64;
    }

    @Override
    public double leapDistance() {
        return 0x1.0p96;
    }

    @Override
    public void jump() {
        jump(0x2bd7a6a6e99c2ddcL, 0x0992ccaf6a6fca05L);
    }

    @Override
    public void leap() {
        jump(0x360fd5f2cf8d5d99L, 0x9c6e6877736c46e3L);
    }

    private void jump(long l0, long l1) {
        long s0 = 0;
        long s1 = 0;

        for (int b = 0; b < 64; b++) {
            if ((l0 & (1L << b)) != 0) {
                s0 ^= this.s0;
                s1 ^= this.s1;
            }
            nextLong();
        }

        for (int b = 0; b < 64; b++) {
            if ((l1 & (1L << b)) != 0) {
                s0 ^= this.s0;
                s1 ^= this.s1;
            }
            nextLong();
        }

        this.s0 = s0;
        this.s1 = s1;
    }

    public static final class RandomWrapper extends Random implements RandomGenerator {
        private final Xoshiro128PP generator;

        public RandomWrapper(Xoshiro128PP randomToWrap) {
            super(0);
            this.generator = randomToWrap;
        }

        @Override
        public void setSeed(long seed) {
            if (this.generator != null) {
                this.generator.setSeed(seed);
            }
        }

        @Override
        public boolean isDeprecated() {
            return generator.isDeprecated();
        }

        @Override
        public void nextBytes(byte[] bytes) {
            this.generator.nextBytes(bytes);
        }

        @Override
        public int nextInt() {
            return this.generator.nextInt();
        }

        @Override
        public int nextInt(int bound) {
            return this.generator.nextInt(bound);
        }

        @Override
        public int nextInt(int origin, int bound) {
            return generator.nextInt(origin, bound);
        }

        @Override
        public long nextLong() {
            return this.generator.nextLong();
        }

        @Override
        public long nextLong(long bound) {
            return generator.nextLong(bound);
        }

        @Override
        public long nextLong(long origin, long bound) {
            return generator.nextLong(origin, bound);
        }

        @Override
        public boolean nextBoolean() {
            return this.generator.nextBoolean();
        }

        @Override
        public float nextFloat() {
            return this.generator.nextFloat();
        }

        @Override
        public float nextFloat(float bound) {
            return generator.nextFloat(bound);
        }

        @Override
        public float nextFloat(float origin, float bound) {
            return generator.nextFloat(origin, bound);
        }

        @Override
        public double nextDouble() {
            return this.generator.nextDouble();
        }

        @Override
        public double nextDouble(double bound) {
            return generator.nextDouble(bound);
        }

        @Override
        public double nextDouble(double origin, double bound) {
            return generator.nextDouble(origin, bound);
        }

        @Override
        public double nextExponential() {
            return generator.nextExponential();
        }

        @Override
        public double nextGaussian() {
            return this.generator.nextGaussian();
        }

        @Override
        public double nextGaussian(double mean, double stddev) {
            return generator.nextGaussian(mean, stddev);
        }

        @Override
        public IntStream ints(long streamSize) {
            return this.generator.ints(streamSize);
        }

        @Override
        public IntStream ints() {
            return this.generator.ints();
        }

        @Override
        public IntStream ints(long streamSize, int randomNumberOrigin, int randomNumberBound) {
            return this.generator.ints(streamSize, randomNumberOrigin, randomNumberBound);
        }

        @Override
        public IntStream ints(int randomNumberOrigin, int randomNumberBound) {
            return this.generator.ints(randomNumberOrigin, randomNumberBound);
        }

        @Override
        public LongStream longs(long streamSize) {
            return this.generator.longs(streamSize);
        }

        @Override
        public LongStream longs() {
            return this.generator.longs();
        }

        @Override
        public LongStream longs(long streamSize, long randomNumberOrigin, long randomNumberBound) {
            return this.generator.longs(streamSize, randomNumberOrigin, randomNumberBound);
        }

        @Override
        public LongStream longs(long randomNumberOrigin, long randomNumberBound) {
            return this.generator.longs(randomNumberOrigin, randomNumberBound);
        }

        @Override
        public DoubleStream doubles(long streamSize) {
            return this.generator.doubles(streamSize);
        }

        @Override
        public DoubleStream doubles() {
            return this.generator.doubles();
        }

        @Override
        public DoubleStream doubles(long streamSize, double randomNumberOrigin, double randomNumberBound) {
            return this.generator.doubles(streamSize, randomNumberOrigin, randomNumberBound);
        }

        @Override
        public DoubleStream doubles(double randomNumberOrigin, double randomNumberBound) {
            return this.generator.doubles(randomNumberOrigin, randomNumberBound);
        }

        @Override
        protected int next(int bits) {
            throw new UnsupportedOperationException();
        }
    }
}
