package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.block.ExBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.AxixAlignedBoundingBox;
import net.minecraft.world.World;

public class AC_BlockPlant extends Block implements AC_IBlockColor {

    protected AC_BlockPlant(int var1, int var2) {
        super(var1, var2, Material.PLANT);
        float var3 = 0.2F;
        this.setBoundingBox(0.5F - var3, 0.0F, 0.5F - var3, 0.5F + var3, var3 * 3.0F, 0.5F + var3);
    }

    @Override
    public int getTextureForSide(int var1, int var2) {
        return this.texture + var2;
    }

    @Override
    public AxixAlignedBoundingBox getCollisionShape(World var1, int var2, int var3, int var4) {
        return null;
    }

    @Override
    public boolean isFullOpaque() {
        return false;
    }

    @Override
    public boolean isFullCube() {
        return false;
    }

    @Override
    public int getRenderType() {
        return 1;
    }

    @Override
    public void incrementColor(World world, int x, int y, int z) {
        int var5 = world.getBlockMeta(x, y, z);
        world.setBlockMeta(x, y, z, (var5 + 1) % ExBlock.subTypes[this.id]);
    }
}
