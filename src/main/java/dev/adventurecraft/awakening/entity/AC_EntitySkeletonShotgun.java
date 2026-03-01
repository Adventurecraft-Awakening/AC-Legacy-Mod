package dev.adventurecraft.awakening.entity;

import dev.adventurecraft.awakening.common.AC_UtilBullet;
import dev.adventurecraft.awakening.extension.entity.ExMob;
import dev.adventurecraft.awakening.extension.item.ExItemStack;
import dev.adventurecraft.awakening.item.AC_Items;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.level.Level;

public class AC_EntitySkeletonShotgun extends Skeleton {

    public AC_EntitySkeletonShotgun(Level var1) {
        super(var1);
        this.damage = 2;
        ((ExMob) this).setHeldItem(new ItemInstance(AC_Items.shotgun, 1));
    }

    @Override
    protected void checkHurtTarget(Entity var1, float var2) {
        if (!((double) var2 < 10.0D) || !this.random.nextBoolean()) {
            return;
        }

        this.setLookAt(var1, 30.0F, 30.0F);
        if (this.attackTime == 0) {
            this.setLookAt(var1, 60.0F, 90.0F);
            this.yRot = (float) ((double) this.yRot + 12.0D * this.random.nextGaussian());
            this.xRot = (float) ((double) this.xRot + 6.0D * this.random.nextGaussian());

            for (int var3 = 0; var3 < 8; ++var3) {
                AC_UtilBullet.fireBullet(this.level, this, 0.12F, this.damage);
            }

            this.attackTime = 50;
            this.level.playSound(this, "items.shotgun.fire_and_pump", 1.0F, 1.0F);
        }

        double var7 = var1.x - this.x;
        double var5 = var1.z - this.z;
        this.yRot = (float) (Math.atan2(var5, var7) * 180.0D / (double) ((float) Math.PI)) - 90.0F;
        this.holdGround = true;

    }

    @Override
    public void tick() {
        super.tick();

        var heldItem = (ExItemStack) ((ExMob) this).getSelectedItem();
        if (this.health <= 0) {
            heldItem.setTimeLeft(0);
        } else {
            heldItem.setTimeLeft(this.attackTime);
        }
    }

    @Override
    protected int getDeathLoot() {
        return AC_Items.shotgunAmmo.id;
    }
}
