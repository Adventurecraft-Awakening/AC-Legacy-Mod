package dev.adventurecraft.awakening.world;

import net.minecraft.nbt.CompoundTag;

public class BlockTileEntityRegion extends BlockRegion {

    public final CompoundTag[] compoundTags;

    public BlockTileEntityRegion(int[] blockIds, int[] metadata, int width, int height, int depth) {
        super(blockIds, metadata, width, height, depth);
        compoundTags = new CompoundTag[getBlockCount()];
    }

    public BlockTileEntityRegion(int width, int height, int depth) {
        super(width, height, depth);
        compoundTags = new CompoundTag[BlockRegion.calculateVolume(width, height, depth)];
    }
}
