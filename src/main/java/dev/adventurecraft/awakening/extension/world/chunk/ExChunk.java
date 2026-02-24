package dev.adventurecraft.awakening.extension.world.chunk;

import dev.adventurecraft.awakening.world.AC_LevelSource;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.world.level.tile.entity.TileEntity;

public interface ExChunk extends AC_LevelSource {

    Int2ObjectMap<TileEntity> ac$tileEntities();

    boolean ac$setTileAndData(int x, int y, int z, int id, int meta, boolean dropItems);

    boolean setBlockIDWithMetadataTemp(int x, int y, int z, int id, int meta);

    float getTemperatureValue(int x, int z);

    void setTemperatureValue(int x, int z, float value);

    long getLastUpdated();

    void setLastUpdated(long value);

    int getLightUpdateHash(int x, int y, int z);

    void updateLightHash();

    int getAcVersion();

    void setAcVersion(int version);

    int ac$tileEntityKey(int x, int y, int z);

    static byte narrowByte(int id) {
        return (byte) (id & 0xff);
    }

    static int widenByte(byte id) {
        return id & 0xff;
    }

    static int ac$tileEntityKey(int x, int y, int z, int cX, int cZ) {
        int bX = (x - (cX << 4)) & 0xF;
        int bZ = (z - (cZ << 4)) & 0xF;
        int bY = y & 0xFF;
        return (bY << 8) | (bZ << 4) | bX;
    }
}
