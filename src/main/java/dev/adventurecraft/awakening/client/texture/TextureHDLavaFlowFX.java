package dev.adventurecraft.awakening.client.texture;

import dev.adventurecraft.awakening.client.options.Config;
import net.minecraft.client.render.FlowingLavaTextureBinder2;
import net.minecraft.client.resource.TexturePack;
import net.minecraft.util.math.MathHelper;

public class TextureHDLavaFlowFX extends FlowingLavaTextureBinder2 implements TextureHDFX {
    private TexturePack texturePackBase;
    private int tileWidth;
    protected float[] buf1;
    protected float[] buf2;
    protected float[] buf3;
    protected float[] buf4;
    int tickCounter;

    public TextureHDLavaFlowFX() {
        this.tileWidth = 16;
        this.grid = new byte[this.tileWidth * this.tileWidth * 4];
        this.buf1 = new float[this.tileWidth * this.tileWidth];
        this.buf2 = new float[this.tileWidth * this.tileWidth];
        this.buf3 = new float[this.tileWidth * this.tileWidth];
        this.buf4 = new float[this.tileWidth * this.tileWidth];
        this.tickCounter = 0;
        this.textureSize = 2;
    }

    public void setTileWidth(int var1) {
        if (var1 > Config.getMaxDynamicTileWidth()) {
            var1 = Config.getMaxDynamicTileWidth();
        }

        this.tileWidth = var1;
        this.grid = new byte[var1 * var1 * 4];
        this.buf1 = new float[var1 * var1];
        this.buf2 = new float[var1 * var1];
        this.buf3 = new float[var1 * var1];
        this.buf4 = new float[var1 * var1];
    }

    public void setTexturePackBase(TexturePack var1) {
        this.texturePackBase = var1;
    }

    @Override
    public void updateTexture() {
        if (!Config.isAnimatedLava()) {
            this.grid = null;
        }

        if (this.grid != null) {
            ++this.tickCounter;
            int var1 = this.tileWidth - 1;

            int var3;
            int var7;
            int var8;
            int var9;
            int var10;
            for (int var2 = 0; var2 < this.tileWidth; ++var2) {
                for (var3 = 0; var3 < this.tileWidth; ++var3) {
                    float var4 = 0.0F;
                    int var5 = (int) (MathHelper.sin((float) var3 * 3.141593F * 2.0F / 16.0F) * 1.2F);
                    int var6 = (int) (MathHelper.sin((float) var2 * 3.141593F * 2.0F / 16.0F) * 1.2F);

                    for (var7 = var2 - 1; var7 <= var2 + 1; ++var7) {
                        for (var8 = var3 - 1; var8 <= var3 + 1; ++var8) {
                            var9 = var7 + var5 & var1;
                            var10 = var8 + var6 & var1;
                            var4 += this.buf1[var9 + var10 * this.tileWidth];
                        }
                    }

                    this.buf2[var2 + var3 * this.tileWidth] = var4 / 10.0F + (this.buf3[(var2 & var1) + (var3 & var1) * this.tileWidth] + this.buf3[(var2 + 1 & var1) + (var3 & var1) * this.tileWidth] + this.buf3[(var2 + 1 & var1) + (var3 + 1 & var1) * this.tileWidth] + this.buf3[(var2 & var1) + (var3 + 1 & var1) * this.tileWidth]) / 4.0F * 0.8F;
                    this.buf3[var2 + var3 * this.tileWidth] += this.buf4[var2 + var3 * this.tileWidth] * 0.01F;
                    if (this.buf3[var2 + var3 * this.tileWidth] < 0.0F) {
                        this.buf3[var2 + var3 * this.tileWidth] = 0.0F;
                    }

                    this.buf4[var2 + var3 * this.tileWidth] -= 0.06F;
                    if (Math.random() < 0.005D) {
                        this.buf4[var2 + var3 * this.tileWidth] = 1.5F;
                    }
                }
            }

            float[] var13 = this.buf2;
            this.buf2 = this.buf1;
            this.buf1 = var13;
            var3 = this.tileWidth * this.tileWidth - 1;

            for (int var14 = 0; var14 < this.tileWidth * this.tileWidth; ++var14) {
                float var15 = this.buf1[var14 - this.tickCounter / 3 * this.tileWidth & var3] * 2.0F;
                if (var15 > 1.0F) {
                    var15 = 1.0F;
                }

                if (var15 < 0.0F) {
                    var15 = 0.0F;
                }

                var7 = (int) (var15 * 100.0F + 155.0F);
                var8 = (int) (var15 * var15 * 255.0F);
                var9 = (int) (var15 * var15 * var15 * var15 * 128.0F);
                if (this.render3d) {
                    var10 = (var7 * 30 + var8 * 59 + var9 * 11) / 100;
                    int var11 = (var7 * 30 + var8 * 70) / 100;
                    int var12 = (var7 * 30 + var9 * 70) / 100;
                    var7 = var10;
                    var8 = var11;
                    var9 = var12;
                }

                this.grid[var14 * 4] = (byte) var7;
                this.grid[var14 * 4 + 1] = (byte) var8;
                this.grid[var14 * 4 + 2] = (byte) var9;
                this.grid[var14 * 4 + 3] = -1;
            }

        }
    }
}
