package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.entity.ExLivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.SkeletonEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class AC_EntitySkeletonSword extends SkeletonEntity {

    public AC_EntitySkeletonSword(World var1) {
        super(var1);
        this.attackDamage = 1;
        ((ExLivingEntity) this).setHeldItem(new ItemStack(Item.WOOD_SWORD, 1));
    }

    @Override
    protected void tryAttack(Entity var1, float var2) {
        if ((double) var2 < 2.5D && var1.boundingBox.maxY > this.boundingBox.minY && var1.boundingBox.minY < this.boundingBox.maxY) {
            this.attackTime = 20;
            var1.damage(this, this.attackDamage);
        }
    }

    @Override
    protected int getMobDrops() {
        return 0;
    }
}
