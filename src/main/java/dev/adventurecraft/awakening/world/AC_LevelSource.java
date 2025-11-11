package dev.adventurecraft.awakening.world;

import net.minecraft.world.level.tile.entity.TileEntity;

import java.nio.ByteBuffer;

public interface AC_LevelSource {

    <E extends TileEntity> E ac$tryGetTileEntity(int x, int y, int z, Class<E> type);

    <E extends TileEntity> E ac$getTileEntity(int x, int y, int z, Class<E> type);

    void getTileColumn(ByteBuffer buffer, int x, int y0, int z, int y1);
}
