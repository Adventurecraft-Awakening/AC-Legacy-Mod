package dev.adventurecraft.awakening.common;

import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public interface AC_IBlockColor {

    int numColors = 7;
    int defaultColor = 13421772;

    default int getColorMetaData(BlockView view, int x, int y, int z) {
        return view.getBlockMeta(x, y, z);
    }

    default void setColorMetaData(World world, int x, int y, int z, int meta) {
        world.setBlockMeta(x, y, z, meta);
    }

    default void incrementColor(World world, int x, int y, int z) {
        int meta = (this.getColorMetaData(world, x, y, z) + 1) % numColors;
        this.setColorMetaData(world, x, y, z, meta);
    }
}
