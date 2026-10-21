package dev.adventurecraft.awakening.mixin.client.particle;

import dev.adventurecraft.awakening.client.render.ParticleRenderContext;
import dev.adventurecraft.awakening.client.render.VertexSink;
import net.minecraft.client.particle.LavaParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LavaParticle.class)
public abstract class MixinLavaGeometry extends MixinParticle {
    @Shadow private float oSize;
    @Override public void ac$render(ParticleRenderContext context,VertexSink out) {
        float progress=(this.age+context.partialTick())/this.lifetime;
        this.size=this.oSize*(1-progress*progress);
        super.ac$render(context,out);
    }
}
