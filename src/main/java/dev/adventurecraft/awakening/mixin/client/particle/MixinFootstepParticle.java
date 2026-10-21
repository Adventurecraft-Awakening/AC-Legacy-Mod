package dev.adventurecraft.awakening.mixin.client.particle;

import dev.adventurecraft.awakening.client.render.*;
import dev.adventurecraft.awakening.client.render.state.*;
import dev.adventurecraft.awakening.extension.client.ExTextureManager;
import net.minecraft.client.particle.FootprintParticle;
import net.minecraft.client.renderer.Textures;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(FootprintParticle.class)
public abstract class MixinFootstepParticle extends MixinParticle {
    @Shadow private int ticks;
    @Shadow private int maxAge;
    @Shadow private Textures textureManager;

    @Override public void ac$renderLit(ParticleRenderContext context) {
        float life = (this.ticks + context.partialTick()) / this.maxAge;
        float alpha = Math.min(1, 2 - life * life * 2) * .2F;
        float brightness = this.level.getBrightness(Mth.floor(this.x), Mth.floor(this.y), Mth.floor(this.z));
        float x = (float)(this.x-context.x()), y = (float)(this.y-context.y()), z = (float)(this.z-context.z());
        var out = QuadBuilder.frameOwned(4);
        out.color(brightness, brightness, brightness, alpha);
        out.vertexUV(x-.125F,y,z+.125F,0,1); out.vertexUV(x+.125F,y,z+.125F,1,1);
        out.vertexUV(x+.125F,y,z-.125F,1,0); out.vertexUV(x-.125F,y,z-.125F,0,0);
        var parameters = context.parameters().withRaster(context.parameters().raster().withBlend(BlendState.ALPHA));
        try(var pass=NativeRenderer.pass(parameters)) { pass.drawQuads(out, ((ExTextureManager)this.textureManager).texture("/misc/footprint.png")); }
    }


    @Overwrite
    public int getParticleTexture() {
        return 5;
    }
}
