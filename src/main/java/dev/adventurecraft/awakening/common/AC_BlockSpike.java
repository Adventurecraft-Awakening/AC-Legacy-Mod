package dev.adventurecraft.awakening.common;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxixAlignedBoundingBox;
import net.minecraft.world.World;

public class AC_BlockSpike extends Block {

    protected AC_BlockSpike(int var1) {
        super(var1, 246, Material.METAL);
    }

    @Override
    public AxixAlignedBoundingBox getCollisionShape(World world, int x, int y, int z) {
        float var5 = 0.25F;
        return AxixAlignedBoundingBox.createAndAddToList((float) x + var5, y, (float) z + var5, (float) (x + 1) - var5, (float) (y + 1) - var5, (float) (z + 1) - var5);
    }

    @Override
    public boolean isFullCube() {
        return false;
    }

    @Override
    public boolean isFullOpaque() {
        return false;
    }

    @Override
    public int getRenderType() {
        return 32;
    }

    @Override
    public void onEntityCollision(World world, int x, int y, int z, Entity entity) {
        entity.damage(null, 10);
    }
}
