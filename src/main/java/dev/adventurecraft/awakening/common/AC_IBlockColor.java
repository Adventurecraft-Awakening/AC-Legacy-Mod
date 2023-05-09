package dev.adventurecraft.awakening.common;

import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public interface AC_IBlockColor {

    int numColors = 7;
    int defaultColor = 13421772;

    default int getColorMultiplier(BlockView view, int x, int y, int z) {
        int meta = this.getColorMetaData(view, x, y, z);
        if (meta == 1) {
            meta = 16775065;
        } else if (meta == 2) {
            meta = 16767663;
        } else if (meta == 3) {
            meta = 10736540;
        } else if (meta == 4) {
            meta = 9755639;
        } else if (meta == 5) {
            meta = 8880573;
        } else if (meta == 6) {
            meta = 15539236;
        } else {
            meta = defaultColor;
        }
        return meta;
    }

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
