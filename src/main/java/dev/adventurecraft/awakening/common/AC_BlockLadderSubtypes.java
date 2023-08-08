package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.block.ExLadderBlock;
import net.minecraft.block.LadderBlock;
import net.minecraft.world.World;

public class AC_BlockLadderSubtypes extends LadderBlock implements AC_IBlockColor {

    protected AC_BlockLadderSubtypes(int var1, int var2) {
        super(var1, var2);
    }

    @Override
    public int getTextureForSide(int var1, int meta) {
        meta /= 4;
        return this.texture + meta;
    }

    @Override
    public void onBlockPlaced(World world, int x, int y, int z, int side) {
        int coreMeta = world.getBlockMeta(x, y, z);
        int meta = 0;
        if (meta == 0 && ExLadderBlock.isLadderID(world.getBlockId(x, y - 1, z))) {
            meta = world.getBlockMeta(x, y - 1, z) % 4 + 2;
        }

        if (meta == 0 && ExLadderBlock.isLadderID(world.getBlockId(x, y + 1, z))) {
            meta = world.getBlockMeta(x, y + 1, z) % 4 + 2;
        }

        if ((meta == 0 || side == 2) && world.method_1783(x, y, z + 1)) {
            meta = 2;
        }

        if ((meta == 0 || side == 3) && world.method_1783(x, y, z - 1)) {
            meta = 3;
        }

        if ((meta == 0 || side == 4) && world.method_1783(x + 1, y, z)) {
            meta = 4;
        }

        if ((meta == 0 || side == 5) && world.method_1783(x - 1, y, z)) {
            meta = 5;
        }

        coreMeta += Math.max(meta - 2, 0) % 4;
        world.setBlockMeta(x, y, z, coreMeta);
    }

    @Override
    public void onAdjacentBlockUpdate(World world, int x, int y, int z, int id) {
    }

    @Override
    public int getMaxColorMeta() {
        return 4;
    }

    @Override
    public void incrementColor(World world, int x, int y, int z, int amount) {
        int meta = this.getColorMeta(world, x, y, z);
        int maxMeta = this.getMaxColorMeta();
        int clampedMeta = Integer.remainderUnsigned(meta + amount * 4, maxMeta * 4);
        world.setBlockMeta(x, y, z, clampedMeta);
    }
}
