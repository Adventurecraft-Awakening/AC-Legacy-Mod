package dev.adventurecraft.awakening.world.region;

import dev.adventurecraft.awakening.world.BlockRegion;
import net.minecraft.world.level.Level;

public sealed class BlockIdLayer implements BlockLayer permits BlockMetaLayer {

    private final byte[] blockIds;

    public BlockIdLayer(int width, int height, int depth) {
        this.blockIds = new byte[BlockRegion.calculateVolume(width, height, depth)];
    }

    public final byte getBlock(int index) {
        return this.blockIds[index];
    }

    @Override
    public boolean readBlock(Level level, int index, int x, int y, int z) {
        this.blockIds[index] = (byte) (level.getTile(x, y, z) & 0xFF);
        return true;
    }

    @Override
    public boolean clearBlock(Level level, int index, int x, int y, int z) {
        return level.setTileNoUpdate(x, y, z, 0);
    }

    @Override
    public boolean writeBlock(Level level, int index, int x, int y, int z) {
        return level.setTile(x, y, z, this.getBlock(index));
    }

    @Override
    public boolean updateBlock(Level level, int index, int x, int y, int z) {
        level.tileUpdated(x, y, z, this.getBlock(index));
        return true;
    }
}
