package dev.adventurecraft.awakening.mixin.client.render;

import java.awt.image.BufferedImage;

import dev.adventurecraft.awakening.client.render.AC_TextureBinder;
import dev.adventurecraft.awakening.common.Vec2;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.FlowingLavaTextureBinder;
import net.minecraft.client.render.TextureBinder;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(FlowingLavaTextureBinder.class)
public abstract class MixinFlowingLavaTextureBinder extends TextureBinder implements AC_TextureBinder {

    @Shadow
    protected float[] field_2701;

    @Shadow
    protected float[] field_2702;

    @Shadow
    protected float[] field_2703;

    @Shadow
    protected float[] field_2704;

    boolean hasImages;
    int numFrames;
    int[] frameImages;
    int width;
    int curFrame = 0;

    public MixinFlowingLavaTextureBinder() {
        super(Block.FLOWING_LAVA.texture);
    }

    @Override
    public void onTick(Vec2 var1) {
        int var2 = var1.x / 16;
        int var3 = var1.y / 16;
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
                        var20 = frameImages[var9 + var8 * width + var5];

                        for (var11 = 0; var11 < var4; ++var11) {
                            for (var12 = 0; var12 < var4; ++var12) {
                                var6 = var9 * var4 + var11 + (var8 * var4 + var12) * var2;
                                this.grid[var6 * 4 + 0] = (byte) (var20 >> 16 & 255);
                                this.grid[var6 * 4 + 1] = (byte) (var20 >> 8 & 255);
                                this.grid[var6 * 4 + 2] = (byte) (var20 & 255);
                                this.grid[var6 * 4 + 3] = (byte) (var20 >> 24 & 255);
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
                                var16 = frameImages[var9 * var4 + var14 + (var8 * var4 + var15) * width + var5];
                                var20 += var16 >> 16 & 255;
                                var11 += var16 >> 8 & 255;
                                var12 += var16 & 255;
                                var13 += var16 >> 24 & 255;
                            }
                        }

                        this.grid[var6 * 4 + 0] = (byte) (var20 / var4 / var4);
                        this.grid[var6 * 4 + 1] = (byte) (var11 / var4 / var4);
                        this.grid[var6 * 4 + 2] = (byte) (var12 / var4 / var4);
                        this.grid[var6 * 4 + 3] = (byte) (var13 / var4 / var4);
                        ++var6;
                    }
                }
            }

            curFrame = (curFrame + 1) % numFrames;
        } else {
            var4 = var2 * var3;
            if (this.field_2701.length != var4) {
                this.field_2701 = new float[var4];
                this.field_2702 = new float[var4];
                this.field_2703 = new float[var4];
                this.field_2704 = new float[var4];
            }

            var5 = (int) Math.sqrt((double) (var2 / 16));
            var6 = (int) Math.sqrt((double) (var3 / 16));
            float var7 = (float) ((var5 * 2 + 1) * (var6 * 2 + 1)) * 1.1F;

            float var10;
            for (var8 = 0; var8 < var2; ++var8) {
                for (var9 = 0; var9 < var3; ++var9) {
                    var10 = 0.0F;
                    var11 = (int) (MathHelper.sin((float) var9 * 3.141593F * 2.0F / (float) var2) * 1.2F);
                    var12 = (int) (MathHelper.sin((float) var8 * 3.141593F * 2.0F / (float) var3) * 1.2F);

                    for (var13 = var8 - var5; var13 <= var8 + var5; ++var13) {
                        for (var14 = var9 - var6; var14 <= var9 + var6; ++var14) {
                            var15 = var13 + var11 & var2 - 1;
                            var16 = var14 + var12 & var3 - 1;
                            var10 += this.field_2701[var15 + var16 * var2];
                        }
                    }

                    this.field_2702[var8 + var9 * var2] = var10 / var7 + (this.field_2703[(var8 + 0 & var2 - 1) + (var9 + 0 & var3 - 1) * var2] + this.field_2703[(var8 + 1 & var2 - 1) + (var9 + 0 & var3 - 1) * var2] + this.field_2703[(var8 + 1 & var2 - 1) + (var9 + 1 & var3 - 1) * var2] + this.field_2703[(var8 + 0 & var2 - 1) + (var9 + 1 & var3 - 1) * var2]) / 4.0F * 0.8F;
                    this.field_2703[var8 + var9 * var2] += this.field_2704[var8 + var9 * var2] * 0.01F;
                    if (this.field_2703[var8 + var9 * var2] < 0.0F) {
                        this.field_2703[var8 + var9 * var2] = 0.0F;
                    }

                    this.field_2704[var8 + var9 * var2] -= 0.06F;
                    if (Math.random() < 0.005D) {
                        this.field_2704[var8 + var9 * var2] = 1.5F;
                    }
                }
            }

            float[] var19 = this.field_2702;
            this.field_2702 = this.field_2701;
            this.field_2701 = var19;

            for (var9 = 0; var9 < var4; ++var9) {
                var10 = this.field_2701[var9] * 2.0F;
                if (var10 > 1.0F) {
                    var10 = 1.0F;
                }

                if (var10 < 0.0F) {
                    var10 = 0.0F;
                }

                var12 = (int) (var10 * 100.0F + 155.0F);
                var13 = (int) (var10 * var10 * 255.0F);
                var14 = (int) (var10 * var10 * var10 * var10 * 128.0F);
                if (this.render3d) {
                    var15 = (var12 * 30 + var13 * 59 + var14 * 11) / 100;
                    var16 = (var12 * 30 + var13 * 70) / 100;
                    int var17 = (var12 * 30 + var14 * 70) / 100;
                    var12 = var15;
                    var13 = var16;
                    var14 = var17;
                }

                this.grid[var9 * 4 + 0] = (byte) var12;
                this.grid[var9 * 4 + 1] = (byte) var13;
                this.grid[var9 * 4 + 2] = (byte) var14;
                this.grid[var9 * 4 + 3] = -1;
            }

        }
    }

    @Override
    public void loadImage() {
        loadImage("/custom_lava_still.png");
    }

    @Override
    public void loadImage(String name) {
        BufferedImage var1 = null;
        if (Minecraft.instance.world != null) {
            var1 = ((ExWorld) Minecraft.instance.world).loadMapTexture(name);
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
