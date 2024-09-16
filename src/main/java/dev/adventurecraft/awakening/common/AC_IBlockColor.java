package dev.adventurecraft.awakening.common;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;

public interface AC_IBlockColor {

    int defaultColor = 13421772;

    default int getMaxColorMeta() {
        return 7;
    }

    default int getColorMeta(LevelSource view, int x, int y, int z) {
        return view.getData(x, y, z);
    }

    default void setColorMeta(Level world, int x, int y, int z, int meta) {
        world.setData(x, y, z, meta);
    }

    default void incrementColor(Level world, int x, int y, int z, int amount) {
        int maxMeta = this.getMaxColorMeta();
        if (maxMeta == 0) {
            return;
        }

        int meta = this.getColorMeta(world, x, y, z);
        int clampedMeta = Integer.remainderUnsigned(meta + amount, maxMeta);
        this.setColorMeta(world, x, y, z, clampedMeta);
    }
}
