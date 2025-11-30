package dev.adventurecraft.awakening.world;

import dev.adventurecraft.awakening.util.NibbleBuffer;
import net.minecraft.world.level.tile.entity.TileEntity;

import java.nio.ByteBuffer;

public interface AC_LevelSource {

    <E extends TileEntity> E ac$tryGetTileEntity(int x, int y, int z, Class<E> type);

    <E extends TileEntity> E ac$getTileEntity(int x, int y, int z, Class<E> type);

    void getTileColumn(ByteBuffer buffer, int x, int y0, int z, int y1);

    void getDataColumn(DataType type, NibbleBuffer buffer, int x, int y0, int z, int y1);

    enum DataType {
        BLOCK_META,
        BLOCK_LIGHT,
        SKY_LIGHT;
    }
}
