package dev.adventurecraft.awakening.mixin.client.particle;

import dev.adventurecraft.awakening.client.render.ParticleRenderContext;
import dev.adventurecraft.awakening.client.render.VertexSink;
import net.minecraft.client.particle.ExplosionParticle;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ExplosionParticle.class)
public abstract class MixinExplosionGeometry extends MixinParticle {
    @Override public void ac$render(ParticleRenderContext context,VertexSink out) { super.ac$render(context,out); }
}
