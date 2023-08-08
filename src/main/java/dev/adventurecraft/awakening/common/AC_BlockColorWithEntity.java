package dev.adventurecraft.awakening.common;

import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.material.Material;
import net.minecraft.world.BlockView;

public abstract class AC_BlockColorWithEntity extends BlockWithEntity implements AC_IBlockColor {

    protected AC_BlockColorWithEntity(int i, int j, Material arg) {
        super(i, j, arg);
    }

    @Override
    public int getColorMultiplier(BlockView view, int x, int y, int z) {
        int meta = this.getColorMeta(view, x, y, z);
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
