package dev.adventurecraft.awakening.mixin.client.particle;

import dev.adventurecraft.awakening.client.render.ParticleRenderContext;
import dev.adventurecraft.awakening.client.render.VertexSink;
import net.minecraft.client.particle.FlameParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(FlameParticle.class)
public abstract class MixinHotParticle extends MixinParticle {
    @Shadow private float oSize;
    @Override public void ac$render(ParticleRenderContext context,VertexSink out) {
        float progress=(this.age+context.partialTick())/this.lifetime;
        this.size=this.oSize*(1-progress*progress*.5F);
        super.ac$render(context,out);
    }
}
