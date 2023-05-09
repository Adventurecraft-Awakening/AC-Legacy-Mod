package dev.adventurecraft.awakening.common;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.BlockView;

public class AC_BlockClip extends Block implements AC_ITriggerBlock {

    protected AC_BlockClip(int var1, int var2, Material var3) {
        super(var1, var2, var3);
    }

    public boolean isFullOpaque() {
        return false;
    }

    @Override
    public boolean shouldRender(BlockView view, int x, int y, int z) {
        return AC_DebugMode.active;
    }
}
