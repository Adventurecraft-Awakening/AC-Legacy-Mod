package dev.adventurecraft.awakening.tile;

import net.minecraft.world.level.tile.Tile;

public class AC_BlockSlope extends AC_BlockStairMulti {

    protected AC_BlockSlope(int id, Tile template, int texture) {
        super(id, template, texture);
    }

    @Override
    public int getRenderShape() {
        return AC_BlockShapes.BLOCK_SLOPE;
    }
}
