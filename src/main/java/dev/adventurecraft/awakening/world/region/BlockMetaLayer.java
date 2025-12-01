package dev.adventurecraft.awakening.world.region;

import dev.adventurecraft.awakening.common.Coord;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.util.NibbleBuffer;
import net.minecraft.world.level.Level;

public sealed class BlockMetaLayer extends BlockIdLayer permits BlockEntityLayer {

    private final NibbleBuffer metadata;

    public BlockMetaLayer(Coord size) {
        super(size);
        this.metadata = NibbleBuffer.allocate(size.getVolume());
    }

    public final int getMeta(int index) {
        return this.metadata.get(index);
    }

    public final NibbleBuffer getMetaBuffer() {
        return this.metadata;
    }

    @Override
    public boolean readBlock(Level level, int index, int x, int y, int z) {
        this.metadata.put(index, level.getData(x, y, z));
        return super.readBlock(level, index, x, y, z);
    }

    @Override
    public boolean writeBlock(Level level, int index, int x, int y, int z) {
        int id = this.getBlock(index);
        int meta = this.getMeta(index);
        return ((ExWorld) level).ac$setTileAndDataNoUpdate(x, y, z, id, meta, false);
    }
}
