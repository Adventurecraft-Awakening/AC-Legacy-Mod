package dev.adventurecraft.awakening.mixin.client.particle;

import dev.adventurecraft.awakening.extension.client.render.ExTesselator;
import dev.adventurecraft.awakening.extension.client.particle.ExParticle;
import dev.adventurecraft.awakening.client.render.ParticleRenderContext;
import dev.adventurecraft.awakening.client.render.VertexSink;
import dev.adventurecraft.awakening.mixin.entity.MixinEntity;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.Tesselator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Particle.class)
public abstract class MixinParticle extends MixinEntity implements ExParticle {

    @Shadow public static double zOff;
    @Shadow public static double yOff;
    @Shadow public static double xOff;

    @Shadow protected int texture;
    @Shadow protected float size;
    @Shadow protected int age;
    @Shadow protected int lifetime;
    @Shadow protected float uo;
    @Shadow protected float vo;

    @Shadow protected float rCol;
    @Shadow protected float gCol;
    @Shadow protected float bCol;

    @Redirect(
        method = "<init>(Lnet/minecraft/world/level/Level;DDDDDD)V",
        at = @At(
            value = "INVOKE",
            target = "Ljava/lang/Math;random()D",
            remap = false
        )
    )
    private double useFastRandomInInit() {
        return this.random.nextFloat();
    }

    @Override
    public void ac$render(ParticleRenderContext context, VertexSink out) {
        float tick = context.partialTick();
        float luma = this.getBrightness(tick);
        out.ac$color32(this.rCol * luma, this.gCol * luma, this.bCol * luma);
        float u = (this.texture % 16) / 16F, v = (this.texture / 16) / 16F;
        float px = (float) (this.xo - context.x()) + (float) (this.x - this.xo) * tick;
        float py = (float) (this.yo - context.y()) + (float) (this.y - this.yo) * tick;
        float pz = (float) (this.zo - context.z()) + (float) (this.z - this.zo) * tick;
        context.billboard(out, px, py, pz, .1F * this.size, u, v, u + .0624375F, v + .0624375F, true);
    }

    @Overwrite
    public void render(Tesselator t, float partialTick, float x0, float y0, float z0, float x1, float z1) {
        var et = (ExTesselator) t;

        float luma = this.getBrightness(partialTick);
        t.color(this.rCol * luma, this.gCol * luma, this.bCol * luma);

        float u0 = (float) (this.texture % 16) / 16.0f;
        float v0 = (float) (this.texture / 16) / 16.0f;
        float u1 = u0 + 0.0624375f;
        float v1 = v0 + 0.0624375f;

        float px = (float) (this.xo - xOff) + (float) (this.x - this.xo) * partialTick;
        float py = (float) (this.yo - yOff) + (float) (this.y - this.yo) * partialTick;
        float pz = (float) (this.zo - zOff) + (float) (this.z - this.zo) * partialTick;

        float s = 0.1f * this.size;
        float xA = x0 * s;
        float xB = x1 * s;
        float yA = y0 * s;
        float zA = z0 * s;
        float zB = z1 * s;
        et.ac$vertexUV(px - xA - xB, py - yA, pz - zA - zB, u1, v1);
        et.ac$vertexUV(px - xA + xB, py + yA, pz - zA + zB, u1, v0);
        et.ac$vertexUV(px + xA + xB, py + yA, pz + zA + zB, u0, v0);
        et.ac$vertexUV(px + xA - xB, py - yA, pz + zA - zB, u0, v1);
    }
}
