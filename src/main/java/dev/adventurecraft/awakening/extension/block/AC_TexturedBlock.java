package dev.adventurecraft.awakening.extension.block;

import net.minecraft.world.level.LevelSource;

public interface AC_TexturedBlock {

    long BIOME_BIT = 1L << 32;

    long getTextureForSideEx(LevelSource view, int x, int y, int z, int side);

    static long fromTexture(int texture) {
        return Integer.toUnsignedLong(texture);
    }

    static int toTexture(long key) {
        return (int) (key & 0xFFFF_FFFFL);
    }

    static boolean hasBiomeBit(long key) {
        return (key & BIOME_BIT) == BIOME_BIT;
    }
}
