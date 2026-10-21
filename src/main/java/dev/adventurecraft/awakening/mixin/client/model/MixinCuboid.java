package dev.adventurecraft.awakening.mixin.client.model;

import dev.adventurecraft.awakening.client.render.NativeRenderer;
import dev.adventurecraft.awakening.client.render.ModelRenderContext;
import dev.adventurecraft.awakening.client.render.state.Matrix4;
import dev.adventurecraft.awakening.client.render.state.Color4;
import dev.adventurecraft.awakening.client.render.compat.LegacyPassBridge;
import dev.adventurecraft.awakening.client.render.vulkan.VulkanRenderer;
import dev.adventurecraft.awakening.client.render.mesh.MemoryTesselator;
import dev.adventurecraft.awakening.client.render.mesh.PoolingBlockAllocator;
import dev.adventurecraft.awakening.extension.client.model.ExCuboid;
import dev.adventurecraft.awakening.extension.client.model.ExTexturedQuad;
import net.minecraft.client.model.Polygon;
import net.minecraft.client.model.Vertex;
import net.minecraft.client.model.geom.ModelPart;
import org.lwjgl.util.vector.Matrix4f;
import java.nio.FloatBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelPart.class)
public abstract class MixinCuboid implements ExCuboid {

    @Shadow
    private Vertex[] vertices;
    @Shadow
    private Polygon[] polygons;
    @Shadow
    private int xTexOffs;
    @Shadow
    private int yTexOffs;

    @Shadow
    public float x;
    @Shadow
    public float y;
    @Shadow
    public float z;

    @Shadow
    public float yRot;
    @Shadow
    public float xRot;
    @Shadow
    public float zRot;

    @Shadow private boolean compiled;
    @Unique private static final PoolingBlockAllocator AC_MODEL_BLOCKS = new PoolingBlockAllocator(4096, 16);
    @Unique private VulkanRenderer.Mesh ac$mesh;
    @Unique private long ac$meshGeneration = -1;
    @Unique private float ac$compiledScale;
    @Unique private boolean ac$hasCompiledScale;
    @Shadow
    public boolean mirror;
    @Shadow
    public boolean visible;
    @Shadow
    public boolean neverRender;

    @Unique
    private int tWidth;
    @Unique
    private int tHeight;


    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(int j, int par2, CallbackInfo ci) {
        this.tWidth = 64;
        this.tHeight = 32;
    }

    @Redirect(
        method = "addBox(FFFIIIF)V",
        at = @At(
            value = "NEW",
            target = "([Lnet/minecraft/client/model/Vertex;IIII)Lnet/minecraft/client/model/Polygon;"))
    private Polygon createQuadsWithSize(Vertex[] var1, int var2, int var3, int var4, int var5) {
        return ExTexturedQuad.create(var1, var2, var3, var4, var5, tWidth, tHeight);
    }

    @Override
    public void addBoxInverted(float offsetX, float offsetY, float offsetZ, int width, int height, int length, float offsetLength) {
        float offsetMaxX = offsetX + (float) width + offsetLength;
        float offsetMaxY = offsetY + (float) height + offsetLength;
        float offsetMaxZ = offsetZ + (float) length + offsetLength;
        offsetX -= offsetLength;
        offsetY -= offsetLength;
        offsetZ -= offsetLength;
        if (this.mirror) {
            float var11 = offsetMaxX;
            offsetMaxX = offsetX;
            offsetX = var11;
        }

        this.vertices = new Vertex[8];
        this.vertices[0] = new Vertex(offsetX, offsetY, offsetZ, 0.0F, 0.0F);
        this.vertices[1] = new Vertex(offsetMaxX, offsetY, offsetZ, 0.0F, 8.0F);
        this.vertices[2] = new Vertex(offsetMaxX, offsetMaxY, offsetZ, 8.0F, 8.0F);
        this.vertices[3] = new Vertex(offsetX, offsetMaxY, offsetZ, 8.0F, 0.0F);
        this.vertices[4] = new Vertex(offsetX, offsetY, offsetMaxZ, 0.0F, 0.0F);
        this.vertices[5] = new Vertex(offsetMaxX, offsetY, offsetMaxZ, 0.0F, 8.0F);
        this.vertices[6] = new Vertex(offsetMaxX, offsetMaxY, offsetMaxZ, 8.0F, 8.0F);
        this.vertices[7] = new Vertex(offsetX, offsetMaxY, offsetMaxZ, 8.0F, 0.0F);

        int w = this.tWidth;
        int h = this.tHeight;

        this.polygons = new Polygon[6];
        this.polygons[0] = ExTexturedQuad.create(new Vertex[]{this.vertices[5], this.vertices[1], this.vertices[2], this.vertices[6]},
            this.xTexOffs + length + width + length,
            this.yTexOffs + length + height, this.xTexOffs + length + width, this.yTexOffs + length, w, h);
        this.polygons[1] = ExTexturedQuad.create(new Vertex[]{this.vertices[0], this.vertices[4], this.vertices[7], this.vertices[3]},
            this.xTexOffs + length, this.yTexOffs + length + height, this.xTexOffs, this.yTexOffs + length, w, h);
        this.polygons[2] = ExTexturedQuad.create(new Vertex[]{this.vertices[0], this.vertices[1], this.vertices[5], this.vertices[4]},
            this.xTexOffs + length + width + width, this.yTexOffs,
            this.xTexOffs + length + width, this.yTexOffs + length, w, h);
        this.polygons[3] = ExTexturedQuad.create(new Vertex[]{this.vertices[3], this.vertices[2], this.vertices[6], this.vertices[7]},
            this.xTexOffs + length + width, this.yTexOffs, this.xTexOffs + length, this.yTexOffs + length, w, h);

        this.polygons[4] = ExTexturedQuad.create(new Vertex[]{this.vertices[1], this.vertices[0], this.vertices[3], this.vertices[2]},
            this.xTexOffs + length + width + length + width,
            this.yTexOffs + length + height, this.xTexOffs + length + width + length, this.yTexOffs + length, w, h);
        this.polygons[5] = ExTexturedQuad.create(new Vertex[]{this.vertices[4], this.vertices[5], this.vertices[6], this.vertices[7]},
            this.xTexOffs + length + width,
            this.yTexOffs + length + height, this.xTexOffs + length, this.yTexOffs + length, w, h);

        if (this.mirror) {
            for (Polygon face : this.polygons) {
                face.mirror();
            }
        }
    }

