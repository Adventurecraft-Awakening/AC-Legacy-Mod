package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.entity.projectile.ExArrowEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;

public class AC_EntitySkeletonBoss extends Skeleton {

    public AC_EntitySkeletonBoss(Level var1) {
        super(var1);
        this.setSize(this.bbWidth * 2.5F, this.bbHeight * 2.5F);
        this.runSpeed = 0.25F;
        this.health = 100;
    }

    @Override
    public void baseTick() {
        super.baseTick();
        if (this.onFire > 0 && this.onFire % 20 != 0 && this.onFire % 5 == 0) {
            this.hurt(null, 1);
        }
    }

    @Override
    protected void checkHurtTarget(Entity var1, float var2) {
        if (var2 < 20.0F) {
            double var3 = var1.x - this.x;
            double var5 = var1.z - this.z;
            if (this.attackTime == 0) {
                for (int var7 = 0; var7 < 5; ++var7) {
                    Arrow var8 = new Arrow(this.level, this);
                    ((ExArrowEntity) var8).setAttackStrength(this.damage);
                    var8.y += 1.4F;
                    double var9 = var1.y - (double) 0.2F - var8.y;
                    float var11 = Mth.sqrt(var3 * var3 + var5 * var5) * 0.2F;
                    this.level.playSound(this, "random.bow", 1.0F, 1.0F / (this.random.nextFloat() * 0.4F + 0.8F));
                    this.level.addEntity(var8);
                    var8.shoot(var3, var9 + (double) var11, var5, 0.9F, 36.0F);
                }

                this.attackTime = 30;
            }

            this.yRot = (float) (Math.atan2(var5, var3) * 180.0D / (double) ((float) Math.PI)) - 90.0F;
            if (var2 < 7.5F) {
                this.holdGround = true;
            }
        }

    }

    @Override
    protected int getDeathLoot() {
        return 0;
    }

    @Override
    public boolean hurt(Entity var1, int var2) {
        return var1 == null && super.hurt(var1, var2);
    }
}
