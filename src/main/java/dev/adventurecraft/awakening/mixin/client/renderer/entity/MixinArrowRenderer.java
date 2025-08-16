package dev.adventurecraft.awakening.mixin.client.renderer.entity;

import dev.adventurecraft.awakening.extension.client.render.ExTesselator;
import dev.adventurecraft.awakening.util.MathF;
import dev.adventurecraft.awakening.util.VecUtil;
import net.minecraft.client.renderer.Tesselator;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.Arrow;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

import java.nio.FloatBuffer;

@Mixin(ArrowRenderer.class)
public abstract class MixinArrowRenderer extends EntityRenderer {

    @Unique private static final float[] BODY_VECTORS = new float[4 * 5 * 3];

    @Overwrite
    public void render(Arrow arrow, double x, double y, double z, float yRot, float partialTick) {
        if (arrow.yRotO == 0.0f && arrow.xRotO == 0.0f) {
            return;
        }

        this.bindTexture("/item/arrows.png");
        int n = 0;
        float f2 = 0.0f;
        float f3 = 0.5f;
        float f4 = (float) (0 + n) / 32.0f;
        float f5 = (float) (5 + n) / 32.0f;
        float f6 = 0.0f;
        float f7 = 0.15625f;
        float f8 = (float) (5 + n) / 32.0f;
        float f9 = (float) (10 + n) / 32.0f;
        float f10 = 0.05625f;
        float f11 = (float) arrow.shakeTime - partialTick;

        var mat = new Matrix4f();
        mat.translate((float) x, (float) y, (float) z);
        mat.rotateY(MathF.toRadians(arrow.yRotO + (arrow.yRot - arrow.yRotO) * partialTick - 90.0f));
        mat.rotateZ(MathF.toRadians(arrow.xRotO + (arrow.xRot - arrow.xRotO) * partialTick));
        if (f11 > 0.0f) {
            float f12 = -Mth.sin(f11 * 3.0f) * f11;
            mat.rotateZ(MathF.toRadians(f12));
        }
        mat.rotateX(MathF.toRadians(45.0f));
        mat.scale(f10, f10, f10);
        mat.translate(-4.0f, 0.0f, 0.0f);

        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glPushMatrix();

        try (var stack = MemoryStack.stackPush()) {
            var matBuf = stack.mallocFloat(16);
            mat.store(matBuf);
            GL11.glMultMatrixf(matBuf.flip());
        }

        Tesselator tesselator = Tesselator.instance;
        var ts = (ExTesselator) tesselator;
        tesselator.begin();

        // Render feather.
        ts.ac$normal(-1.0f, 0.0f, 0.0f);
        ts.ac$vertexUV(-7.0f, -2.0f, -2.0f, f6, f8);
        ts.ac$vertexUV(-7.0f, -2.0f, +2.0f, f7, f8);
        ts.ac$vertexUV(-7.0f, +2.0f, +2.0f, f7, f9);
        ts.ac$vertexUV(-7.0f, +2.0f, -2.0f, f6, f9);

        // Render opposite feather.
        ts.ac$normal(1.0f, 0.0f, 0.0f);
        ts.ac$vertexUV(-7.0f, +2.0f, -2.0f, f6, f8);
        ts.ac$vertexUV(-7.0f, +2.0f, +2.0f, f7, f8);
        ts.ac$vertexUV(-7.0f, -2.0f, +2.0f, f7, f9);
        ts.ac$vertexUV(-7.0f, -2.0f, -2.0f, f6, f9);

        // Render body.
        var a = BODY_VECTORS;
        for (int i = 0; i < 4; ++i) {
            int j = i * 15;
            ts.ac$normal(a[j++], a[j++], a[j++]);

            ts.ac$vertexUV(a[j++], a[j++], a[j++], f2, f4);
            ts.ac$vertexUV(a[j++], a[j++], a[j++], f3, f4);
            ts.ac$vertexUV(a[j++], a[j++], a[j++], f3, f5);
            ts.ac$vertexUV(a[j++], a[j++], a[j], f2, f5);
        }

        tesselator.end();

        GL11.glPopMatrix();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
    }

    static {
        var buf = FloatBuffer.wrap(BODY_VECTORS);
        var v = new Vector3f();

        for (int i = 0; i < 4; i++) {
            float sin = new float[] {0, 1, 0, -1}[i];
            float cos = new float[] {1, 0, -1, 0}[i];

            VecUtil.rotateX(sin, cos, 0.0f, 0.0f, 1.0f, v).store(buf);

            VecUtil.rotateX(sin, cos, -8.0f, -2.0f, 0.0f, v).store(buf);
            VecUtil.rotateX(sin, cos, +8.0f, -2.0f, 0.0f, v).store(buf);
            VecUtil.rotateX(sin, cos, +8.0f, +2.0f, 0.0f, v).store(buf);
            VecUtil.rotateX(sin, cos, -8.0f, +2.0f, 0.0f, v).store(buf);
        }
    }
}