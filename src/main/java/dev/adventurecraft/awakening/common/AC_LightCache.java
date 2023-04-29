package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.ACMod;

public class AC_LightCache {
    public static final int cacheSize = 16384;
    public static final int cacheSizeThird = 5461;
    public static final int cacheSizeTwoThird = 10922;
    public AC_CoordBlock[] coords = new AC_CoordBlock[16384];
    public float[] lightValues = new float[16384];
    public static AC_LightCache cache = new AC_LightCache();

    public void clear() {
        for (int var1 = 0; var1 < 16384; ++var1) {
            this.coords[var1] = null;
        }
    }

    private int calcHash(int var1, int var2, int var3) {
        int var4 = 1540483477;
        byte var5 = 24;
        int var6 = 1234567890;
        int var7 = var1 * var4;
        var7 ^= var7 >>> var5;
        var7 *= var4;
        var6 *= var4;
        var6 ^= var7;
        var7 = var2 * var4;
        var7 ^= var7 >>> var5;
        var7 *= var4;
        var6 *= var4;
        var6 ^= var7;
        var7 = var3 * var4;
        var7 ^= var7 >>> var5;
        var7 *= var4;
        var6 *= var4;
        var6 ^= var7;
        var6 ^= var6 >>> 13;
        var6 *= var4;
        var6 ^= var6 >>> 15;
        return var6;
    }

    private int findEntry(int var1, int var2, int var3) {
        int var4 = Math.abs(this.calcHash(var1, var2, var3)) % 16384;
        int var6 = 0;

        while (this.coords[var4] != null && !this.coords[var4].isEqual(var1, var2, var3)) {
            var4 = (var4 + 1) % 16384;
            if (var6++ > 16384) {
                throw new RuntimeException("Light cache full");
            }
        }

        return var4;
    }

    public float getLightValue(int var1, int var2, int var3) {
        int var4 = this.findEntry(var1, var2, var3);
        return this.coords[var4] == null ? -1.0F : this.lightValues[var4];
    }

    public void setLightValue(int var1, int var2, int var3, float var4) {
        int var5 = this.findEntry(var1, var2, var3);
        this.coords[var5] = AC_CoordBlock.getFromPool(var1, var2, var3);
        this.lightValues[var5] = var4;
    }
}
