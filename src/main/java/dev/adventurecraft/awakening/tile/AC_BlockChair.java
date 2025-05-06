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
        return 34;
    }

    @Override
    public void setPlacedBy(Level var1, int var2, int var3, int var4, Mob var5) {
        int var6 = var1.getData(var2, var3, var4);
        int var7 = Mth.floor((double) (var5.yRot * 4.0F / 360.0F) + 0.5D) & 3;
        var1.setData(var2, var3, var4, var6 + (var7 + 1) % 4);
    }
}
