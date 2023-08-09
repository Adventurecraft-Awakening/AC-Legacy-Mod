package dev.adventurecraft.awakening.mixin.entity.monster;

import dev.adventurecraft.awakening.extension.entity.monster.ExMonsterEntity;
import dev.adventurecraft.awakening.mixin.entity.MixinMobEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.MonsterEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MonsterEntity.class)
public abstract class MixinMonsterEntity extends MixinMobEntity implements ExMonsterEntity {

    @Shadow
    protected int attackDamage;

    @Inject(method = "damage", at = @At("HEAD"))
    private void remindOnHit(Entity var1, int var2, CallbackInfoReturnable<Boolean> cir) {
        this.timeBeforeForget = 40;
    }

    @Override
    public boolean attackEntityFromMulti(Entity entity, int damage) {
        this.timeBeforeForget = 40;
        if (super.attackEntityFromMulti(entity, damage)) {
            if (this.passenger != entity && this.vehicle != entity) {
                if (entity != (Object) this) {
                    this.entity = entity;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int getAttackDamage() {
        return this.attackDamage;
    }

    @Override
    public void setAttackDamage(int value) {
        this.attackDamage = value;
    }
}
