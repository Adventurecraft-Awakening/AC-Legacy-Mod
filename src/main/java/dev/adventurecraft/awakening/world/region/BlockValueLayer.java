package dev.adventurecraft.awakening.world.region;

import net.minecraft.world.level.Level;

public final class BlockValueLayer implements BlockLayer {

    private final byte id;
    private final byte meta;

    public BlockValueLayer(int id, int meta) {
        this.id = (byte) (id & 0xff);
        this.meta = (byte) (meta & 0xff);
    }

    @Override
    public boolean readBlock(Level level, int index, int x, int y, int z) {
        return false;
    }

    @Override
    public boolean writeBlock(Level level, int index, int x, int y, int z) {
        return level.setTileAndDataNoUpdate(x, y, z, this.id, this.meta);
    }

    @Override
    public boolean updateBlock(Level level, int index, int x, int y, int z) {
        level.tileUpdated(x, y, z, 0);
        return true;
    }
}
