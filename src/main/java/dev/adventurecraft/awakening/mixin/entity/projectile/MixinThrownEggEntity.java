package dev.adventurecraft.awakening.mixin.entity.projectile;

import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.mixin.entity.MixinEntity;
import net.minecraft.entity.projectile.ThrownEggEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrownEggEntity.class)
public abstract class MixinThrownEggEntity extends MixinEntity {

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        this.collidesWithClipBlocks = false;
    }

    @Redirect(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;method_160(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/hit/HitResult;"))
    private HitResult useRayTrace2(World instance, Vec3d var1, Vec3d var2) {
        return ((ExWorld) this.world).rayTraceBlocks2(var1, var2, false, true, false);
    }
}
