package dev.adventurecraft.awakening.world.region;

import dev.adventurecraft.awakening.extension.world.chunk.ExChunk;
import net.minecraft.world.level.Level;

import java.nio.ByteBuffer;

public sealed class BlockMetaLayer extends BlockIdLayer permits BlockEntityLayer {

    private final byte[] metadata;

    public BlockMetaLayer(int width, int height, int depth) {
        super(width, height, depth);
        this.metadata = new byte[dev.adventurecraft.awakening.world.BlockRegion.calculateVolume(width, height, depth)];
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
        return level.setTileAndDataNoUpdate(x, y, z, id, meta);
    }
}
