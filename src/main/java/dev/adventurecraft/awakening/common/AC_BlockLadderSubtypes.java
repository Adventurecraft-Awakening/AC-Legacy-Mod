package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.block.ExLadderBlock;
import net.minecraft.block.LadderBlock;
import net.minecraft.world.World;

public class AC_BlockLadderSubtypes extends LadderBlock implements AC_IBlockColor {
    protected AC_BlockLadderSubtypes(int var1, int var2) {
        super(var1, var2);
    }

    @Override
    public int getTextureForSide(int var1, int var2) {
        var2 /= 4;
        return this.texture + var2;
    }

    @Override
    public void onBlockPlaced(World var1, int var2, int var3, int var4, int var5) {
        int var6 = var1.getBlockMeta(var2, var3, var4);
        int var7 = 0;
        if (var7 == 0 && ExLadderBlock.isLadderID(var1.getBlockId(var2, var3 - 1, var4))) {
            var7 = var1.getBlockMeta(var2, var3 - 1, var4) % 4 + 2;
        }

        if (var7 == 0 && ExLadderBlock.isLadderID(var1.getBlockId(var2, var3 + 1, var4))) {
            var7 = var1.getBlockMeta(var2, var3 + 1, var4) % 4 + 2;
        }

        if ((var7 == 0 || var5 == 2) && var1.method_1783(var2, var3, var4 + 1)) {
            var7 = 2;
        }

        if ((var7 == 0 || var5 == 3) && var1.method_1783(var2, var3, var4 - 1)) {
            var7 = 3;
        }

        if ((var7 == 0 || var5 == 4) && var1.method_1783(var2 + 1, var3, var4)) {
            var7 = 4;
        }

        if ((var7 == 0 || var5 == 5) && var1.method_1783(var2 - 1, var3, var4)) {
            var7 = 5;
        }

        var6 += Math.max(var7 - 2, 0) % 4;
        var1.setBlockMeta(var2, var3, var4, var6);
    }

    @Override
    public void onAdjacentBlockUpdate(World var1, int var2, int var3, int var4, int var5) {
    }

    @Override
    public void incrementColor(World var1, int var2, int var3, int var4) {
        int var5 = var1.getBlockMeta(var2, var3, var4);
        var1.setBlockMeta(var2, var3, var4, (var5 + 4) % 16);
    }
}
