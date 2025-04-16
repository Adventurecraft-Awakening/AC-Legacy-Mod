package dev.adventurecraft.awakening.mixin.client.particle;

import dev.adventurecraft.awakening.mixin.entity.MixinEntity;
import net.minecraft.client.particle.Particle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Particle.class)
public abstract class MixinParticle extends MixinEntity {

    @Redirect(
        method = "<init>(Lnet/minecraft/world/level/Level;DDDDDD)V",
        at = @At(
            value = "INVOKE",
            target = "Ljava/lang/Math;random()D",
            remap = false
        ))
    private double useFastRandomInInit() {
        return this.random.nextFloat();
    }
}
