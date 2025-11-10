package dev.adventurecraft.awakening.mixin.client.render;

import dev.adventurecraft.awakening.image.Rgba;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ptexture.ClockTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClockTexture.class)
public class MixinClockTexture extends MixinTextureBinder {

    @Shadow
    private Minecraft mc;

    @Shadow
    private int[] raw;

    @Shadow
    private int[] dialRaw;

    @Shadow
    private double rot;

    @Shadow
    private double rota;

    @Override
    public void tick() {
        double var1 = 0.0;
        if (this.mc.level != null && this.mc.player != null) {
            float var3 = this.mc.level.getTimeOfDay(1.0F);
            var1 = (double) (-var3 * 3.1415927F * 2.0F);
            if (this.mc.level.dimension.natural) {
                var1 = Math.random() * 3.1415927410125732 * 2.0;
            }
        }

        double var22;
        for (var22 = var1 - this.rot; var22 < -3.141592653589793; var22 += 6.283185307179586) {
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
        double var5 = Math.sin(this.rot);
        double var7 = Math.cos(this.rot);

        var imageData = this.imageData.asIntBuffer();
        for (int var9 = 0; var9 < 256; ++var9) {
            int var10 = this.raw[var9] >> 24 & 255;
            int var11 = this.raw[var9] >> 16 & 255;
            int var12 = this.raw[var9] >> 8 & 255;
            int var13 = this.raw[var9] >> 0 & 255;
            if (var11 == var13 && var12 == 0 && var13 > 0) {
                double var14 = -((double) (var9 % 16) / 15.0 - 0.5);
                double var16 = (double) (var9 / 16) / 15.0 - 0.5;
                int var18 = var11;
                int var19 = (int) ((var14 * var7 + var16 * var5 + 0.5) * 16.0);
                int var20 = (int) ((var16 * var7 - var14 * var5 + 0.5) * 16.0);
                int var21 = (var19 & 15) + (var20 & 15) * 16;
                var10 = this.dialRaw[var21] >> 24 & 255;
                var11 = (this.dialRaw[var21] >> 16 & 255) * var11 / 255;
                var12 = (this.dialRaw[var21] >> 8 & 255) * var18 / 255;
                var13 = (this.dialRaw[var21] >> 0 & 255) * var18 / 255;
            }

            if (this.anaglyph3d) {
                int var23 = (var11 * 30 + var12 * 59 + var13 * 11) / 100;
                int var15 = (var11 * 30 + var12 * 70) / 100;
                int var24 = (var11 * 30 + var13 * 70) / 100;
                var11 = var23;
                var12 = var15;
                var13 = var24;
            }

            int color = Rgba.fromRgba8(var11, var12, var13, var10);
            imageData.put(var9, color);
        }
    }
}
