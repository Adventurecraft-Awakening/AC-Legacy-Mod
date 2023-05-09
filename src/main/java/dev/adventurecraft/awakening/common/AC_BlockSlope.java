package dev.adventurecraft.awakening.common;

import net.minecraft.block.Block;

public class AC_BlockSlope extends AC_BlockStairMulti {

    protected AC_BlockSlope(int var1, Block var2, int var3) {
        super(var1, var2, var3);
    }

    @Override
    public int getRenderType() {
        return 38;
    }
}
