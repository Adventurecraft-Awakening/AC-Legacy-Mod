package dev.adventurecraft.awakening.common;

import net.minecraft.world.level.LevelSource;

public class AC_BlockTransparent extends AC_BlockSolid {
    
    protected AC_BlockTransparent(int var1, int var2) {
        super(var1, var2);
    }

    @Override
    public boolean isSolidRender() {
        return false;
    }

    @Override
    public boolean shouldRenderFace(LevelSource view, int x, int y, int z, int side) {
        int id = view.getTile(x, y, z);
        return id == this.id ? false : super.shouldRenderFace(view, x, y, z, side);
    }
}
