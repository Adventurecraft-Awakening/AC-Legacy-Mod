package dev.adventurecraft.awakening.extension.world.chunk;

import net.minecraft.entity.BlockEntity;

public interface ExChunk {

    boolean setBlockIDWithMetadataTemp(int var1, int var2, int var3, int var4, int var5);

    BlockEntity getChunkBlockTileEntityDontCreate(int var1, int var2, int var3);

    double getTemperatureValue(int var1, int var2);

    void setTemperatureValue(int var1, int var2, double var3);

    long getLastUpdated();

    void setLastUpdated(long value);

    static int translate128(int var0) {
        return var0 > 127 ? -129 + (var0 - 127) : var0;
    }

    static int translate256(int var0) {
        return var0 < 0 ? var0 + 256 : var0;
    }
}
