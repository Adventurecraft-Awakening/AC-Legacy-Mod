package dev.adventurecraft.awakening.world.region;

import dev.adventurecraft.awakening.common.Coord;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.extension.world.chunk.ExChunk;
import net.minecraft.world.level.Level;

import java.nio.ByteBuffer;

public sealed class BlockIdLayer implements BlockLayer permits BlockMetaLayer {

    private final byte[] blockIds;

    public BlockIdLayer(Coord size) {
        this.blockIds = new byte[size.getVolume()];
    }

    public final int getBlock(int index) {
        return ExChunk.widenByte(this.blockIds[index]);
    }

    public final ByteBuffer getBlockBuffer() {
        return ByteBuffer.wrap(this.blockIds);
    }

    @Override
    public boolean readBlock(Level level, int index, int x, int y, int z) {
        this.blockIds[index] = ExChunk.narrowByte(level.getTile(x, y, z));
        return true;
    }

    @Override
    public boolean writeBlock(Level level, int index, int x, int y, int z) {
        return ((ExWorld) level).ac$setTileAndDataNoUpdate(x, y, z, this.getBlock(index), 0, false);
    }

    @Override
    public boolean updateBlock(Level level, int index, int x, int y, int z) {
        // TODO: can we update all neighbors once within region in bulk, instead of 6 times per tile?
        level.tileUpdated(x, y, z, this.getBlock(index));
        return true;
    }
}
