package dev.adventurecraft.awakening.mixin.client.particle;

import net.minecraft.client.particle.RedDustParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.random.RandomGenerator;

@Mixin(RedDustParticle.class)
public abstract class MixinRedDustParticle {

    @Unique
    private static final RandomGenerator random32 = RandomGenerator.of("Xoroshiro128PlusPlus");

    @Redirect(
        method = "<init>(Lnet/minecraft/world/level/Level;DDDFFFF)V",
        at = @At(
            value = "INVOKE",
            target = "Ljava/lang/Math;random()D",
            remap = false
        ))
    private double useFastRandomInInit() {
        return random32.nextFloat();
    }
}
