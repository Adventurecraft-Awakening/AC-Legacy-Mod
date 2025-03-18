package dev.adventurecraft.awakening.tile;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class AC_BlockRope extends AC_BlockPlant {

    protected AC_BlockRope(int var1, int var2) {
        super(var1, var2);
        float var3 = 0.2F;
        this.setShape(0.5F - var3, 0.0F, 0.5F - var3, 0.5F + var3, 1.0F, 0.5F + var3);
    }

    @Override
    public AABB getTileAABB(Level world, int x, int y, int z) {
        this.updateBounds(world, x, y, z);
        return super.getTileAABB(world, x, y, z);
    }

    @Override
    public AABB getAABB(Level world, int x, int y, int z) {
        int meta = world.getData(x, y, z) % 3;
        if (meta == 0) {
            return null;
        }

        this.updateBounds(world, x, y, z);
        return AABB.newTemp((double) x + this.xx0, (double) y + this.yy0, (double) z + this.zz0, (double) x + this.xx1, (double) y + this.yy1, (double) z + this.zz1);
    }

    private void updateBounds(Level var1, int var2, int var3, int var4) {
        int var5 = var1.getData(var2, var3, var4) % 3;
        float var6 = 0.2F;
        if (var5 == 0) {
            this.setShape(0.5F - var6, 0.0F, 0.5F - var6, 0.5F + var6, 1.0F, 0.5F + var6);
        } else if (var5 == 1) {
            this.setShape(0.0F, 0.5F - var6, 0.5F - var6, 1.0F, 0.5F + var6, 0.5F + var6);
        } else {
            this.setShape(0.5F - var6, 0.5F - var6, 0.0F, 0.5F + var6, 0.5F + var6, 1.0F);
        }
    }

    @Override
    public int getRenderShape() {
        return 35;
    }

    @Override
    public int getTexture(int var1, int var2) {
        return this.tex + var2 / 3;
    }
}
