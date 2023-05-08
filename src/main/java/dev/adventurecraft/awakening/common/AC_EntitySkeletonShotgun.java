package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.entity.ExLivingEntity;
import dev.adventurecraft.awakening.extension.item.ExItemStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.SkeletonEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class AC_EntitySkeletonShotgun extends SkeletonEntity {

    public AC_EntitySkeletonShotgun(World var1) {
        super(var1);
        this.attackDamage = 2;
        ((ExLivingEntity) this).setHeldItem(new ItemStack(AC_Items.shotgun, 1));
    }

    @Override
    protected void tryAttack(Entity var1, float var2) {
        if (!((double) var2 < 10.0D) || !this.rand.nextBoolean()) {
            return;
        }

        this.lookAt(var1, 30.0F, 30.0F);
        if (this.attackTime == 0) {
            this.lookAt(var1, 60.0F, 90.0F);
            this.yaw = (float) ((double) this.yaw + 12.0D * this.rand.nextGaussian());
            this.pitch = (float) ((double) this.pitch + 6.0D * this.rand.nextGaussian());

            for (int var3 = 0; var3 < 8; ++var3) {
                AC_UtilBullet.fireBullet(this.world, this, 0.12F, this.attackDamage);
            }

            this.attackTime = 50;
            this.world.playSound(this, "items.shotgun.fire_and_pump", 1.0F, 1.0F);
        }

        double var7 = var1.x - this.x;
        double var5 = var1.z - this.z;
        this.yaw = (float) (Math.atan2(var5, var7) * 180.0D / (double) ((float) Math.PI)) - 90.0F;
        this.field_663 = true;

    }

    @Override
    public void tick() {
        super.tick();

        var heldItem = (ExItemStack) ((ExLivingEntity) this).getHeldItem();
        if (this.health <= 0) {
            heldItem.setTimeLeft(0);
        } else {
            heldItem.setTimeLeft(this.attackTime);
        }
    }

    @Override
    protected int getMobDrops() {
        return AC_Items.shotgunAmmo.id;
    }
}
