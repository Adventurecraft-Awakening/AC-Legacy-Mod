package dev.adventurecraft.awakening.common;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class AC_BlockChair extends AC_BlockSolid {

    protected AC_BlockChair(int var1, int var2) {
        super(var1, var2);
        this.setBoundingBox(2.0F / 16.0F, 0.5F, 2.0F / 16.0F, 14.0F / 16.0F, 10.0F / 16.0F, 14.0F / 16.0F);
    }

    @Override
    public int getTextureForSide(int var1, int var2) {
        var2 /= 4;
        return var1 <= 1 ? this.texture + var2 : this.texture + 16 + var2;
    }

    @Override
    public boolean isFullOpaque() {
        return false;
    }

    @Override
    public int getRenderType() {
        return 34;
    }

    @Override
    public void afterPlaced(World var1, int var2, int var3, int var4, LivingEntity var5) {
        int var6 = var1.getBlockMeta(var2, var3, var4);
        int var7 = MathHelper.floor((double) (var5.yaw * 4.0F / 360.0F) + 0.5D) & 3;
        var1.setBlockMeta(var2, var3, var4, var6 + (var7 + 1) % 4);
    }
}
