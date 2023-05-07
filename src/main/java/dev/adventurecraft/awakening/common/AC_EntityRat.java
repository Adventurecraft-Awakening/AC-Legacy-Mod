package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.entity.ExLivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public class AC_EntityRat extends MonsterEntity {

    public AC_EntityRat(World var1) {
        super(var1);
        this.texture = "/mob/rat.png";
        this.movementSpeed = 0.5F;
        this.attackDamage = 1;
        this.setSize(0.6F, 0.6F);
        this.health = 6;
        ((ExLivingEntity) this).setMaxHealth(6);
    }

    @Override
    protected Entity getAttackTarget() {
        PlayerEntity var1 = this.world.getClosestPlayerTo(this, 5.0D);
        return var1 != null && this.method_928(var1) ? var1 : null;
    }

    @Override
    protected String getAmbientSound() {
        return "mob.rat.ambient";
    }

    @Override
    protected String getHurtSound() {
        return "mob.rat.hurt";
    }

    @Override
    protected String getDeathSound() {
        return "mob.rat.death";
    }
}
