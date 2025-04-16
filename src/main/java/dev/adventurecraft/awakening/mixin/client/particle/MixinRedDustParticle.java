package dev.adventurecraft.awakening.mixin.client.particle;

import net.minecraft.client.particle.RedDustParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RedDustParticle.class)
public abstract class MixinRedDustParticle extends MixinParticle {

    @Redirect(
        method = "<init>(Lnet/minecraft/world/level/Level;DDDFFFF)V",
        at = @At(
            value = "INVOKE",
            target = "Ljava/lang/Math;random()D",
            remap = false
        ))
    private double useFastRandomInInit() {
        return this.random.nextFloat();
    }
}