    @Override
    public void setTWidth(int value) {
        this.tWidth = value;
    }

    @Override
    public void setTHeight(int value) {
        this.tHeight = value;
    }

    public @Override boolean canRender() {
        if (this.neverRender) {
            return false;
        }
        if (!this.visible) {
            return false;
        }
        return true;
    }

    @Overwrite
    private void compile(float scale) {
        this.ac$mesh(scale);
        this.compiled = true;
    }

    @Unique
    private VulkanRenderer.Mesh ac$mesh(float scale) {
        long generation = NativeRenderer.generation();
        if (this.ac$mesh == null || this.ac$meshGeneration != generation) {
            if (!this.ac$hasCompiledScale) {
                this.ac$compiledScale = scale;
                this.ac$hasCompiledScale = true;
            }
            var writer = MemoryTesselator.create();
            writer.setAllocator(AC_MODEL_BLOCKS);
            writer.ac$color8(-1);
            try {
                for (Polygon polygon : this.polygons) {
                    ((ExTexturedQuad)polygon).ac$write(writer, this.ac$compiledScale);
                }
                try (var data = writer.takeMesh()) {
                    this.ac$mesh = NativeRenderer.uploadQuads(data.vertexBlocks, Math.toIntExact(data.getVertexCount()));
                    this.compiled = true;
                    NativeRenderer.track(this, this.ac$mesh);
                    this.ac$meshGeneration = generation;
                }
            } finally {
                writer.clear();
            }
        }
        return this.ac$mesh;
    }

    @Override
    public void render() {
        if (this.canRender()) {
            try (var pass = LegacyPassBridge.modelPass(LegacyPassBridge.modelView(), LegacyPassBridge.currentTint())) {
                pass.draw(this.ac$mesh(1F / 16F), LegacyPassBridge.currentTexture());
            }
        }
    }

    @Override
    public void render(Matrix4f modelView, float[] tint) {
        if (this.canRender()) {
            try (var pass = LegacyPassBridge.scriptModelPass(ac$array(modelView), tint)) {
                pass.draw(this.ac$mesh(1F / 16F), LegacyPassBridge.currentTexture());
            }
        }
    }

    @Override
    public void render(ModelRenderContext context, Matrix4 model, Color4 tint) {
        if (this.canRender()) context.draw(this.ac$mesh(1F / 16F), model, tint);
    }

    @Override
    public Matrix4 pose(Matrix4 parent,float scale,boolean rollable) {
        var pose=parent.translated(this.x*scale,this.y*scale,this.z*scale);
        float degrees=180F/(float)Math.PI;
        if(rollable) return pose.rotated(this.yRot*degrees,0,1,0).rotated(this.xRot*degrees,1,0,0).rotated(this.zRot*degrees,0,0,1);
        return pose.rotated(this.zRot*degrees,0,0,1).rotated(this.yRot*degrees,0,1,0).rotated(this.xRot*degrees,1,0,0);
    }
    @Override
    public void render(ModelRenderContext context,Matrix4 parent,Color4 tint,float scale,boolean rollable) {
        if(this.canRender()) context.draw(this.ac$mesh(scale),this.pose(parent,scale,rollable),tint);
    }

    /** Vanilla model parts now submit retained meshes without modifying global matrices. */
    @Overwrite
    public void render(float scale) {
        this.ac$renderTransformed(scale, false);
    }

    @Overwrite
    public void renderRollable(float scale) {
        this.ac$renderTransformed(scale, true);
    }

    @Unique
    private void ac$renderTransformed(float scale, boolean rollable) {
        if (!this.canRender()) {
            return;
        }
        var model = new Matrix4f().load(FloatBuffer.wrap(LegacyPassBridge.modelView()));
        model.translate(this.x * scale, this.y * scale, this.z * scale);
        if (rollable) {
            model.rotateY(this.yRot);
            model.rotateX(this.xRot);
            model.rotateZ(this.zRot);
        } else {
            model.rotateZ(this.zRot);
            model.rotateY(this.yRot);
            model.rotateX(this.xRot);
        }
        try (var pass = LegacyPassBridge.modelPass(ac$array(model), LegacyPassBridge.currentTint())) {
            pass.draw(this.ac$mesh(scale), LegacyPassBridge.currentTexture());
        }
    }

    @Unique
    private static float[] ac$array(Matrix4f matrix) {
        float[] values = new float[16];
        matrix.store(FloatBuffer.wrap(values));
        return values;
    }

    @Override
    public void translateTo(Matrix4f matrix) {
        matrix.translate(this.x, this.y, this.z);

        if (this.zRot != 0.0F) {
            matrix.rotateZ(this.zRot);
        }
        if (this.yRot != 0.0F) {
            matrix.rotateY(this.yRot);
        }
        if (this.xRot != 0.0F) {
            matrix.rotateX(this.xRot);
        }
    }
}
