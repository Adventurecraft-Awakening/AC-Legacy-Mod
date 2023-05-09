package dev.adventurecraft.awakening.common;

import net.minecraft.world.BlockView;

public class AC_BlockTransparent extends AC_BlockSolid {
    
    protected AC_BlockTransparent(int var1, int var2) {
        super(var1, var2);
    }

    @Override
    public boolean isFullOpaque() {
        return false;
    }

    @Override
    public boolean isSideRendered(BlockView view, int x, int y, int z, int side) {
        int id = view.getBlockId(x, y, z);
        return id == this.id ? false : super.isSideRendered(view, x, y, z, side);
    }
}
