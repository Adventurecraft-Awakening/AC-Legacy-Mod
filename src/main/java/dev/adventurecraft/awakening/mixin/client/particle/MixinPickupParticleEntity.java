package dev.adventurecraft.awakening.mixin.client.particle;

import net.minecraft.client.particle.TakeAnimationParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(TakeAnimationParticle.class)
public abstract class MixinPickupParticleEntity {

    @Overwrite
    public int getParticleTexture() {
        return 5;
    }
}
