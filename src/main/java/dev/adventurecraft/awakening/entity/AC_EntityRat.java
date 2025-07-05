package dev.adventurecraft.awakening.entity;

import dev.adventurecraft.awakening.extension.entity.ExMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class AC_EntityRat extends Monster {

    public AC_EntityRat(Level var1) {
        super(var1);
        this.textureName = "/mob/rat.png";
        this.runSpeed = 0.5F;
        this.damage = 1;
        this.setSize(0.6F, 0.6F);
        this.health = 6;
        ((ExMob) this).setMaxHealth(6);
    }

    @Override
    protected Entity findAttackTarget() {
        Player var1 = this.level.getNearestPlayer(this, 5.0D);
        return var1 != null && this.canSee(var1) ? var1 : null;
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
