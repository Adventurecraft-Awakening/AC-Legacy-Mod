package dev.adventurecraft.awakening.common;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.BlockView;

public class AC_BlockColor extends Block implements AC_IBlockColor {

    public AC_BlockColor(int var1, int var2, Material var3) {
        super(var1, var2, var3);
    }

    @Override
    public int getColorMultiplier(BlockView view, int x, int y, int z) {
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
}
