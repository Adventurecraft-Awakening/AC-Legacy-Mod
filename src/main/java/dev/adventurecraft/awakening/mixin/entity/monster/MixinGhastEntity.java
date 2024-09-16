package dev.adventurecraft.awakening.mixin.entity.monster;

import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.extension.entity.projectile.ExFireballEntity;
import dev.adventurecraft.awakening.mixin.entity.MixinFlyingEntity;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Ghast.class)
public abstract class MixinGhastEntity extends MixinFlyingEntity {

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(Level var1, CallbackInfo ci) {
        this.collidesWithClipBlocks = false;
    }

    @Inject(
        method = "tickHandSwing",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z",
            shift = At.Shift.BEFORE))
    private void modifyAttackStrength(CallbackInfo ci, @Local Fireball fireball) {
        ((ExFireballEntity) fireball).setRadius(this.getAttackStrength());
    }
}
