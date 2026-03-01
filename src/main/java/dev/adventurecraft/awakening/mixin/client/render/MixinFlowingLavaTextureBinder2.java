package dev.adventurecraft.awakening.mixin.client.render;

import dev.adventurecraft.awakening.image.Rgba;
import dev.adventurecraft.awakening.layout.Size;
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
    public void onTick(Size size) {
        int var2 = size.w / 16;
        int var3 = size.h / 16;
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

            var5 = (int) Math.sqrt((double) (var2 / 16));
            var6 = (int) Math.sqrt((double) (var3 / 16));
            float var7 = (float) ((var5 * 2 + 1) * (var6 * 2 + 1)) * 1.1F;
            var8 = (int) Math.sqrt((double) (var2 / 16));
            this.ticks += var8;

            float var11;
            int var17;
            for (var9 = 0; var9 < var2; ++var9) {
                var13 = (int) (Mth.sin((float) var9 * 3.141593F * 2.0F / (float) var3) * 1.2F);

                for (var10 = 0; var10 < var3; ++var10) {
                    var11 = 0.0F;
                    var12 = (int) (Mth.sin((float) var10 * 3.141593F * 2.0F / (float) var2) * 1.2F);

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

            var imageData = this.imageData.asIntBuffer();
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

                int color = Rgba.fromRgb8(var13, var14, var15);
                imageData.put(var10, color);
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
