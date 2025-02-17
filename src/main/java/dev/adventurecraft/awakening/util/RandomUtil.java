package dev.adventurecraft.awakening.util;

import java.util.Random;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

public final class RandomUtil {

    private static final RandomGeneratorFactory<RandomGenerator.LeapableGenerator> factoryXoshiro128PP =
        RandomGeneratorFactory.of("Xoroshiro128PlusPlus");

    public static Random newXoshiro128PP() {
        return Random.from(factoryXoshiro128PP.create());
    }

    public static Random newXoshiro128PP(long seed) {
        return Random.from(factoryXoshiro128PP.create(seed));
    }
}
