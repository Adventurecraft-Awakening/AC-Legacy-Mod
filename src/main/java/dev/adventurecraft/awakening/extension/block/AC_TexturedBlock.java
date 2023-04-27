package dev.adventurecraft.awakening.extension.block;

import net.minecraft.world.BlockView;

public interface AC_TexturedBlock {

    long getTextureForSideEx(BlockView view, int x, int y, int z, int side);
}
