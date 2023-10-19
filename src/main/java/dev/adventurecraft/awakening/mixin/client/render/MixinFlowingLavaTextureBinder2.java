package dev.adventurecraft.awakening.mixin.client.render;

import dev.adventurecraft.awakening.common.Vec2;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.client.render.FlowingLavaTextureBinder2;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.awt.image.BufferedImage;

@Mixin(FlowingLavaTextureBinder2.class)
public class MixinFlowingLavaTextureBinder2 extends MixinTextureBinder {

    @Shadow
    protected float[] field_1166;

    @Shadow
    protected float[] field_1167;

    @Shadow
    protected float[] field_1168;

    @Shadow
    protected float[] field_1169;

    @Shadow
    int field_1170;

    boolean hasImages;
    int numFrames;
    int[] frameImages;
    int width;
    int curFrame = 0;

    @Override
    public void onTick(Vec2 var1) {
        int var2 = var1.x / 16;
        int var3 = var1.y / 16;
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
                        var10 = frameImages[var9 + var8 * width + var5];

                        for (var21 = 0; var21 < var4; ++var21) {
                            for (var12 = 0; var12 < var4; ++var12) {
                                var6 = var9 * var4 + var21 + (var8 * var4 + var12) * var2;
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
                        var21 = 0;
                        var12 = 0;
                        var13 = 0;

                        for (var14 = 0; var14 < var4; ++var14) {
                            for (var15 = 0; var15 < var4; ++var15) {
                                var16 = frameImages[var9 * var4 + var14 + (var8 * var4 + var15) * width + var5];
                                var10 += var16 >> 16 & 255;
                                var21 += var16 >> 8 & 255;
                                var12 += var16 & 255;
                                var13 += var16 >> 24 & 255;
                            }
                        }

                        this.grid[var6 * 4 + 0] = (byte) (var10 / var4 / var4);
                        this.grid[var6 * 4 + 1] = (byte) (var21 / var4 / var4);
                        this.grid[var6 * 4 + 2] = (byte) (var12 / var4 / var4);
                        this.grid[var6 * 4 + 3] = (byte) (var13 / var4 / var4);
                        ++var6;
                    }
                }
            }

            curFrame = (curFrame + 1) % numFrames;
        } else {
            var4 = var2 * var3;
            if (this.field_1166.length != var4) {
                this.field_1166 = new float[var4];
                this.field_1167 = new float[var4];
                this.field_1168 = new float[var4];
                this.field_1169 = new float[var4];
            }

            var5 = (int) Math.sqrt((double) (var2 / 16));
            var6 = (int) Math.sqrt((double) (var3 / 16));
            float var7 = (float) ((var5 * 2 + 1) * (var6 * 2 + 1)) * 1.1F;
            var8 = (int) Math.sqrt((double) (var2 / 16));
            this.field_1170 += var8;

            float var11;
            int var17;
            for (var9 = 0; var9 < var2; ++var9) {
                for (var10 = 0; var10 < var3; ++var10) {
                    var11 = 0.0F;
                    var12 = (int) (MathHelper.sin((float) var10 * 3.141593F * 2.0F / (float) var2) * 1.2F);
                    var13 = (int) (MathHelper.sin((float) var9 * 3.141593F * 2.0F / (float) var3) * 1.2F);

                    for (var14 = var9 - var5; var14 <= var9 + var5; ++var14) {
                        for (var15 = var10 - var6; var15 <= var10 + var6; ++var15) {
                            var16 = var14 + var12 & var2 - 1;
                            var17 = var15 + var13 & var3 - 1;
                            var11 += this.field_1166[var16 + var17 * var2];
                        }
                    }

                    this.field_1167[var9 + var10 * var2] = var11 / var7 + (this.field_1168[(var9 + 0 & var2 - 1) + (var10 + 0 & var3 - 1) * var2] + this.field_1168[(var9 + 1 & var2 - 1) + (var10 + 0 & var3 - 1) * var2] + this.field_1168[(var9 + 1 & var2 - 1) + (var10 + 1 & var3 - 1) * var2] + this.field_1168[(var9 + 0 & var2 - 1) + (var10 + 1 & var3 - 1) * var2]) / 4.0F * 0.8F;
                    this.field_1168[var9 + var10 * var2] += this.field_1169[var9 + var10 * var2] * 0.01F;
                    if (this.field_1168[var9 + var10 * var2] < 0.0F) {
                        this.field_1168[var9 + var10 * var2] = 0.0F;
                    }

                    this.field_1169[var9 + var10 * var2] -= 0.06F;
                    if (Math.random() < 0.005D) {
                        this.field_1169[var9 + var10 * var2] = 1.5F;
                    }
                }
            }

            float[] var20 = this.field_1167;
            this.field_1167 = this.field_1166;
            this.field_1166 = var20;

            for (var10 = 0; var10 < var4; ++var10) {
                var11 = this.field_1166[var10 - this.field_1170 / 3 * var2 & var4 - 1] * 2.0F;
                if (var11 > 1.0F) {
                    var11 = 1.0F;
                }

                if (var11 < 0.0F) {
                    var11 = 0.0F;
                }

                var13 = (int) (var11 * 100.0F + 155.0F);
                var14 = (int) (var11 * var11 * 255.0F);
                var15 = (int) (var11 * var11 * var11 * var11 * 128.0F);
                if (this.render3d) {
                    var16 = (var13 * 30 + var14 * 59 + var15 * 11) / 100;
                    var17 = (var13 * 30 + var14 * 70) / 100;
                    int var18 = (var13 * 30 + var15 * 70) / 100;
                    var13 = var16;
                    var14 = var17;
                    var15 = var18;
                }

                this.grid[var10 * 4 + 0] = (byte) var13;
                this.grid[var10 * 4 + 1] = (byte) var14;
                this.grid[var10 * 4 + 2] = (byte) var15;
                this.grid[var10 * 4 + 3] = -1;
            }

        }
    }

    @Override
    public void loadImage(World world) {
        loadImage("/custom_lava_flowing.png", world);
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
