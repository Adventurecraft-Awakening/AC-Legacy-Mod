package dev.adventurecraft.awakening.mixin.entity.monster;

import dev.adventurecraft.awakening.mixin.entity.MixinMobEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.MonsterEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MonsterEntity.class)
public abstract class MixinMonsterEntity extends MixinMobEntity {

    @Inject(method = "damage", at = @At("HEAD"))
    private void remindOnHit(Entity var1, int var2, CallbackInfoReturnable<Boolean> cir) {
        this.timeBeforeForget = 40;
    }

    @Override
    public boolean attackEntityFromMulti(Entity var1, int var2) {
        this.timeBeforeForget = 40;
        if (super.attackEntityFromMulti(var1, var2)) {
            if (this.passenger != var1 && this.vehicle != var1) {
                if (var1 != (Object) this) {
                    this.entity = var1;
                }
            }
            return true;
        } else {
            return false;
        }
    }
}
