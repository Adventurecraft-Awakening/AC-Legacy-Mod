package dev.adventurecraft.awakening.mixin.client.render;

import dev.adventurecraft.awakening.common.Vec2;
import net.minecraft.client.renderer.ptexture.LavaSideTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LavaSideTexture.class)
public class MixinFlowingLavaTextureBinder2 extends MixinTextureBinder {

    @Shadow
    protected float[] current;

    @Shadow
    protected float[] next;

    @Shadow
    protected float[] heat;

    @Shadow
    protected float[] heata;

    @Shadow
    int ticks;

    @Override
    public void onTick(Vec2 size) {
        int var2 = size.x / 16;
        int var3 = size.y / 16;
        int var4;
        int var5;
        int var6;
        int var8;
        int var9;
        int var10;
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
            boolean var19 = false;
            if (var4 == 0) {
                var19 = true;
                var4 = width / var2;
            }

            int var21;
            if (!var19) {
                for (var8 = 0; var8 < width; ++var8) {
                    for (var9 = 0; var9 < width; ++var9) {
                        var10 = this.imageData.get(var9 + var8 * width + var5);

                        for (var21 = 0; var21 < var4; ++var21) {
                            for (var12 = 0; var12 < var4; ++var12) {
                                var6 = var9 * var4 + var21 + (var8 * var4 + var12) * var2;
                                this.pixels[var6 * 4 + 0] = (byte) (var10 >> 16 & 255);
                                this.pixels[var6 * 4 + 1] = (byte) (var10 >> 8 & 255);
                                this.pixels[var6 * 4 + 2] = (byte) (var10 & 255);
                                this.pixels[var6 * 4 + 3] = (byte) (var10 >> 24 & 255);
                            }
                        }
                    }
                }
            } else {
                for (var8 = 0; var8 < var2; ++var8) {
                    for (var9 = 0; var9 < var2; ++var9) {
                        var10 = 0;
                        var21 = 0;
                        var12 = 0;
                        var13 = 0;

                        for (var14 = 0; var14 < var4; ++var14) {
                            for (var15 = 0; var15 < var4; ++var15) {
                                var16 = this.imageData.get(var9 * var4 + var14 + (var8 * var4 + var15) * width + var5);
                                var10 += var16 >> 16 & 255;
                                var21 += var16 >> 8 & 255;
                                var12 += var16 & 255;
                                var13 += var16 >> 24 & 255;
                            }
                        }

                        this.pixels[var6 * 4 + 0] = (byte) (var10 / var4 / var4);
                        this.pixels[var6 * 4 + 1] = (byte) (var21 / var4 / var4);
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
            var8 = (int) Math.sqrt((double) (var2 / 16));
            this.ticks += var8;

            float var11;
            int var17;
            for (var9 = 0; var9 < var2; ++var9) {
                for (var10 = 0; var10 < var3; ++var10) {
                    var11 = 0.0F;
                    var12 = (int) (Mth.sin((float) var10 * 3.141593F * 2.0F / (float) var2) * 1.2F);
                    var13 = (int) (Mth.sin((float) var9 * 3.141593F * 2.0F / (float) var3) * 1.2F);

                    for (var14 = var9 - var5; var14 <= var9 + var5; ++var14) {
                        for (var15 = var10 - var6; var15 <= var10 + var6; ++var15) {
                            var16 = var14 + var12 & var2 - 1;
                            var17 = var15 + var13 & var3 - 1;
                            var11 += this.current[var16 + var17 * var2];
                        }
                    }

                    this.next[var9 + var10 * var2] = var11 / var7 + (this.heat[(var9 + 0 & var2 - 1) + (var10 + 0 & var3 - 1) * var2] + this.heat[(var9 + 1 & var2 - 1) + (var10 + 0 & var3 - 1) * var2] + this.heat[(var9 + 1 & var2 - 1) + (var10 + 1 & var3 - 1) * var2] + this.heat[(var9 + 0 & var2 - 1) + (var10 + 1 & var3 - 1) * var2]) / 4.0F * 0.8F;
                    this.heat[var9 + var10 * var2] += this.heata[var9 + var10 * var2] * 0.01F;
                    if (this.heat[var9 + var10 * var2] < 0.0F) {
                        this.heat[var9 + var10 * var2] = 0.0F;
                    }

                    this.heata[var9 + var10 * var2] -= 0.06F;
                    if (Math.random() < 0.005D) {
                        this.heata[var9 + var10 * var2] = 1.5F;
                    }
                }
            }

            float[] var20 = this.next;
            this.next = this.current;
            this.current = var20;

            for (var10 = 0; var10 < var4; ++var10) {
                var11 = this.current[var10 - this.ticks / 3 * var2 & var4 - 1] * 2.0F;
                if (var11 > 1.0F) {
                    var11 = 1.0F;
                }

                if (var11 < 0.0F) {
                    var11 = 0.0F;
                }

                var13 = (int) (var11 * 100.0F + 155.0F);
                var14 = (int) (var11 * var11 * 255.0F);
                var15 = (int) (var11 * var11 * var11 * var11 * 128.0F);
                if (this.anaglyph3d) {
                    var16 = (var13 * 30 + var14 * 59 + var15 * 11) / 100;
                    var17 = (var13 * 30 + var14 * 70) / 100;
                    int var18 = (var13 * 30 + var15 * 70) / 100;
                    var13 = var16;
                    var14 = var17;
                    var15 = var18;
                }

                this.pixels[var10 * 4 + 0] = (byte) var13;
                this.pixels[var10 * 4 + 1] = (byte) var14;
                this.pixels[var10 * 4 + 2] = (byte) var15;
                this.pixels[var10 * 4 + 3] = -1;
            }
        }
    }

    @Override
    public void loadImage(String name, Level world) {
        if (name == null) {
            name = "/custom_lava_flowing.png";
        }

        super.loadImage(name, world);
    }
}
