package dev.adventurecraft.awakening.mixin.client.render;

import dev.adventurecraft.awakening.common.AC_TerrainImage;
import dev.adventurecraft.awakening.image.Rgba;
import dev.adventurecraft.awakening.layout.Size;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.renderer.ptexture.WaterSideTexture;
import net.minecraft.world.level.Level;

@Mixin(WaterSideTexture.class)
public class MixinFlowingWaterTextureBinder extends MixinTextureBinder {

    @Shadow
    protected float[] current;

    @Shadow
    protected float[] next;

    @Shadow
    protected float[] heat;

    @Shadow
    protected float[] heata;

    @Shadow
    private int tickCount;

    @Override
    public void onTick(Size size) {
        int var2 = size.w / 16;
        int var3 = size.h / 16;
        int var4;
        int var5;
        int var8;
        int var10;
        int var11;
        int var12;
        int var13;
        int var14;
        int var15;
        int var16;
        if (hasImages) {
            this.animate();
        } else {
            var4 = var2 * var3;
            if (this.current.length != var4) {
                this.current = new float[var4];
                this.next = new float[var4];
                this.heat = new float[var4];
                this.heata = new float[var4];
                this.imageData = this.allocImageData(var2, var3);
            }

            var5 = (int) Math.sqrt((double) (var3 / 16));
            this.tickCount += var5;
            float var6 = (float) (var5 * 2 + 1) * 1.0667F;

            int var7;
            float var9;
            for (var7 = 0; var7 < var2; ++var7) {
                for (var8 = 0; var8 < var3; ++var8) {
                    var9 = 0.0F;

                    for (var10 = var8 - 2 * var5; var10 <= var8; ++var10) {
                        var11 = var7 & var2 - 1;
                        var12 = var10 & var3 - 1;
                        var9 += this.current[var11 + var12 * var2];
                    }

                    this.next[var7 + var8 * var2] = var9 / var6 + this.heat[var7 + var8 * var2] * 0.8F;
                }
            }

            for (var7 = 0; var7 < var2; ++var7) {
                for (var8 = 0; var8 < var3; ++var8) {
                    this.heat[var7 + var8 * var2] += this.heata[var7 + var8 * var2] * 0.05F;
                    if (this.heat[var7 + var8 * var2] < 0.0F) {
                        this.heat[var7 + var8 * var2] = 0.0F;
                    }

                    this.heata[var7 + var8 * var2] -= 0.3F;
                    if (Math.random() < 0.2D) {
                        this.heata[var7 + var8 * var2] = 0.5F;
                    }
                }
            }

            float[] var19 = this.next;
            this.next = this.current;
            this.current = var19;

            var imageData = this.imageData.asIntBuffer();
            for (var8 = 0; var8 < var4; ++var8) {
                var9 = this.current[var8 - this.tickCount * var2 & var4 - 1];
                if (var9 > 1.0F) {
                    var9 = 1.0F;
                }

                if (var9 < 0.0F) {
                    var9 = 0.0F;
                }

                float var21 = var9 * var9;
                if (AC_TerrainImage.isWaterLoaded()) {
                    var11 = (int) (127.0F + var21 * 128.0F);
                    var12 = (int) (127.0F + var21 * 128.0F);
                    var13 = (int) (127.0F + var21 * 128.0F);
                } else {
                    var11 = (int) (32.0F + var21 * 32.0F);
                    var12 = (int) (50.0F + var21 * 64.0F);
                    var13 = 255;
                }

                var14 = (int) (146.0F + var21 * 50.0F);
                if (this.anaglyph3d) {
                    var15 = (var11 * 30 + var12 * 59 + var13 * 11) / 100;
                    var16 = (var11 * 30 + var12 * 70) / 100;
                    int var17 = (var11 * 30 + var13 * 70) / 100;
                    var11 = var15;
                    var12 = var16;
                    var13 = var17;
                }

                int color = Rgba.fromRgba8(var11, var12, var13, var14);
                imageData.put(var8, color);
            }
        }
    }

    @Override
    public void loadImage(String name, Level world) {
        if (name == null) {
            name = "/custom_water_flowing.png";
        }

        super.loadImage(name, world);
    }
}
