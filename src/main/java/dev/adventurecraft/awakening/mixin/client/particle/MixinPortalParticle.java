package dev.adventurecraft.awakening.mixin.client.particle;

import dev.adventurecraft.awakening.client.render.ParticleRenderContext;
import dev.adventurecraft.awakening.client.render.VertexSink;
import net.minecraft.client.particle.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin({PortalParticle.class})
public abstract class MixinPortalParticle extends MixinParticle {
    @Shadow private float scaleFactor;
    @Override public void ac$render(ParticleRenderContext context, VertexSink out) {
        float progress = ((float)this.age + context.partialTick()) / this.lifetime;
        this.size = this.scaleFactor * (1 - (1 - progress) * (1 - progress));
        super.ac$render(context, out);
    }
}
