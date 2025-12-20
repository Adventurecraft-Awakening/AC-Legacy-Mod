package dev.adventurecraft.awakening.util;

import java.io.*;
import java.security.SecureRandom;
import java.util.Random;

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
}
