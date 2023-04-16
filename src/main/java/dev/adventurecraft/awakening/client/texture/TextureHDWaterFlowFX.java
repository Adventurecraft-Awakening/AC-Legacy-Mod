package dev.adventurecraft.awakening.client.texture;

import dev.adventurecraft.awakening.client.options.Config;
import net.minecraft.block.Block;
import net.minecraft.client.render.TextureBinder;
import net.minecraft.client.resource.TexturePack;

public class TextureHDWaterFlowFX extends TextureBinder implements TextureHDFX {
    private TexturePack texturePackBase;
    private int tileWidth;
    protected float[] buf1;
    protected float[] buf2;
    protected float[] buf3;
    protected float[] buf4;
    private int tickCounter;

    public TextureHDWaterFlowFX() {
        super(Block.FLOWING_WATER.texture + 1);
        this.tileWidth = 16;
        this.grid = new byte[this.tileWidth * this.tileWidth * 4];
        this.buf1 = new float[this.tileWidth * this.tileWidth];
        this.buf2 = new float[this.tileWidth * this.tileWidth];
        this.buf3 = new float[this.tileWidth * this.tileWidth];
        this.buf4 = new float[this.tileWidth * this.tileWidth];
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
        this.tickCounter = 0;
    }

    public void setTexturePackBase(TexturePack var1) {
        this.texturePackBase = var1;
    }

    @Override
    public void updateTexture() {
        if (!Config.isAnimatedWater()) {
            this.grid = null;
        }

        if (this.grid != null) {
            ++this.tickCounter;
            int var1 = this.tileWidth - 1;

            int var2;
            int var3;
            int var7;
            for (var2 = 0; var2 < this.tileWidth; ++var2) {
                for (var3 = 0; var3 < this.tileWidth; ++var3) {
                    float var4 = 0.0F;

                    for (int var5 = var3 - 2; var5 <= var3; ++var5) {
                        int var6 = var2 & var1;
                        var7 = var5 & var1;
                        var4 += this.buf1[var6 + var7 * this.tileWidth];
                    }

                    this.buf2[var2 + var3 * this.tileWidth] = var4 / 3.2F + this.buf3[var2 + var3 * this.tileWidth] * 0.8F;
                }
            }

            for (var2 = 0; var2 < this.tileWidth; ++var2) {
                for (var3 = 0; var3 < this.tileWidth; ++var3) {
                    this.buf3[var2 + var3 * this.tileWidth] += this.buf4[var2 + var3 * this.tileWidth] * 0.05F;
                    if (this.buf3[var2 + var3 * this.tileWidth] < 0.0F) {
                        this.buf3[var2 + var3 * this.tileWidth] = 0.0F;
                    }

                    this.buf4[var2 + var3 * this.tileWidth] -= 0.3F;
                    if (Math.random() < 0.2D) {
                        this.buf4[var2 + var3 * this.tileWidth] = 0.5F;
                    }
                }
            }

            float[] var14 = this.buf2;
            this.buf2 = this.buf1;
            this.buf1 = var14;
            var3 = this.tileWidth * this.tileWidth - 1;

            for (int var15 = 0; var15 < this.tileWidth * this.tileWidth; ++var15) {
                float var16 = this.buf1[var15 - this.tickCounter * this.tileWidth & var3];
                if (var16 > 1.0F) {
                    var16 = 1.0F;
                }

                if (var16 < 0.0F) {
                    var16 = 0.0F;
                }

                float var17 = var16 * var16;
                var7 = (int) (32.0F + var17 * 32.0F);
                int var8 = (int) (50.0F + var17 * 64.0F);
                int var9 = 255;
                int var10 = (int) (146.0F + var17 * 50.0F);
                if (this.render3d) {
                    int var11 = (var7 * 30 + var8 * 59 + var9 * 11) / 100;
                    int var12 = (var7 * 30 + var8 * 70) / 100;
                    int var13 = (var7 * 30 + var9 * 70) / 100;
                    var7 = var11;
                    var8 = var12;
                    var9 = var13;
                }

                this.grid[var15 * 4] = (byte) var7;
                this.grid[var15 * 4 + 1] = (byte) var8;
                this.grid[var15 * 4 + 2] = (byte) var9;
                this.grid[var15 * 4 + 3] = (byte) var10;
            }

        }
    }
}
