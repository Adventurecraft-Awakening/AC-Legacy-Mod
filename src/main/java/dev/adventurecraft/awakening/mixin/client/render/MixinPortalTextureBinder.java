package dev.adventurecraft.awakening.mixin.client.render;

import dev.adventurecraft.awakening.common.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;
import net.minecraft.client.renderer.ptexture.PortalTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

@Mixin(PortalTexture.class)
public class MixinPortalTextureBinder extends MixinTextureBinder {

    @Shadow
    private int time;

    @Shadow
    private byte[][] frames;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initData(CallbackInfo ci) {
        this.generatePortalData(16, 16);
    }

    private void generatePortalData(int var1, int var2) {
        this.frames = new byte[32][var1 * var2 * 4];
        Random var3 = new Random(100L);

        for (int var4 = 0; var4 < 32; ++var4) {
            for (int var5 = 0; var5 < var1; ++var5) {
                for (int var6 = 0; var6 < var2; ++var6) {
                    float var7 = 0.0F;

                    int var8;
                    for (var8 = 0; var8 < 2; ++var8) {
                        float var9 = (float) (var8 * var1 / 2);
                        float var10 = (float) (var8 * var2 / 2);
                        float var11 = ((float) var5 - var9) / (float) var1 * 2.0F;
                        float var12 = ((float) var6 - var10) / (float) var2 * 2.0F;
                        if (var11 < -1.0F) {
                            var11 += 2.0F;
                        }

                        if (var11 >= 1.0F) {
                            var11 -= 2.0F;
                        }

                        if (var12 < -1.0F) {
                            var12 += 2.0F;
                        }

                        if (var12 >= 1.0F) {
                            var12 -= 2.0F;
                        }

                        float var13 = var11 * var11 + var12 * var12;
                        float var14 = (float) Math.atan2((double) var12, (double) var11) + ((float) var4 / 32.0F * 3.141593F * 2.0F - var13 * 10.0F + (float) (var8 * 2)) * (float) (var8 * 2 - 1);
                        var14 = (Mth.sin(var14) + 1.0F) / 2.0F;
                        var14 /= var13 + 1.0F;
                        var7 += var14 * 0.5F;
                    }

                    var7 += var3.nextFloat() * 0.1F;
                    var8 = (int) (var7 * 100.0F + 155.0F);
                    int var15 = (int) (var7 * var7 * 200.0F + 55.0F);
                    int var16 = (int) (var7 * var7 * var7 * var7 * 255.0F);
                    int var17 = (int) (var7 * 100.0F + 155.0F);
                    int var18 = var6 * var1 + var5;
                    this.frames[var4][var18 * 4 + 0] = (byte) var15;
                    this.frames[var4][var18 * 4 + 1] = (byte) var16;
                    this.frames[var4][var18 * 4 + 2] = (byte) var8;
                    this.frames[var4][var18 * 4 + 3] = (byte) var17;
                }
            }
        }
    }

    @Override
    public void onTick(Vec2 size) {
        int var2 = size.x / 16;
        int var3 = size.y / 16;
        int var4;
        int var6;
        int var8;
        int var9;
        int var10;
        int var11;
        int var12;
        int var13;
        if (hasImages) {
            this.imageData.clear();

            var4 = var2 / width;
            int var17 = curFrame * width * width;
            var6 = 0;
            boolean var18 = false;
            if (var4 == 0) {
                var18 = true;
                var4 = width / var2;
            }

            if (!var18) {
                for (var8 = 0; var8 < width; ++var8) {
                    for (var9 = 0; var9 < width; ++var9) {
                        var10 = this.imageData.get(var9 + var8 * width + var17);

                        for (var11 = 0; var11 < var4; ++var11) {
                            for (var12 = 0; var12 < var4; ++var12) {
                                var6 = var9 * var4 + var11 + (var8 * var4 + var12) * var2;
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
                        var11 = 0;
                        var12 = 0;
                        var13 = 0;

                        for (int var14 = 0; var14 < var4; ++var14) {
                            for (int var15 = 0; var15 < var4; ++var15) {
                                int var16 = this.imageData.get(var9 * var4 + var14 + (var8 * var4 + var15) * width + var17);
                                var10 += var16 >> 16 & 255;
                                var11 += var16 >> 8 & 255;
                                var12 += var16 & 255;
                                var13 += var16 >> 24 & 255;
                            }
                        }

                        this.pixels[var6 * 4 + 0] = (byte) (var10 / var4 / var4);
                        this.pixels[var6 * 4 + 1] = (byte) (var11 / var4 / var4);
                        this.pixels[var6 * 4 + 2] = (byte) (var12 / var4 / var4);
                        this.pixels[var6 * 4 + 3] = (byte) (var13 / var4 / var4);
                        ++var6;
                    }
                }
            }

            curFrame = (curFrame + 1) % numFrames;
        } else {
            var4 = size.x * size.y / 256;
            if (this.frames[0].length != var4 * 4) {
                this.generatePortalData(size.x / 16, size.y / 16);
            }

            ++this.time;
            byte[] var5 = this.frames[this.time & 31];

            for (var6 = 0; var6 < var4; ++var6) {
                int var7 = var5[var6 * 4 + 0] & 255;
                var8 = var5[var6 * 4 + 1] & 255;
                var9 = var5[var6 * 4 + 2] & 255;
                var10 = var5[var6 * 4 + 3] & 255;
                if (this.anaglyph3d) {
                    var11 = (var7 * 30 + var8 * 59 + var9 * 11) / 100;
                    var12 = (var7 * 30 + var8 * 70) / 100;
                    var13 = (var7 * 30 + var9 * 70) / 100;
                    var7 = var11;
                    var8 = var12;
                    var9 = var13;
                }

                this.pixels[var6 * 4 + 0] = (byte) var7;
                this.pixels[var6 * 4 + 1] = (byte) var8;
                this.pixels[var6 * 4 + 2] = (byte) var9;
                this.pixels[var6 * 4 + 3] = (byte) var10;
            }

        }
    }

    @Override
    public void loadImage(String name, Level world) {
        if (name == null) {
            name = "/custom_portal.png";
        }

        super.loadImage(name, world);
    }
}

