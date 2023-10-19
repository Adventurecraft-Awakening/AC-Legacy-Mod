package dev.adventurecraft.awakening.mixin.client.render;

import dev.adventurecraft.awakening.common.Vec2;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.client.render.FireTextureBinder;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.awt.image.BufferedImage;

@Mixin(FireTextureBinder.class)
public class MixinFireTextureBinder extends MixinTextureBinder {

    @Shadow
    protected float[] currentFireFrame;

    @Shadow
    protected float[] lastFireFrame;

    boolean hasImages;
    int numFrames;
    int[] frameImages;
    int width;
    int curFrame = 0;

    @Override
    public void onTick(Vec2 var1) {
        int var2 = var1.x / 16;
        int var3 = var1.y / 16;
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
        int var24;
        if (hasImages) {
            int var18 = var2 / width;
            var5 = curFrame * width * width;
            var6 = 0;
            boolean var20 = false;
            if (var18 == 0) {
                var20 = true;
                var18 = width / var2;
            }

            if (!var20) {
                for (var8 = 0; var8 < width; ++var8) {
                    for (var9 = 0; var9 < width; ++var9) {
                        var10 = frameImages[var9 + var8 * width + var5];

                        for (var24 = 0; var24 < var18; ++var24) {
                            for (var12 = 0; var12 < var18; ++var12) {
                                var6 = var9 * var18 + var24 + (var8 * var18 + var12) * var2;
                                this.grid[var6 * 4 + 0] = (byte) (var10 >> 16 & 255);
                                this.grid[var6 * 4 + 1] = (byte) (var10 >> 8 & 255);
                                this.grid[var6 * 4 + 2] = (byte) (var10 & 255);
                                this.grid[var6 * 4 + 3] = (byte) (var10 >> 24 & 255);
                            }
                        }
                    }
                }
            } else {
                for (var8 = 0; var8 < var2; ++var8) {
                    for (var9 = 0; var9 < var2; ++var9) {
                        var10 = 0;
                        var24 = 0;
                        var12 = 0;
                        var13 = 0;

                        for (var14 = 0; var14 < var18; ++var14) {
                            for (var15 = 0; var15 < var18; ++var15) {
                                var16 = frameImages[var9 * var18 + var14 + (var8 * var18 + var15) * width + var5];
                                var10 += var16 >> 16 & 255;
                                var24 += var16 >> 8 & 255;
                                var12 += var16 & 255;
                                var13 += var16 >> 24 & 255;
                            }
                        }

                        this.grid[var6 * 4 + 0] = (byte) (var10 / var18 / var18);
                        this.grid[var6 * 4 + 1] = (byte) (var24 / var18 / var18);
                        this.grid[var6 * 4 + 2] = (byte) (var12 / var18 / var18);
                        this.grid[var6 * 4 + 3] = (byte) (var13 / var18 / var18);
                        ++var6;
                    }
                }
            }

            curFrame = (curFrame + 1) % numFrames;
        } else {
            var3 = var1.y / 16 * 20 / 16;
            if (this.currentFireFrame.length != var2 * var3) {
                this.currentFireFrame = new float[var2 * var3];
                this.lastFireFrame = new float[var2 * var3];
            }

            float var4 = 1.0F + 15.36F / (float) var1.y;
            var5 = var1.y / 256;
            var6 = 14 + (var5 + 1) * (var5 + 1);
            byte var19;
            if (var5 >= 4) {
                var19 = 2;
            } else {
                var19 = 1;
            }

            int var7;
            for (var7 = 0; var7 < var19; ++var7) {
                for (var8 = 0; var8 < var3; ++var8) {
                    for (var9 = 0; var9 < var2; ++var9) {
                        var10 = var6;
                        float var11 = this.currentFireFrame[var9 + (var8 + 1) % var3 * var2] * (float) var6;

                        for (var12 = var9 - 1; var12 <= var9 + 1; ++var12) {
                            for (var13 = var8; var13 <= var8 + 1; ++var13) {
                                var14 = var12 % var2;
                                while (var14 < 0) {
                                    var14 += var2;
                                }

                                if (var13 >= 0 && var13 < var3) {
                                    var11 += this.currentFireFrame[var14 + var13 * var2];
                                }

                                ++var10;
                            }
                        }

                        this.lastFireFrame[var9 + var8 * var2] = var11 / ((float) var10 * var4);
                        if (var8 >= var3 - 1) {
                            this.lastFireFrame[var9 + var8 * var2] = (float) (Math.random() * Math.random() * Math.random() * 4.0D + Math.random() * (double) 0.1F + (double) 0.2F);
                        }
                    }
                }

                float[] var21 = this.lastFireFrame;
                this.lastFireFrame = this.currentFireFrame;
                this.currentFireFrame = var21;
            }

            var3 = var1.y / 16;
            var7 = var2 * var3;

            for (var8 = 0; var8 < var7; ++var8) {
                float var22 = this.currentFireFrame[var8] * 1.8F;
                if (var22 > 1.0F) {
                    var22 = 1.0F;
                }

                if (var22 < 0.0F) {
                    var22 = 0.0F;
                }

                var24 = (int) (var22 * 155.0F + 100.0F);
                var12 = (int) (var22 * var22 * 255.0F);
                var13 = (int) (var22 * var22 * var22 * var22 * var22 * var22 * var22 * var22 * var22 * var22 * 255.0F);
                short var25 = 255;
                if (var22 < 0.5F) {
                    var25 = 0;
                }

                float var23 = (var22 - 0.5F) * 2.0F;
                if (this.render3d) {
                    var15 = (var24 * 30 + var12 * 59 + var13 * 11) / 100;
                    var16 = (var24 * 30 + var12 * 70) / 100;
                    int var17 = (var24 * 30 + var13 * 70) / 100;
                    var24 = var15;
                    var12 = var16;
                    var13 = var17;
                }

                this.grid[var8 * 4 + 0] = (byte) var24;
                this.grid[var8 * 4 + 1] = (byte) var12;
                this.grid[var8 * 4 + 2] = (byte) var13;
                this.grid[var8 * 4 + 3] = (byte) var25;
            }
        }
    }

    @Override
    public void loadImage(World world) {
        loadImage("/custom_fire.png", world);
    }

    @Override
    public void loadImage(String name, World world) {
        BufferedImage var1 = null;
        if (world != null) {
            var1 = ((ExWorld) world).loadMapTexture(name);
        }

        curFrame = 0;
        if (var1 == null) {
            hasImages = false;
        } else {
            width = var1.getWidth();
            numFrames = var1.getHeight() / var1.getWidth();
            frameImages = new int[var1.getWidth() * var1.getHeight()];
            var1.getRGB(0, 0, var1.getWidth(), var1.getHeight(), frameImages, 0, var1.getWidth());
            hasImages = true;
        }
    }
}
