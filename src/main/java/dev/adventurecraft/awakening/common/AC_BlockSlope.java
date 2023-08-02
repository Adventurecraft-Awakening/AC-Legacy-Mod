package dev.adventurecraft.awakening.common;

import net.minecraft.block.Block;

public class AC_BlockSlope extends AC_BlockStairMulti {

    protected AC_BlockSlope(int id, Block template, int texture) {
        super(id, template, texture);
    }

    @Override
    public int getRenderType() {
        return 38;
    }
}
