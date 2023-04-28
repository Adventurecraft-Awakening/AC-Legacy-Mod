package dev.adventurecraft.awakening.mixin.entity.monster;

import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreeperEntity.class)
public abstract class MixinCreeperEntity extends MonsterEntity {

    public MixinCreeperEntity(World arg) {
        super(arg);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(World var1, CallbackInfo ci) {
        this.attackDamage = 3;
    }

    @ModifyArg(
        method = "tryAttack",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;createExplosion(Lnet/minecraft/entity/Entity;DDDF)Lnet/minecraft/world/explosion/Explosion;"),
        index = 4)
    private float modifyAttackStrength(float i) {
        return i / 3.0F * (float) this.attackDamage;
    }
}
