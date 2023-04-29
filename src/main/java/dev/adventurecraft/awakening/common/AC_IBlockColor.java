package dev.adventurecraft.awakening.common;

import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public interface AC_IBlockColor {

    int numColors = 7;
    int defaultColor = 13421772;

    default int getDefaultColor() {
        return defaultColor;
    }

    default int getColorMultiplier(BlockView var1, int var2, int var3, int var4) {
        int var5 = this.getColorMetaData(var1, var2, var3, var4);
        if (var5 == 1) {
            var5 = 16775065;
        } else if (var5 == 2) {
            var5 = 16767663;
        } else if (var5 == 3) {
            var5 = 10736540;
        } else if (var5 == 4) {
            var5 = 9755639;
        } else if (var5 == 5) {
            var5 = 8880573;
        } else if (var5 == 6) {
            var5 = 15539236;
        } else {
            var5 = this.getDefaultColor();
        }
        return var5;
    }

    default int getColorMetaData(BlockView var1, int var2, int var3, int var4) {
        return var1.getBlockMeta(var2, var3, var4);
    }

    default void setColorMetaData(World var1, int var2, int var3, int var4, int var5) {
        var1.setBlockMeta(var2, var3, var4, var5);
    }

    default void incrementColor(World var1, int var2, int var3, int var4) {
        int var5 = (this.getColorMetaData(var1, var2, var3, var4) + 1) % numColors;
        this.setColorMetaData(var1, var2, var3, var4, var5);
    }
}
