package dev.adventurecraft.awakening.mixin.client.model;

import dev.adventurecraft.awakening.extension.client.model.ExCuboid;
import dev.adventurecraft.awakening.extension.client.model.ExTexturedQuad;
import net.minecraft.client.model.Cuboid;
import net.minecraft.client.render.QuadPoint;
import net.minecraft.client.render.TexturedQuad;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Cuboid.class)
public abstract class MixinCuboid implements ExCuboid {

    @Shadow
    private QuadPoint[] corners;
    @Shadow
    private TexturedQuad[] faces;
    @Shadow
    private int textureOffsetX;
    @Shadow
    private int textureOffsetY;
    @Shadow
    public boolean mirror;

    private int tWidth;
    private int tHeight;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(int j, int par2, CallbackInfo ci) {
        this.tWidth = 64;
        this.tHeight = 32;
    }

    @Redirect(
        method = "method_1818",
        at = @At(
            value = "NEW",
            target = "([Lnet/minecraft/client/render/QuadPoint;IIII)Lnet/minecraft/client/render/TexturedQuad;"))
    private TexturedQuad createQuadsWithSize(QuadPoint[] var1, int var2, int var3, int var4, int var5) {
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

        this.corners = new QuadPoint[8];
        this.corners[0] = new QuadPoint(offsetX, offsetY, offsetZ, 0.0F, 0.0F);
        this.corners[1] = new QuadPoint(offsetMaxX, offsetY, offsetZ, 0.0F, 8.0F);
        this.corners[2] = new QuadPoint(offsetMaxX, offsetMaxY, offsetZ, 8.0F, 8.0F);
        this.corners[3] = new QuadPoint(offsetX, offsetMaxY, offsetZ, 8.0F, 0.0F);
        this.corners[4] = new QuadPoint(offsetX, offsetY, offsetMaxZ, 0.0F, 0.0F);
        this.corners[5] = new QuadPoint(offsetMaxX, offsetY, offsetMaxZ, 0.0F, 8.0F);
        this.corners[6] = new QuadPoint(offsetMaxX, offsetMaxY, offsetMaxZ, 8.0F, 8.0F);
        this.corners[7] = new QuadPoint(offsetX, offsetMaxY, offsetMaxZ, 8.0F, 0.0F);

        int w = this.tWidth;
        int h = this.tHeight;

        this.faces = new TexturedQuad[6];
        this.faces[0] = ExTexturedQuad.create(new QuadPoint[]{this.corners[5], this.corners[1], this.corners[2], this.corners[6]}, this.textureOffsetX + length + width + length, this.textureOffsetY + length + height, this.textureOffsetX + length + width, this.textureOffsetY + length, w, h);
        this.faces[1] = ExTexturedQuad.create(new QuadPoint[]{this.corners[0], this.corners[4], this.corners[7], this.corners[3]}, this.textureOffsetX + length, this.textureOffsetY + length + height, this.textureOffsetX, this.textureOffsetY + length, w, h);
        this.faces[2] = ExTexturedQuad.create(new QuadPoint[]{this.corners[5], this.corners[4], this.corners[0], this.corners[1]}, this.textureOffsetX + length + width + width, this.textureOffsetY, this.textureOffsetX + length + width, this.textureOffsetY + length, w, h);
        this.faces[3] = ExTexturedQuad.create(new QuadPoint[]{this.corners[2], this.corners[3], this.corners[7], this.corners[6]}, this.textureOffsetX + length + width, this.textureOffsetY, this.textureOffsetX + length, this.textureOffsetY + length, w, h);
        this.faces[4] = ExTexturedQuad.create(new QuadPoint[]{this.corners[1], this.corners[0], this.corners[3], this.corners[2]}, this.textureOffsetX + length + width + length + width, this.textureOffsetY + length + height, this.textureOffsetX + length + width + length, this.textureOffsetY + length, w, h);
        this.faces[5] = ExTexturedQuad.create(new QuadPoint[]{this.corners[4], this.corners[5], this.corners[6], this.corners[7]}, this.textureOffsetX + length + width, this.textureOffsetY + length + height, this.textureOffsetX + length, this.textureOffsetY + length, w, h);

        if (this.mirror) {
            for (TexturedQuad face : this.faces) {
                face.method_1925();
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
}
