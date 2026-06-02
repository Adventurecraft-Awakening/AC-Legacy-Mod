package dev.adventurecraft.awakening.util;

import dev.adventurecraft.awakening.world.BlockPos;

import java.io.*;
import java.security.SecureRandom;
import java.util.Random;
import java.util.random.RandomGenerator;

public final class RandomUtil {

    public static Random newXoshiro128PP() {
        return new Xoshiro128PP.RandomWrapper(new Xoshiro128PP());
    }

    public static long secureNextInt64() {
        byte[] bytes = SecureRandom.getSeed(8);
        long s = 0;
        for (byte b : bytes) {
            s = (s << 8) | ((long) b & 0xffL);
        }
        return s;
    }

    public static Random clone(Random random) {
        try {
            var bo = new ByteArrayOutputStream();
            try (var oos = new ObjectOutputStream(bo)) {
                oos.writeObject(random);
            }
            var ois = new ObjectInputStream(new ByteArrayInputStream(bo.toByteArray()));
            return (Random) ois.readObject();
        }
        catch (IOException | ClassNotFoundException ex) {
            throw new AssertionError(null, ex);
        }
    }

    public static RandomGenerator at(long seed, int x, int y, int z) {
        return new Random(VecUtil.getSeed(x, y, z) ^ seed);
    }

    public static RandomGenerator at(long seed, BlockPos pos) {
        return at(seed, pos.x(), pos.y(), pos.z());
    }
}
