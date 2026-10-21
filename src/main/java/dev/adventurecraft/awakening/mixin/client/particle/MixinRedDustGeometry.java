package dev.adventurecraft.awakening.mixin.client.particle;

import dev.adventurecraft.awakening.client.render.ParticleRenderContext;
import dev.adventurecraft.awakening.client.render.VertexSink;
import net.minecraft.client.particle.RedDustParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(RedDustParticle.class)
public abstract class MixinRedDustGeometry extends MixinParticle {
    @Shadow private float scaleFactor;
    @Override public void ac$render(ParticleRenderContext context,VertexSink out) {
        float progress=(this.age+context.partialTick())/this.lifetime;
        this.size=this.scaleFactor*(Math.clamp(progress*32,0,1));
        super.ac$render(context,out);
    }
}
