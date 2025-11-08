package dev.adventurecraft.awakening.mixin.client.render;

import dev.adventurecraft.awakening.image.Rgba;
import dev.adventurecraft.awakening.layout.Size;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.nio.IntBuffer;
import java.util.Random;

import net.minecraft.client.renderer.ptexture.PortalTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(PortalTexture.class)
public class MixinPortalTextureBinder extends MixinTextureBinder {

    @Shadow
    private int time;

    @Shadow
    private byte[][] frames = new byte[0][];

    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 32))
    private int disableInit(int constant) {
        return 0;
    }

    @Unique
    private IntBuffer generatePortalData(int width, int height, int frameCount) {
        var rawImageData = this.allocImageData(width, height, frameCount);
        var imageData = rawImageData;
        Random var3 = new Random(100L);

        for (int frameIdx = 0; frameIdx < frameCount; ++frameIdx) {
            int frameOffset = frameIdx * width * height;

            for (int y = 0; y < height; ++y) {
                for (int x = 0; x < width; ++x) {
                    float var7 = 0.0F;

                    int var8;
                    for (var8 = 0; var8 < 2; ++var8) {
                        float var9 = (float) (var8 * width / 2);
                        float var10 = (float) (var8 * height / 2);
                        float var11 = ((float) x - var9) / (float) width * 2.0F;
                        float var12 = ((float) y - var10) / (float) height * 2.0F;
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
                        float var14 = (float) Math.atan2((double) var12, (double) var11) + ((float) frameIdx / 32.0F * 3.141593F * 2.0F - var13 * 10.0F + (float) (var8 * 2)) * (float) (var8 * 2 - 1);
                        var14 = (Mth.sin(var14) + 1.0F) / 2.0F;
                        var14 /= var13 + 1.0F;
                        var7 += var14 * 0.5F;
                    }

                    var7 += var3.nextFloat() * 0.1F;
                    var8 = (int) (var7 * 100.0F + 155.0F);
                    int var15 = (int) (var7 * var7 * 200.0F + 55.0F);
                    int var16 = (int) (var7 * var7 * var7 * var7 * 255.0F);
                    int var17 = (int) (var7 * 100.0F + 155.0F);
                    int var18 = frameOffset + y * width + x;
                    int color = Rgba.fromRgba8(var15, var16, var8, var17);
                    imageData.put(var18, color);
                }
            }
        }
        return rawImageData;
    }

    @Override
    public void onTick(Size size) {
        int var2 = size.w / 16;
        int var3 = size.h / 16;

        if (hasImages) {
        } else {
            int var4 = var2 * var3;
            if (this.imageData.capacity() != var4 * 32) {
                this.numFrames = 32;
                this.imageData = this.generatePortalData(var2, var3, this.numFrames);
            }
        }
        this.animate();
    }

    @Override
    public void loadImage(String name, Level world) {
        if (name == null) {
            name = "/custom_portal.png";
        }

        super.loadImage(name, world);
    }
}

