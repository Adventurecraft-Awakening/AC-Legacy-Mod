package dev.adventurecraft.awakening.mixin.client.entity.particle;

import net.minecraft.client.entity.particle.PickupParticleEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(PickupParticleEntity.class)
public abstract class MixinPickupParticleEntity {

    @Overwrite
    public int method_2003() {
        return 5;
    }
}
