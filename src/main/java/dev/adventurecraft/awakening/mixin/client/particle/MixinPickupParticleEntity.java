package dev.adventurecraft.awakening.mixin.client.particle;

import dev.adventurecraft.awakening.client.render.ParticleRenderContext;
import dev.adventurecraft.awakening.client.render.state.Color4;
import dev.adventurecraft.awakening.client.render.compat.LegacyPassBridge;
import net.minecraft.client.particle.TakeAnimationParticle;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(TakeAnimationParticle.class)
public abstract class MixinPickupParticleEntity extends MixinParticle {
    @Shadow private Entity item;
    @Shadow private Entity target;
    @Shadow private int life;
    @Shadow private int lifeTime;
    @Shadow private float yOffs;

    @Override public void ac$renderLit(ParticleRenderContext context) {
        float tick = context.partialTick(), progress = (this.life + tick) / this.lifeTime;
        progress *= progress;
        double tx = this.target.xOld + (this.target.x-this.target.xOld)*tick;
        double ty = this.target.yOld + (this.target.y-this.target.yOld)*tick + this.yOffs;
        double tz = this.target.zOld + (this.target.z-this.target.zOld)*tick;
        double x = this.item.x + (tx-this.item.x)*progress;
        double y = this.item.y + (ty-this.item.y)*progress;
        double z = this.item.z + (tz-this.item.z)*progress;
        float brightness = this.level.getBrightness(Mth.floor(x),Mth.floor(y+this.heightOffset/2),Mth.floor(z));
        var parameters = context.parameters().withMaterial(dev.adventurecraft.awakening.client.render.state.MaterialRecipe.ENTITY_CUTOUT.material()).withObject(context.parameters().object()
            .withTint(new Color4(brightness,brightness,brightness,1)));
        // Entity dispatch remains the boundary for arbitrary modded pickup renderers.
        LegacyPassBridge.drawLegacy(parameters, null, () -> EntityRenderDispatcher.INSTANCE.render(this.item,
            (float)(x-context.x()),(float)(y-context.y()),(float)(z-context.z()),this.item.yRot,tick));
    }


    @Overwrite
    public int getParticleTexture() {
        return 5;
    }
}
