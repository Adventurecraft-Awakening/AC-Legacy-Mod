package dev.adventurecraft.awakening.tile;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.phys.AABB;

public class AC_BlockSpike extends Tile {

    protected AC_BlockSpike(int var1) {
        super(var1, 246, Material.METAL);
    }

    @Override
    public AABB getAABB(Level world, int x, int y, int z) {
        float var5 = 0.25F;
        return AABB.newTemp((float) x + var5, y, (float) z + var5, (float) (x + 1) - var5, (float) (y + 1) - var5, (float) (z + 1) - var5);
    }

    @Override
    public boolean isCubeShaped() {
        return false;
    }

    @Override
    public boolean isSolidRender() {
        return false;
    }

    @Override
    public int getRenderShape() {
        return 32;
    }

    @Override
    public void entityInside(Level world, int x, int y, int z, Entity entity) {
        entity.hurt(null, 10);
    }
}
