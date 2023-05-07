package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.entity.projectile.ExArrowEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.SkeletonEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class AC_EntitySkeletonBoss extends SkeletonEntity {

    public AC_EntitySkeletonBoss(World var1) {
        super(var1);
        this.setSize(this.width * 2.5F, this.height * 2.5F);
        this.movementSpeed = 0.25F;
        this.health = 100;
    }

    @Override
    public void baseTick() {
        super.baseTick();
        if (this.fireTicks > 0 && this.fireTicks % 20 != 0 && this.fireTicks % 5 == 0) {
            this.damage(null, 1);
        }
    }

    @Override
    protected void tryAttack(Entity var1, float var2) {
        if (var2 < 20.0F) {
            double var3 = var1.x - this.x;
            double var5 = var1.z - this.z;
            if (this.attackTime == 0) {
                for (int var7 = 0; var7 < 5; ++var7) {
                    ArrowEntity var8 = new ArrowEntity(this.world, this);
                    ((ExArrowEntity) var8).setAttackStrength(this.attackDamage);
                    var8.y += 1.4F;
                    double var9 = var1.y - (double) 0.2F - var8.y;
                    float var11 = MathHelper.sqrt(var3 * var3 + var5 * var5) * 0.2F;
                    this.world.playSound(this, "random.bow", 1.0F, 1.0F / (this.rand.nextFloat() * 0.4F + 0.8F));
                    this.world.spawnEntity(var8);
                    var8.method_1291(var3, var9 + (double) var11, var5, 0.9F, 36.0F);
                }

                this.attackTime = 30;
            }

            this.yaw = (float) (Math.atan2(var5, var3) * 180.0D / (double) ((float) Math.PI)) - 90.0F;
            if (var2 < 7.5F) {
                this.field_663 = true;
            }
        }

    }

    @Override
    protected int getMobDrops() {
        return 0;
    }

    @Override
    public boolean damage(Entity var1, int var2) {
        return var1 == null && super.damage(var1, var2);
    }
}
