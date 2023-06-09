package dev.adventurecraft.awakening.mixin.entity.animal;

import dev.adventurecraft.awakening.extension.entity.animal.ExWolfEntity;
import net.minecraft.entity.animal.WolfEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(WolfEntity.class)
public abstract class MixinWolfEntity implements ExWolfEntity {

    public int attackStrength = -1;

    @ModifyArg(
        method = "tryAttack",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/Entity;I)Z"),
        index = 1)
    private int modifyAttackStrength(int i) {
        if (this.attackStrength != -1) {
            return this.attackStrength;
        }
        return i;
    }

    @Override
    public int getAttackStrength() {
        return this.attackStrength;
    }

    @Override
    public void setAttackStrength(int value) {
        this.attackStrength = value;
    }
}
