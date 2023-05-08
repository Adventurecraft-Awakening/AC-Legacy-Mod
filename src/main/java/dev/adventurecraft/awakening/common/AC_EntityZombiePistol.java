package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.entity.ExLivingEntity;
import dev.adventurecraft.awakening.extension.item.ExItemStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class AC_EntityZombiePistol extends ZombieEntity {

    int ammo = 15;

    public AC_EntityZombiePistol(World var1) {
        super(var1);
        ((ExLivingEntity) this).setHeldItem(new ItemStack(AC_Items.pistol, 1));
        this.attackDamage = 6;
    }

    @Override
    protected void tryAttack(Entity var1, float var2) {
        if (this.attackTime != 0 || !this.rand.nextBoolean() || !this.rand.nextBoolean()) {
            return;
        }

        this.lookAt(var1, 45.0F, 90.0F);
        --this.ammo;
        this.yaw = (float) ((double) this.yaw + 6.0D * this.rand.nextGaussian());
        this.pitch = (float) ((double) this.pitch + 2.0D * this.rand.nextGaussian());
        this.world.playSound(this, "items.pistol.fire", 1.0F, 1.0F);
        AC_UtilBullet.fireBullet(this.world, this, 0.08F, this.attackDamage);
        this.attackTime = 20;
        if (this.ammo == 0) {
            this.ammo = 15;
            this.attackTime = 50;
        }
    }

    @Override
    public void tick() {
        super.tick();

        var heldItem = (ExItemStack) ((ExLivingEntity) this).getHeldItem();
        if (this.health <= 0) {
            heldItem.setTimeLeft(0);
            return;
        }

        if (this.ammo == 15) {
            heldItem.setTimeLeft(this.attackTime - 43);
        } else {
            heldItem.setTimeLeft(this.attackTime - 13);
        }

        if (heldItem.getTimeLeft() < 0) {
            heldItem.setTimeLeft(0);
        }
    }

    @Override
    protected void getDrops() {
        int count = this.rand.nextInt(3) + 1;

        for (int i = 0; i < count; ++i) {
            ItemEntity itemEntity = this.dropItem(this.getMobDrops(), 1);
            itemEntity.stack.count = 5;
        }
    }

    @Override
    protected int getMobDrops() {
        return AC_Items.pistolAmmo.id;
    }
}
