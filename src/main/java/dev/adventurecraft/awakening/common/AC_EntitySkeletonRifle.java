package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.entity.ExLivingEntity;
import dev.adventurecraft.awakening.extension.item.ExItemStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.SkeletonEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class AC_EntitySkeletonRifle extends SkeletonEntity {

    int ammo;

    public AC_EntitySkeletonRifle(World var1) {
        super(var1);
        this.attackDamage = 6;
        this.ammo = 30;
        ((ExLivingEntity) this).setHeldItem(new ItemStack(AC_Items.rifle, 1));
    }

    @Override
    protected void tryAttack(Entity var1, float var2) {
        if (!((double) var2 < 15.0D) || !this.rand.nextBoolean()) {
            return;
        }

        this.lookAt(var1, 30.0F, 30.0F);
        if (this.attackTime == 0) {
            --this.ammo;
            this.lookAt(var1, 60.0F, 90.0F);
            this.yaw = (float) ((double) this.yaw + 10.0D * this.rand.nextGaussian());
            this.pitch = (float) ((double) this.pitch + 3.0D * this.rand.nextGaussian());
            this.world.playSound(this, "items.rifle.fire", 1.0F, 1.0F);
            AC_UtilBullet.fireBullet(this.world, this, 0.07F, this.attackDamage);
            this.attackTime = 5;
            if (this.ammo == 0) {
                this.ammo = 30;
                this.attackTime = 40;
            }
        }

        double var3 = var1.x - this.x;
        double var5 = var1.z - this.z;
        this.yaw = (float) (Math.atan2(var5, var3) * 180.0D / (double) ((float) Math.PI)) - 90.0F;
        this.field_663 = true;
    }

    @Override
    public void tick() {
        super.tick();

        var heldItem = (ExItemStack) ((ExLivingEntity) this).getHeldItem();
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
    protected int getMobDrops() {
        return AC_Items.rifleAmmo.id;
    }
}
