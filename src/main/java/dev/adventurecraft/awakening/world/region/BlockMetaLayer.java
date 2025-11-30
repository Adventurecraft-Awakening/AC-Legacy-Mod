package dev.adventurecraft.awakening.world.region;

import dev.adventurecraft.awakening.common.Coord;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.extension.world.chunk.ExChunk;
import net.minecraft.world.level.Level;

import java.nio.ByteBuffer;

public sealed class BlockMetaLayer extends BlockIdLayer permits BlockEntityLayer {

    private final byte[] metadata;

    public BlockMetaLayer(Coord size) {
        super(size);
        this.metadata = new byte[size.getVolume()];
    }

    public final int getMeta(int index) {
        return ExChunk.widenByte(this.metadata[index]);
    }

    public final ByteBuffer getMetaBuffer() {
        return ByteBuffer.wrap(this.metadata);
    }

    @Override
    public boolean readBlock(Level level, int index, int x, int y, int z) {
        // TODO: 4-bit nibbles
        this.metadata[index] = (byte) (level.getData(x, y, z) & 0xf);
        return super.readBlock(level, index, x, y, z);
    }

    @Override
    public boolean writeBlock(Level level, int index, int x, int y, int z) {
        int id = this.getBlock(index);
        int meta = this.getMeta(index);
        return ((ExWorld) level).ac$setTileAndDataNoUpdate(x, y, z, id, meta, false);
    }
}
