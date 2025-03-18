package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.entity.ExLivingEntity;
import dev.adventurecraft.awakening.extension.item.ExItemStack;
import dev.adventurecraft.awakening.item.AC_Items;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;

public class AC_EntityZombiePistol extends Zombie {

    int ammo = 15;

    public AC_EntityZombiePistol(Level var1) {
        super(var1);
        ((ExLivingEntity) this).setHeldItem(new ItemInstance(AC_Items.pistol, 1));
        this.damage = 6;
    }

    @Override
    protected void checkHurtTarget(Entity var1, float var2) {
        if (this.attackTime != 0 || !this.random.nextBoolean() || !this.random.nextBoolean()) {
            return;
        }

        this.setLookAt(var1, 45.0F, 90.0F);
        --this.ammo;
        this.yRot = (float) ((double) this.yRot + 6.0D * this.random.nextGaussian());
        this.xRot = (float) ((double) this.xRot + 2.0D * this.random.nextGaussian());
        this.level.playSound(this, "items.pistol.fire", 1.0F, 1.0F);
        AC_UtilBullet.fireBullet(this.level, this, 0.08F, this.damage);
        this.attackTime = 20;
        if (this.ammo == 0) {
            this.ammo = 15;
            this.attackTime = 50;
        }
    }

    @Override
    public void tick() {
        super.tick();

        var heldItem = (ExItemStack) ((ExLivingEntity) this).getSelectedItem();
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
    protected void dropDeathLoot() {
        int count = this.random.nextInt(3) + 1;

        for (int i = 0; i < count; ++i) {
            ItemEntity itemEntity = this.spawnAtLocation(this.getDeathLoot(), 1);
            itemEntity.item.count = 5;
        }
    }

    @Override
    protected int getDeathLoot() {
        return AC_Items.pistolAmmo.id;
    }
}
