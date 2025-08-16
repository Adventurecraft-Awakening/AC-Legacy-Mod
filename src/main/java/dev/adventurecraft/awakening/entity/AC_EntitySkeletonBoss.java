package dev.adventurecraft.awakening.entity;

import dev.adventurecraft.awakening.extension.entity.projectile.ExArrowEntity;
import dev.adventurecraft.awakening.util.MathF;
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
    protected void checkHurtTarget(Entity target, float distance) {
        if (distance >= 20.0F) {
            return;
        }

        double dX = target.x - this.x;
        double dZ = target.z - this.z;
        if (this.attackTime == 0) {
            this.playAttackSound();
            this.spawnArrows(target, dX, dZ, 5);
            this.attackTime = 30;
        }

        this.yRot = MathF.toDegrees((float) Math.atan2(dZ, dX)) - 90.0F;
        if (distance < 7.5F) {
            this.holdGround = true;
        }
    }

    private void playAttackSound() {
        float pitch = 1.0F / (this.random.nextFloat() * 0.4F + 0.8F);
        this.level.playSound(this, "random.bow", 1.0F, pitch);
    }

    private void spawnArrows(Entity target, double dX, double dZ, int count) {
        double y1 = Math.sqrt(dX * dX + dZ * dZ) * 0.2;
        for (int i = 0; i < count; ++i) {
            var arrow = new Arrow(this.level, this);
            ((ExArrowEntity) arrow).setAttackStrength(this.damage);
            arrow.y += 1.4F;
            double y0 = target.y - 0.2 - arrow.y;
            this.level.addEntity(arrow);
            arrow.shoot(dX, y0 + y1, dZ, 0.9F, 36.0F);
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
