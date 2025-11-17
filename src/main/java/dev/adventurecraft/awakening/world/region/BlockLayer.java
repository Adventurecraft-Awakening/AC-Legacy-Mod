package dev.adventurecraft.awakening.world.region;

import net.minecraft.world.level.Level;

public interface BlockLayer {

    boolean readBlock(Level level, int index, int x, int y, int z);

    boolean clearBlock(Level level, int index, int x, int y, int z);

    boolean writeBlock(Level level, int index, int x, int y, int z);

    boolean updateBlock(Level level, int index, int x, int y, int z);
}
