package dev.adventurecraft.awakening.mixin.entity.projectile;

import dev.adventurecraft.awakening.extension.entity.projectile.ExFireballEntity;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.mixin.entity.MixinEntity;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Fireball.class)
public abstract class MixinFireballEntity extends MixinEntity implements ExFireballEntity {

    private float radius = 1.0F;

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        this.collidesWithClipBlocks = false;
    }

    @Redirect(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;method_160(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/hit/HitResult;"))
    private HitResult useRayTrace2(Level instance, Vec3 var1, Vec3 var2) {
        return ((ExWorld) this.world).rayTraceBlocks2(var1, var2, false, true, false);
    }

    @ModifyArg(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;createExplosion(Lnet/minecraft/entity/Entity;DDDFZ)Lnet/minecraft/world/explosion/Explosion;"),
        index = 4)
    private float useExplosionRadius(float g) {
        return this.radius;
    }

    @Override
    public void setRadius(float value) {
        this.radius = value;
    }
}
