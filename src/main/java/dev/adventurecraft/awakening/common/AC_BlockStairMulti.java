package dev.adventurecraft.awakening.common;

import net.minecraft.block.Block;
import net.minecraft.block.StairsBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class AC_BlockStairMulti extends StairsBlock implements AC_IBlockColor {

    protected AC_BlockStairMulti(int id, Block template, int texture) {
        super(id, template);
        this.texture = texture;
    }

    @Override
    public int getRenderType() {
        return 10;
    }

    @Override
    public boolean isSideRendered(BlockView view, int x, int y, int z, int side) {
        return super.isSideRendered(view, x, y, z, side);
    }

    @Override
    public int getTextureForSide(int var1, int var2) {
        return this.texture + (var2 >> 2);
    }

    @Override
    public int getTextureForSide(int i) {
        return this.texture;
    }

    @Override
    public int getTextureForSide(BlockView arg, int i, int j, int k, int l) {
        return this.getTextureForSide(l, arg.getBlockMeta(i, j, k));
    }

    @Override
    public void afterPlaced(World world, int x, int y, int z, LivingEntity placer) {
        int meta = world.getBlockMeta(x, y, z);
        int direction = MathHelper.floor((double) (placer.yaw * 4.0F / 360.0F) + 0.5D) & 3;
        if (direction == 0) {
            world.setBlockMeta(x, y, z, 2 + meta);
        }

        if (direction == 1) {
            world.setBlockMeta(x, y, z, 1 + meta);
        }

        if (direction == 2) {
            world.setBlockMeta(x, y, z, 3 + meta);
        }

        if (direction == 3) {
            world.setBlockMeta(x, y, z, 0 + meta);
        }
    }

    @Override
    public int getMaxColorMeta() {
        return 16;
    }

    @Override
    public int getColorMultiplier(BlockView view, int x, int y, int z) {
        return 0xFFFFFF;
    }

    @Override
    public int getColorMeta(BlockView view, int x, int y, int z) {
        return view.getBlockMeta(x, y, z) >> 2;
    }

    @Override
    public void setColorMeta(World world, int x, int y, int z, int meta) {
        world.setBlockMeta(x, y, z, world.getBlockMeta(x, y, z) & 3 | meta << 2);
    }
}
