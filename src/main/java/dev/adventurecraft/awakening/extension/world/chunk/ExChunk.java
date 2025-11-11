package dev.adventurecraft.awakening.extension.world.chunk;

import dev.adventurecraft.awakening.world.AC_LevelSource;
import net.minecraft.world.level.tile.entity.TileEntity;

import java.nio.ByteBuffer;

public interface ExChunk extends AC_LevelSource {

    boolean setBlockIDWithMetadataTemp(int x, int y, int z, int id, int meta);

    double getTemperatureValue(int x, int z);

    void setTemperatureValue(int x, int z, double value);

    long getLastUpdated();

    void setLastUpdated(long value);

    int getLightUpdateHash(int x, int y, int z);

    void updateLightHash();

    static int translate128(int id) {
        return id > 127 ? -129 + (id - 127) : id;
    }

    static int translate256(int id) {
        return id < 0 ? id + 256 : id;
    }
}
