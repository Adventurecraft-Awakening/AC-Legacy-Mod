package dev.adventurecraft.awakening.extension.block;

import net.minecraft.world.level.LevelSource;

public interface AC_TexturedBlock {

    long getTextureForSideEx(LevelSource view, int x, int y, int z, int side);
}
