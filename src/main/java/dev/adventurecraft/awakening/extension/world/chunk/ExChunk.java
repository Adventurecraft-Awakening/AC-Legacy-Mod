package dev.adventurecraft.awakening.extension.world.chunk;

import net.minecraft.entity.BlockEntity;

public interface ExChunk {

    boolean setBlockIDWithMetadataTemp(int x, int y, int z, int id, int meta);

    BlockEntity getChunkBlockTileEntityDontCreate(int x, int y, int z);

    double getTemperatureValue(int x, int z);

    void setTemperatureValue(int x, int z, double value);

    long getLastUpdated();

    void setLastUpdated(long value);

    static int translate128(int id) {
        return id > 127 ? -129 + (id - 127) : id;
    }

    static int translate256(int id) {
        return id < 0 ? id + 256 : id;
    }
}
