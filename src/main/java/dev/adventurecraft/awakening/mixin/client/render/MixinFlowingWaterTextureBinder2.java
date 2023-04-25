package dev.adventurecraft.awakening.mixin.client.render;

import java.awt.image.BufferedImage;

import dev.adventurecraft.awakening.client.render.AC_TextureBinder;
import dev.adventurecraft.awakening.common.AC_TerrainImage;
import dev.adventurecraft.awakening.common.Vec2;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.FlowingWaterTextureBinder2;
import net.minecraft.client.render.TextureBinder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(FlowingWaterTextureBinder2.class)
public abstract class MixinFlowingWaterTextureBinder2 extends TextureBinder implements AC_TextureBinder {

    @Shadow
    protected float[] field_2567;

    @Shadow
    protected float[] field_2566;

    @Shadow
    protected float[] field_2568;

    @Shadow
    protected float[] field_2569;

    @Shadow
    private int field_2570;

    boolean hasImages;
    int numFrames;
    int[] frameImages;
    int width;
    int curFrame = 0;

    public MixinFlowingWaterTextureBinder2() {
        super(Block.FLOWING_WATER.texture);
    }

    @Override
    public void onTick(Vec2 var1) {
        int var2 = var1.x / 16;
        int var3 = var1.y / 16;
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
            var4 = var2 / width;
            var5 = curFrame * width * width;
            int var18 = 0;
            boolean var20 = false;
            if (var4 == 0) {
                var20 = true;
                var4 = width / var2;
            }

            int var22;
            if (!var20) {
                for (var8 = 0; var8 < width; ++var8) {
                    for (var22 = 0; var22 < width; ++var22) {
                        var10 = frameImages[var22 + var8 * width + var5];

                        for (var11 = 0; var11 < var4; ++var11) {
                            for (var12 = 0; var12 < var4; ++var12) {
                                var18 = var22 * var4 + var11 + (var8 * var4 + var12) * var2;
                                this.grid[var18 * 4 + 0] = (byte) (var10 >> 16 & 255);
                                this.grid[var18 * 4 + 1] = (byte) (var10 >> 8 & 255);
                                this.grid[var18 * 4 + 2] = (byte) (var10 & 255);
                                this.grid[var18 * 4 + 3] = (byte) (var10 >> 24 & 255);
                            }
                        }
                    }
                }
            } else {
                for (var8 = 0; var8 < var2; ++var8) {
                    for (var22 = 0; var22 < var2; ++var22) {
                        var10 = 0;
                        var11 = 0;
                        var12 = 0;
                        var13 = 0;

                        for (var14 = 0; var14 < var4; ++var14) {
                            for (var15 = 0; var15 < var4; ++var15) {
                                var16 = frameImages[var22 * var4 + var14 + (var8 * var4 + var15) * width + var5];
                                var10 += var16 >> 16 & 255;
                                var11 += var16 >> 8 & 255;
                                var12 += var16 & 255;
                                var13 += var16 >> 24 & 255;
                            }
                        }

                        this.grid[var18 * 4 + 0] = (byte) (var10 / var4 / var4);
                        this.grid[var18 * 4 + 1] = (byte) (var11 / var4 / var4);
                        this.grid[var18 * 4 + 2] = (byte) (var12 / var4 / var4);
                        this.grid[var18 * 4 + 3] = (byte) (var13 / var4 / var4);
                        ++var18;
                    }
                }
            }

            curFrame = (curFrame + 1) % numFrames;
        } else {
            var4 = var2 * var3;
            if (this.field_2566.length != var4) {
                this.field_2566 = new float[var4];
                this.field_2567 = new float[var4];
                this.field_2568 = new float[var4];
                this.field_2569 = new float[var4];
            }

            var5 = (int) Math.sqrt((double) (var2 / 16));
            float var6 = (float) (var5 * 2 + 1) * 1.1F;
            ++this.field_2570;

            int var7;
            float var9;
            for (var7 = 0; var7 < var2; ++var7) {
                for (var8 = 0; var8 < var3; ++var8) {
                    var9 = 0.0F;

                    for (var10 = var7 - var5; var10 <= var7 + var5; ++var10) {
                        var11 = var10 & var2 - 1;
                        var12 = var8 & var3 - 1;
                        var9 += this.field_2566[var11 + var12 * var2];
                    }

                    this.field_2567[var7 + var8 * var2] = var9 / var6 + this.field_2568[var7 + var8 * var2] * 0.8F;
                }
            }

            for (var7 = 0; var7 < var2; ++var7) {
                for (var8 = 0; var8 < var3; ++var8) {
                    this.field_2568[var7 + var8 * var2] += this.field_2569[var7 + var8 * var2] * 0.05F;
                    if (this.field_2568[var7 + var8 * var2] < 0.0F) {
                        this.field_2568[var7 + var8 * var2] = 0.0F;
                    }

                    this.field_2569[var7 + var8 * var2] -= 0.1F;
                    if (Math.random() < 0.05D) {
                        this.field_2569[var7 + var8 * var2] = 0.5F;
                    }
                }
            }

            float[] var19 = this.field_2567;
            this.field_2567 = this.field_2566;
            this.field_2566 = var19;

            for (var8 = 0; var8 < var4; ++var8) {
                var9 = this.field_2566[var8];
                if (var9 > 1.0F) {
                    var9 = 1.0F;
                }

                if (var9 < 0.0F) {
                    var9 = 0.0F;
                }

                float var21 = var9 * var9;
                if (AC_TerrainImage.isWaterLoaded) {
                    var11 = (int) (127.0F + var21 * 128.0F);
                    var12 = (int) (127.0F + var21 * 128.0F);
                    var13 = (int) (127.0F + var21 * 128.0F);
                } else {
                    var11 = (int) (32.0F + var21 * 32.0F);
                    var12 = (int) (50.0F + var21 * 64.0F);
                    var13 = 255;
                }

                var14 = (int) (146.0F + var21 * 50.0F);
                if (this.render3d) {
                    var15 = (var11 * 30 + var12 * 59 + var13 * 11) / 100;
                    var16 = (var11 * 30 + var12 * 70) / 100;
                    int var17 = (var11 * 30 + var13 * 70) / 100;
                    var11 = var15;
                    var12 = var16;
                    var13 = var17;
                }

                this.grid[var8 * 4 + 0] = (byte) var11;
                this.grid[var8 * 4 + 1] = (byte) var12;
                this.grid[var8 * 4 + 2] = (byte) var13;
                this.grid[var8 * 4 + 3] = (byte) var14;
            }

        }
    }

    public void loadImage() {
        loadImage("/custom_water_still.png");
    }

    public void loadImage(String var0) {
        BufferedImage var1 = null;
        if (Minecraft.instance.world != null) {
            var1 = ((ExWorld) Minecraft.instance.world).loadMapTexture(var0);
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
