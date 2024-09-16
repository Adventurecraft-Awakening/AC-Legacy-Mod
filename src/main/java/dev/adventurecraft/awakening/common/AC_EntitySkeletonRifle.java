package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.entity.ExLivingEntity;
import dev.adventurecraft.awakening.extension.item.ExItemStack;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.level.Level;

public class AC_EntitySkeletonRifle extends Skeleton {

    int ammo;

    public AC_EntitySkeletonRifle(Level var1) {
        super(var1);
        this.damage = 6;
        this.ammo = 30;
        ((ExLivingEntity) this).setHeldItem(new ItemInstance(AC_Items.rifle, 1));
    }

    @Override
    protected void checkHurtTarget(Entity var1, float var2) {
        if (!((double) var2 < 15.0D) || !this.random.nextBoolean()) {
            return;
        }

        this.setLookAt(var1, 30.0F, 30.0F);
        if (this.attackTime == 0) {
            --this.ammo;
            this.setLookAt(var1, 60.0F, 90.0F);
            this.yRot = (float) ((double) this.yRot + 10.0D * this.random.nextGaussian());
            this.xRot = (float) ((double) this.xRot + 3.0D * this.random.nextGaussian());
            this.level.playSound(this, "items.rifle.fire", 1.0F, 1.0F);
            AC_UtilBullet.fireBullet(this.level, this, 0.07F, this.damage);
            this.attackTime = 5;
            if (this.ammo == 0) {
                this.ammo = 30;
                this.attackTime = 40;
            }
        }

        double var3 = var1.x - this.x;
        double var5 = var1.z - this.z;
        this.yRot = (float) (Math.atan2(var5, var3) * 180.0D / (double) ((float) Math.PI)) - 90.0F;
        this.holdGround = true;
    }

    @Override
    public void tick() {
        super.tick();

        var heldItem = (ExItemStack) ((ExLivingEntity) this).getSelectedItem();
        if (this.health <= 0) {
            heldItem.setTimeLeft(0);
            return;
        }

        if (this.ammo == 30) {
            heldItem.setTimeLeft(this.attackTime - 35);
            if (heldItem.getTimeLeft() < 0) {
                heldItem.setTimeLeft(0);
            }
        } else {
            heldItem.setTimeLeft(this.attackTime);
        }

        if (heldItem.getTimeLeft() < 0) {
            heldItem.setTimeLeft(0);
        }
    }

    @Override
    protected int getDeathLoot() {
        return AC_Items.rifleAmmo.id;
    }
}
