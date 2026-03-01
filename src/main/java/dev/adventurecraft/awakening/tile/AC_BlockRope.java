package dev.adventurecraft.awakening.tile;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class AC_BlockRope extends AC_BlockPlant {

    private static final float SIZE = 0.2F;

    protected AC_BlockRope(int var1, int var2) {
        super(var1, var2);
        this.setShape(0.5F - SIZE, 0.0F, 0.5F - SIZE, 0.5F + SIZE, 1.0F, 0.5F + SIZE);
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
        return AABB.newTemp(
            x + this.xx0,
            y + this.yy0,
            z + this.zz0,
            x + this.xx1,
            y + this.yy1,
            z + this.zz1
        );
    }

    private void updateBounds(Level level, int x, int y, int z) {
        int meta = level.getData(x, y, z) % 3;
        if (meta == 0) {
            this.setShape(0.5F - SIZE, 0.0F, 0.5F - SIZE, 0.5F + SIZE, 1.0F, 0.5F + SIZE);
        }
        else if (meta == 1) {
            this.setShape(0.0F, 0.5F - SIZE, 0.5F - SIZE, 1.0F, 0.5F + SIZE, 0.5F + SIZE);
        }
        else {
            this.setShape(0.5F - SIZE, 0.5F - SIZE, 0.0F, 0.5F + SIZE, 0.5F + SIZE, 1.0F);
        }
    }

    @Override
    public int getRenderShape() {
        return AC_BlockShapes.ROPE;
    }

    @Override
    public int getTexture(int var1, int var2) {
        return this.tex + var2 / 3;
    }
}
