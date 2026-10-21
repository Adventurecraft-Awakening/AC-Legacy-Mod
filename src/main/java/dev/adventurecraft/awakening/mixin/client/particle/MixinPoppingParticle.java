package dev.adventurecraft.awakening.mixin.client.particle;

import dev.adventurecraft.awakening.client.render.ParticleRenderContext;
import dev.adventurecraft.awakening.client.render.VertexSink;
import net.minecraft.client.particle.SnowShovelParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SnowShovelParticle.class)
public abstract class MixinPoppingParticle extends MixinParticle {
    @Shadow private float oSize;
    @Override public void ac$render(ParticleRenderContext context,VertexSink out) {
        float progress=(this.age+context.partialTick())/this.lifetime;
        this.size=this.oSize*(Math.clamp(progress*32,0,1));
        super.ac$render(context,out);
    }
}
