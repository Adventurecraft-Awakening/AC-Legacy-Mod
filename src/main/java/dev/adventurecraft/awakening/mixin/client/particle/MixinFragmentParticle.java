package dev.adventurecraft.awakening.mixin.client.particle;

import dev.adventurecraft.awakening.client.render.ParticleRenderContext;
import dev.adventurecraft.awakening.client.render.VertexSink;
import net.minecraft.client.particle.BreakingItemParticle;
import net.minecraft.client.particle.TerrainParticle;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({BreakingItemParticle.class, TerrainParticle.class})
public abstract class MixinFragmentParticle extends MixinParticle {
    @Override public void ac$render(ParticleRenderContext context, VertexSink out) {
        float tick = context.partialTick(), luma = this.getBrightness(tick);
        out.ac$color32(this.rCol * luma, this.gCol * luma, this.bCol * luma);
        float u = ((this.texture % 16) + this.uo / 4) / 16, v = ((this.texture / 16) + this.vo / 4) / 16;
        float x = (float)(this.xo + (this.x-this.xo)*tick-context.x());
        float y = (float)(this.yo + (this.y-this.yo)*tick-context.y());
        float z = (float)(this.zo + (this.z-this.zo)*tick-context.z());
        context.billboard(out, x,y,z,.1F*this.size,u,v,u+.015609375F,v+.015609375F,false);
    }
}
