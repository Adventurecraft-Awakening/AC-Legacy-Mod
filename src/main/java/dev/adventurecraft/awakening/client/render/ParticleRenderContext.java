package dev.adventurecraft.awakening.client.render;

import dev.adventurecraft.awakening.client.render.state.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

/** One particle extraction view; origin and billboard basis are explicit frame inputs. */
public record ParticleRenderContext(DrawParameters parameters, float partialTick, double x, double y, double z,
                                    float x0, float y0, float z0, float x1, float z1) {
    public static ParticleRenderContext of(Entity camera, float tick) {
        float pitch = (float) Math.toRadians(camera.xRot), yaw = (float) Math.toRadians(camera.yRot);
        float sine = Mth.sin(pitch), x0 = Mth.cos(yaw), z0 = Mth.sin(yaw);
        var parameters = RenderScene.worldParameters();
        return new ParticleRenderContext(parameters.withObject(ObjectData.IDENTITY)
            .withMaterial(MaterialState.UNLIT.withAlpha(CompareOp.GREATER, .1F)), tick,
            RenderScene.cameraX(), RenderScene.cameraY(), RenderScene.cameraZ(), x0, Mth.cos(pitch), z0, -z0 * sine, x0 * sine);
    }
    public void billboard(VertexSink out, float px, float py, float pz, float size,
                          float u0, float v0, float u1, float v1, boolean reverseU) {
        float xa = this.x0 * size, xb = this.x1 * size, ya = this.y0 * size;
        float za = this.z0 * size, zb = this.z1 * size;
        float left = reverseU ? u1 : u0, right = reverseU ? u0 : u1;
        out.ac$vertexUV(px-xa-xb, py-ya, pz-za-zb, left, v1);
        out.ac$vertexUV(px-xa+xb, py+ya, pz-za+zb, left, v0);
        out.ac$vertexUV(px+xa+xb, py+ya, pz+za+zb, right, v0);
        out.ac$vertexUV(px+xa-xb, py-ya, pz+za-zb, right, v1);
    }
}
