package dev.adventurecraft.awakening.mixin.client.particle;

import net.minecraft.client.particle.FootprintParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(FootprintParticle.class)
public abstract class MixinFootstepParticle {

    @Overwrite
    public int getParticleTexture() {
        return 5;
    }
}
