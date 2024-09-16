package dev.adventurecraft.awakening.mixin.entity.projectile;

import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.mixin.entity.MixinEntity;
import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrownEgg.class)
public abstract class MixinThrownEggEntity extends MixinEntity {

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        this.collidesWithClipBlocks = false;
    }

    @Redirect(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;clip(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/HitResult;"))
    private HitResult useRayTrace2(Level instance, Vec3 var1, Vec3 var2) {
        return ((ExWorld) this.level).rayTraceBlocks2(var1, var2, false, true, false);
    }
}
