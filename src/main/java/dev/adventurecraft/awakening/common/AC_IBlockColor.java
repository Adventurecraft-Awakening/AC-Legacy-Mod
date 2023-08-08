package dev.adventurecraft.awakening.common;

import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public interface AC_IBlockColor {

    int defaultColor = 13421772;

    default int getMaxColorMeta() {
        return 7;
    }

    default int getColorMeta(BlockView view, int x, int y, int z) {
        return view.getBlockMeta(x, y, z);
    }

    default void setColorMeta(World world, int x, int y, int z, int meta) {
        world.setBlockMeta(x, y, z, meta);
    }

    default void incrementColor(World world, int x, int y, int z, int amount) {
        int maxMeta = this.getMaxColorMeta();
        if (maxMeta == 0) {
            return;
        }

        int meta = this.getColorMeta(world, x, y, z);
        int clampedMeta = Integer.remainderUnsigned(meta + amount, maxMeta);
        this.setColorMeta(world, x, y, z, clampedMeta);
    }
}
