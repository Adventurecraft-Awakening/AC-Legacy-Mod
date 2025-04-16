package dev.adventurecraft.awakening.mixin.entity.monster;

import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Creeper.class)
public abstract class MixinCreeperEntity extends MixinMonsterEntity {

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(Level var1, CallbackInfo ci) {
        this.damage = 3;
    }

    @ModifyArg(
        method = "checkHurtTarget",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;explode(Lnet/minecraft/world/entity/Entity;DDDF)Lnet/minecraft/world/level/Explosion;"),
        index = 4)
    private float modifyAttackStrength(float i) {
        return i / 3.0F * (float) this.damage;
    }
}
