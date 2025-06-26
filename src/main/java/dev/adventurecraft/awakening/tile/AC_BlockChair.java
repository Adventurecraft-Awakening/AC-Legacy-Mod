package dev.adventurecraft.awakening.tile;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

public class AC_BlockChair extends AC_BlockSolid {

    protected AC_BlockChair(int var1, int var2) {
        super(var1, var2);
        this.setShape(2.0F / 16.0F, 0.5F, 2.0F / 16.0F, 14.0F / 16.0F, 10.0F / 16.0F, 14.0F / 16.0F);
    }

    @Override
    public int getTexture(int var1, int var2) {
        var2 /= 4;
        return var1 <= 1 ? this.tex + var2 : this.tex + 16 + var2;
    }

    @Override
    public boolean isSolidRender() {
        return false;
    }

    @Override
    public int getRenderShape() {
        return AC_BlockShapes.CHAIR;
    }

    @Override
    public void setPlacedBy(Level level, int x, int y, int z, Mob entity) {
        int meta = level.getData(x, y, z);
        int direction = Mth.floor((double) (entity.yRot * 4.0F / 360.0F) + 0.5D) & 3;
        level.setData(x, y, z, meta + (direction + 1) % 4);
    }
}
