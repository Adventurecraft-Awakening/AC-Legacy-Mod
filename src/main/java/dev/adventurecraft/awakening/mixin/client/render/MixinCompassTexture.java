package dev.adventurecraft.awakening.mixin.client.render;

import dev.adventurecraft.awakening.image.Rgba;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ptexture.CompassTexture;
import net.minecraft.util.Vec3i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CompassTexture.class)
public class MixinCompassTexture extends MixinTextureBinder {

    @Shadow
    private int[] raw;

    @Shadow
    private Minecraft mc;

    @Shadow
    private double rota;

    @Shadow
    private double rot;

    @Overwrite
    public void tick() {
        var imageData = this.imageData;
        for (int var1 = 0; var1 < 256; ++var1) {
            int srcColor = this.raw[var1];
            int var2 = srcColor >> 24 & 255;
            int var3 = srcColor >> 16 & 255;
            int var4 = srcColor >> 8 & 255;
            int var5 = srcColor & 255;
            if (this.anaglyph3d) {
                int var6 = (var3 * 30 + var4 * 59 + var5 * 11) / 100;
                int var7 = (var3 * 30 + var4 * 70) / 100;
                int var8 = (var3 * 30 + var5 * 70) / 100;
                var3 = var6;
                var4 = var7;
                var5 = var8;
            }

            int color = Rgba.fromRgba8(var5, var4, var3, var2);
            imageData.put(var1, color);
        }

        double var20 = 0.0;
        if (this.mc.level != null && this.mc.player != null) {
            Vec3i var21 = this.mc.level.getSpawnPos();
            double var23 = (double) var21.x - this.mc.player.x;
            double var25 = (double) var21.z - this.mc.player.z;
            var20 = (double) (this.mc.player.yRot - 90.0F) * Math.PI / 180.0 - Math.atan2(var25, var23);
            if (this.mc.level.dimension.natural) {
                var20 = Math.random() * 3.1415927410125732 * 2.0;
            }
        }

        double var22;
        for (var22 = var20 - this.rot; var22 < -3.141592653589793; var22 += 6.283185307179586) {
        }

        while (var22 >= Math.PI) {
            var22 -= 6.283185307179586;
        }

        if (var22 < -1.0) {
            var22 = -1.0;
        }

        if (var22 > 1.0) {
            var22 = 1.0;
        }

        this.rota += var22 * 0.1;
        this.rota *= 0.8;
        this.rot += this.rota;
        double var24 = Math.sin(this.rot);
        double var26 = Math.cos(this.rot);

        int var9;
        int var10;
        int var11;
        int var12;
        int var13;
        int var14;
        int var15;
        short var16;
        int var17;
        int var18;
        int var19;
        for (var9 = -4; var9 <= 4; ++var9) {
            var10 = (int) (8.5 + var26 * (double) var9 * 0.3);
            var11 = (int) (7.5 - var24 * (double) var9 * 0.3 * 0.5);
            var12 = var11 * 16 + var10;
            var13 = 100;
            var14 = 100;
            var15 = 100;
            var16 = 255;
            if (this.anaglyph3d) {
                var17 = (var13 * 30 + var14 * 59 + var15 * 11) / 100;
                var18 = (var13 * 30 + var14 * 70) / 100;
                var19 = (var13 * 30 + var15 * 70) / 100;
                var13 = var17;
                var14 = var18;
                var15 = var19;
            }

            int color = Rgba.fromRgba8(var15, var14, var13, var16);
            imageData.put(var12, color);
        }

        for (var9 = -8; var9 <= 16; ++var9) {
            var10 = (int) (8.5 + var24 * (double) var9 * 0.3);
            var11 = (int) (7.5 + var26 * (double) var9 * 0.3 * 0.5);
            var12 = var11 * 16 + var10;
            var13 = var9 >= 0 ? 255 : 100;
            var14 = var9 >= 0 ? 20 : 100;
            var15 = var9 >= 0 ? 20 : 100;
            var16 = 255;
            if (this.anaglyph3d) {
                var17 = (var13 * 30 + var14 * 59 + var15 * 11) / 100;
                var18 = (var13 * 30 + var14 * 70) / 100;
                var19 = (var13 * 30 + var15 * 70) / 100;
                var13 = var17;
                var14 = var18;
                var15 = var19;
            }

            int color = Rgba.fromRgba8(var13, var14, var15, var16);
            imageData.put(var12, color);
        }
    }
}
