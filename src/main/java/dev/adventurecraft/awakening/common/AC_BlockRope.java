package dev.adventurecraft.awakening.common;

import net.minecraft.util.math.AxixAlignedBoundingBox;
import net.minecraft.world.World;

public class AC_BlockRope extends AC_BlockPlant {

    protected AC_BlockRope(int var1, int var2) {
        super(var1, var2);
        float var3 = 0.2F;
        this.setBoundingBox(0.5F - var3, 0.0F, 0.5F - var3, 0.5F + var3, 1.0F, 0.5F + var3);
    }

    @Override
    public AxixAlignedBoundingBox getOutlineShape(World world, int x, int y, int z) {
        this.updateBounds(world, x, y, z);
        return super.getOutlineShape(world, x, y, z);
    }

    @Override
    public AxixAlignedBoundingBox getCollisionShape(World world, int x, int y, int z) {
        int meta = world.getBlockMeta(x, y, z) % 3;
        if (meta == 0) {
            return null;
        }

        this.updateBounds(world, x, y, z);
        return AxixAlignedBoundingBox.createAndAddToList((double) x + this.minX, (double) y + this.minY, (double) z + this.minZ, (double) x + this.maxX, (double) y + this.maxY, (double) z + this.maxZ);
    }

    private void updateBounds(World var1, int var2, int var3, int var4) {
        int var5 = var1.getBlockMeta(var2, var3, var4) % 3;
        float var6 = 0.2F;
        if (var5 == 0) {
            this.setBoundingBox(0.5F - var6, 0.0F, 0.5F - var6, 0.5F + var6, 1.0F, 0.5F + var6);
        } else if (var5 == 1) {
            this.setBoundingBox(0.0F, 0.5F - var6, 0.5F - var6, 1.0F, 0.5F + var6, 0.5F + var6);
        } else {
            this.setBoundingBox(0.5F - var6, 0.5F - var6, 0.0F, 0.5F + var6, 0.5F + var6, 1.0F);
        }
    }

    @Override
    public int getRenderType() {
        return 35;
    }

    @Override
    public int getTextureForSide(int var1, int var2) {
        return this.texture + var2 / 3;
    }
}
