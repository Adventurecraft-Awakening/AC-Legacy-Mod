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
    public void addBoxInverted(float var1, float var2, float var3, int var4, int var5, int var6, float var7) {
        this.corners = new QuadPoint[8];
        this.faces = new TexturedQuad[6];
        float var8 = var1 + (float) var4;
        float var9 = var2 + (float) var5;
        float var10 = var3 + (float) var6;
        var1 -= var7;
        var2 -= var7;
        var3 -= var7;
        var8 += var7;
        var9 += var7;
        var10 += var7;
        if (this.mirror) {
            float var11 = var8;
            var8 = var1;
            var1 = var11;
        }

        QuadPoint var20 = new QuadPoint(var1, var2, var3, 0.0F, 0.0F);
        QuadPoint var12 = new QuadPoint(var8, var2, var3, 0.0F, 8.0F);
        QuadPoint var13 = new QuadPoint(var8, var9, var3, 8.0F, 8.0F);
        QuadPoint var14 = new QuadPoint(var1, var9, var3, 8.0F, 0.0F);
        QuadPoint var15 = new QuadPoint(var1, var2, var10, 0.0F, 0.0F);
        QuadPoint var16 = new QuadPoint(var8, var2, var10, 0.0F, 8.0F);
        QuadPoint var17 = new QuadPoint(var8, var9, var10, 8.0F, 8.0F);
        QuadPoint var18 = new QuadPoint(var1, var9, var10, 8.0F, 0.0F);
        this.corners[0] = var20;
        this.corners[1] = var12;
        this.corners[2] = var13;
        this.corners[3] = var14;
        this.corners[4] = var15;
        this.corners[5] = var16;
        this.corners[6] = var17;
        this.corners[7] = var18;

        int w = this.tWidth;
        int h = this.tHeight;
        this.faces[0] = ExTexturedQuad.create(new QuadPoint[]{var16, var12, var13, var17}, this.textureOffsetX + var6 + var4 + var6, this.textureOffsetY + var6 + var5, this.textureOffsetX + var6 + var4, this.textureOffsetY + var6, w, h);
        this.faces[1] = ExTexturedQuad.create(new QuadPoint[]{var20, var15, var18, var14}, this.textureOffsetX + var6, this.textureOffsetY + var6 + var5, this.textureOffsetX, this.textureOffsetY + var6, w, h);
        this.faces[2] = ExTexturedQuad.create(new QuadPoint[]{var16, var15, var20, var12}, this.textureOffsetX + var6 + var4 + var4, this.textureOffsetY, this.textureOffsetX + var6 + var4, this.textureOffsetY + var6, w, h);
        this.faces[3] = ExTexturedQuad.create(new QuadPoint[]{var13, var14, var18, var17}, this.textureOffsetX + var6 + var4, this.textureOffsetY, this.textureOffsetX + var6, this.textureOffsetY + var6, w, h);
        this.faces[4] = ExTexturedQuad.create(new QuadPoint[]{var12, var20, var14, var13}, this.textureOffsetX + var6 + var4 + var6 + var4, this.textureOffsetY + var6 + var5, this.textureOffsetX + var6 + var4 + var6, this.textureOffsetY + var6, w, h);
        this.faces[5] = ExTexturedQuad.create(new QuadPoint[]{var15, var16, var17, var18}, this.textureOffsetX + var6 + var4, this.textureOffsetY + var6 + var5, this.textureOffsetX + var6, this.textureOffsetY + var6, w, h);

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
