package dev.adventurecraft.awakening.mixin.client.model;

import dev.adventurecraft.awakening.extension.client.model.ExCuboid;
import dev.adventurecraft.awakening.extension.client.model.ExTexturedQuad;
import net.minecraft.client.model.Polygon;
import net.minecraft.client.model.Vertex;
import net.minecraft.client.model.geom.ModelPart;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
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

    @Shadow
    private boolean compiled;
    @Shadow
    private int list;
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

    @Shadow
    protected abstract void compile(float scale);

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
        this.polygons[2] = ExTexturedQuad.create(new Vertex[]{this.vertices[5], this.vertices[4], this.vertices[0], this.vertices[1]},
            this.xTexOffs + length + width + width, this.yTexOffs,
            this.xTexOffs + length + width, this.yTexOffs + length, w, h);
        this.polygons[3] = ExTexturedQuad.create(new Vertex[]{this.vertices[2], this.vertices[3], this.vertices[7], this.vertices[6]},
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

    @Override
    public void render() {
        if (!this.canRender()) {
            return;
        }
        if (!this.compiled) {
            this.compile(1F / 16F);
        }
        GL11.glCallList(this.list);
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
