package dev.adventurecraft.awakening.entity;

import dev.adventurecraft.awakening.extension.entity.ExMob;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class AC_EntitySkeletonSword extends Skeleton {

    public AC_EntitySkeletonSword(Level var1) {
        super(var1);
        this.damage = 1;
        ((ExMob) this).setHeldItem(new ItemInstance(Item.WOOD_SWORD, 1));
    }

    @Override
    protected void checkHurtTarget(Entity var1, float var2) {
        if ((double) var2 < 2.5D && var1.bb.y1 > this.bb.y0 && var1.bb.y0 < this.bb.y1) {
            this.attackTime = 20;
            var1.hurt(this, this.damage);
        }
    }

    @Override
    protected int getDeathLoot() {
        return 0;
    }
}
