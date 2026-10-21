package dev.adventurecraft.awakening.mixin.client.particle;

import dev.adventurecraft.awakening.client.render.ParticleRenderContext;
import dev.adventurecraft.awakening.client.render.VertexSink;
import net.minecraft.client.particle.WaterDropParticle;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(WaterDropParticle.class)
public abstract class MixinWaterDropGeometry extends MixinParticle {
    @Override public void ac$render(ParticleRenderContext context,VertexSink out) { super.ac$render(context,out); }
}
