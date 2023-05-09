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
    public void onBlockPlaced(World world, int x, int y, int z, int side) {
        int var6 = world.getBlockMeta(x, y, z);
        int var7 = 0;
        if (var7 == 0 && ExLadderBlock.isLadderID(world.getBlockId(x, y - 1, z))) {
            var7 = world.getBlockMeta(x, y - 1, z) % 4 + 2;
        }

        if (var7 == 0 && ExLadderBlock.isLadderID(world.getBlockId(x, y + 1, z))) {
            var7 = world.getBlockMeta(x, y + 1, z) % 4 + 2;
        }

        if ((var7 == 0 || side == 2) && world.method_1783(x, y, z + 1)) {
            var7 = 2;
        }

        if ((var7 == 0 || side == 3) && world.method_1783(x, y, z - 1)) {
            var7 = 3;
        }

        if ((var7 == 0 || side == 4) && world.method_1783(x + 1, y, z)) {
            var7 = 4;
        }

        if ((var7 == 0 || side == 5) && world.method_1783(x - 1, y, z)) {
            var7 = 5;
        }

        var6 += Math.max(var7 - 2, 0) % 4;
        world.setBlockMeta(x, y, z, var6);
    }

    @Override
    public void onAdjacentBlockUpdate(World world, int x, int y, int z, int id) {
    }

    @Override
    public void incrementColor(World world, int x, int y, int z) {
        int var5 = world.getBlockMeta(x, y, z);
        world.setBlockMeta(x, y, z, (var5 + 4) % 16);
    }
}
