package dev.adventurecraft.awakening.mixin.client.render;

import dev.adventurecraft.awakening.common.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.renderer.ptexture.LavaTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

@Mixin(LavaTexture.class)
public class MixinFlowingLavaTextureBinder extends MixinTextureBinder {

    @Shadow
    protected float[] current;

    @Shadow
    protected float[] next;

    @Shadow
    protected float[] heat;

    @Shadow
    protected float[] heata;

    @Override
    public void onTick(Vec2 size) {
        int var2 = size.x / 16;
        int var3 = size.y / 16;
        int var4;
        int var5;
        int var6;
        int var8;
        int var9;
        int var11;
        int var12;
        int var13;
        int var14;
        int var15;
        int var16;
        if (hasImages) {
            this.imageData.clear();

            var4 = var2 / width;
            var5 = curFrame * width * width;
            var6 = 0;
            boolean var18 = false;
            if (var4 == 0) {
                var18 = true;
                var4 = width / var2;
            }

            int var20;
            if (!var18) {
                for (var8 = 0; var8 < width; ++var8) {
                    for (var9 = 0; var9 < width; ++var9) {
                        var20 = this.imageData.get(var9 + var8 * width + var5);

                        for (var11 = 0; var11 < var4; ++var11) {
                            for (var12 = 0; var12 < var4; ++var12) {
                                var6 = var9 * var4 + var11 + (var8 * var4 + var12) * var2;
                                this.pixels[var6 * 4 + 0] = (byte) (var20 >> 16 & 255);
                                this.pixels[var6 * 4 + 1] = (byte) (var20 >> 8 & 255);
                                this.pixels[var6 * 4 + 2] = (byte) (var20 & 255);
                                this.pixels[var6 * 4 + 3] = (byte) (var20 >> 24 & 255);
                            }
                        }
                    }
                }
            } else {
                for (var8 = 0; var8 < var2; ++var8) {
                    for (var9 = 0; var9 < var2; ++var9) {
                        var20 = 0;
                        var11 = 0;
                        var12 = 0;
                        var13 = 0;

                        for (var14 = 0; var14 < var4; ++var14) {
                            for (var15 = 0; var15 < var4; ++var15) {
                                var16 = this.imageData.get(var9 * var4 + var14 + (var8 * var4 + var15) * width + var5);
                                var20 += var16 >> 16 & 255;
                                var11 += var16 >> 8 & 255;
                                var12 += var16 & 255;
                                var13 += var16 >> 24 & 255;
                            }
                        }

                        this.pixels[var6 * 4 + 0] = (byte) (var20 / var4 / var4);
                        this.pixels[var6 * 4 + 1] = (byte) (var11 / var4 / var4);
                        this.pixels[var6 * 4 + 2] = (byte) (var12 / var4 / var4);
                        this.pixels[var6 * 4 + 3] = (byte) (var13 / var4 / var4);
                        ++var6;
                    }
                }
            }

            curFrame = (curFrame + 1) % numFrames;
        } else {
            var4 = var2 * var3;
            if (this.current.length != var4) {
                this.current = new float[var4];
                this.next = new float[var4];
                this.heat = new float[var4];
                this.heata = new float[var4];
            }

            var5 = (int) Math.sqrt((double) (var2 / 16));
            var6 = (int) Math.sqrt((double) (var3 / 16));
            float var7 = (float) ((var5 * 2 + 1) * (var6 * 2 + 1)) * 1.1F;

            float var10;
            for (var8 = 0; var8 < var2; ++var8) {
                for (var9 = 0; var9 < var3; ++var9) {
                    var10 = 0.0F;
                    var11 = (int) (Mth.sin((float) var9 * 3.141593F * 2.0F / (float) var2) * 1.2F);
                    var12 = (int) (Mth.sin((float) var8 * 3.141593F * 2.0F / (float) var3) * 1.2F);

                    for (var13 = var8 - var5; var13 <= var8 + var5; ++var13) {
                        for (var14 = var9 - var6; var14 <= var9 + var6; ++var14) {
                            var15 = var13 + var11 & var2 - 1;
                            var16 = var14 + var12 & var3 - 1;
                            var10 += this.current[var15 + var16 * var2];
                        }
                    }

                    this.next[var8 + var9 * var2] = var10 / var7 + (this.heat[(var8 + 0 & var2 - 1) + (var9 + 0 & var3 - 1) * var2] + this.heat[(var8 + 1 & var2 - 1) + (var9 + 0 & var3 - 1) * var2] + this.heat[(var8 + 1 & var2 - 1) + (var9 + 1 & var3 - 1) * var2] + this.heat[(var8 + 0 & var2 - 1) + (var9 + 1 & var3 - 1) * var2]) / 4.0F * 0.8F;
                    this.heat[var8 + var9 * var2] += this.heata[var8 + var9 * var2] * 0.01F;
                    if (this.heat[var8 + var9 * var2] < 0.0F) {
                        this.heat[var8 + var9 * var2] = 0.0F;
                    }

                    this.heata[var8 + var9 * var2] -= 0.06F;
                    if (Math.random() < 0.005D) {
                        this.heata[var8 + var9 * var2] = 1.5F;
                    }
                }
            }

            float[] var19 = this.next;
            this.next = this.current;
            this.current = var19;

            for (var9 = 0; var9 < var4; ++var9) {
                var10 = this.current[var9] * 2.0F;
                if (var10 > 1.0F) {
                    var10 = 1.0F;
                }

                if (var10 < 0.0F) {
                    var10 = 0.0F;
                }

                var12 = (int) (var10 * 100.0F + 155.0F);
                var13 = (int) (var10 * var10 * 255.0F);
                var14 = (int) (var10 * var10 * var10 * var10 * 128.0F);
                if (this.anaglyph3d) {
                    var15 = (var12 * 30 + var13 * 59 + var14 * 11) / 100;
                    var16 = (var12 * 30 + var13 * 70) / 100;
                    int var17 = (var12 * 30 + var14 * 70) / 100;
                    var12 = var15;
                    var13 = var16;
                    var14 = var17;
                }

                this.pixels[var9 * 4 + 0] = (byte) var12;
                this.pixels[var9 * 4 + 1] = (byte) var13;
                this.pixels[var9 * 4 + 2] = (byte) var14;
                this.pixels[var9 * 4 + 3] = -1;
            }

        }
    }

    @Override
    public void loadImage(String name, Level world) {
        if (name == null) {
            name = "/custom_lava_still.png";
        }

        super.loadImage(name, world);
    }
}
