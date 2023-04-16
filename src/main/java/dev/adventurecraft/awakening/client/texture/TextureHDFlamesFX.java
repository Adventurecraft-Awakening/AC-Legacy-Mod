package dev.adventurecraft.awakening.client.texture;

import java.util.Random;

import dev.adventurecraft.awakening.client.options.Config;
import net.minecraft.block.Block;
import net.minecraft.client.render.TextureBinder;
import net.minecraft.client.resource.TexturePack;

public class TextureHDFlamesFX extends TextureBinder implements TextureHDFX {
    private int tileWidth;
    private int fireHeight;
    protected float[] buf1;
    protected float[] buf2;
    private final Random random = new Random();

    public TextureHDFlamesFX(int var1) {
        super(Block.FIRE.texture + var1 * 16);
        this.tileWidth = 16;
        this.fireHeight = this.tileWidth + this.tileWidth / 4;
        this.grid = new byte[this.tileWidth * this.tileWidth * 4];
        this.buf1 = new float[this.tileWidth * this.fireHeight];
        this.buf2 = new float[this.tileWidth * this.fireHeight];
    }

    public void setTileWidth(int var1) {
        if (var1 > Config.getMaxDynamicTileWidth()) {
            var1 = Config.getMaxDynamicTileWidth();
        }

        this.tileWidth = var1;
        this.fireHeight = var1 + var1 / 4;
        this.grid = new byte[var1 * var1 * 4];
        this.buf1 = new float[var1 * this.fireHeight];
        this.buf2 = new float[var1 * this.fireHeight];
    }

    public void setTexturePackBase(TexturePack var1) {
    }

    @Override
    public void updateTexture() {
        if (!Config.isAnimatedFire()) {
            this.grid = null;
        }

        if (this.grid != null) {
            float var1 = 1.01F + 0.8F / (float) this.tileWidth;
            float var2 = 3.0F + (float) this.tileWidth / 16.0F;

            int var4;
            int var5;
            float var6;
            int var8;
            for (int var3 = 0; var3 < this.tileWidth; ++var3) {
                for (var4 = 0; var4 < this.fireHeight; ++var4) {
                    var5 = this.fireHeight - this.tileWidth / 8;
                    var6 = this.buf1[var3 + (var4 + 1) % this.fireHeight * this.tileWidth] * (float) var5;

                    for (int var7 = var3 - 1; var7 <= var3 + 1; ++var7) {
                        for (var8 = var4; var8 <= var4 + 1; ++var8) {
                            if (var7 >= 0 && var8 >= 0 && var7 < this.tileWidth && var8 < this.fireHeight) {
                                var6 += this.buf1[var7 + var8 * this.tileWidth];
                            }

                            ++var5;
                        }
                    }

                    this.buf2[var3 + var4 * this.tileWidth] = var6 / ((float) var5 * var1);
                    if (var4 >= this.fireHeight - this.tileWidth / 16) {
                        this.buf2[var3 + var4 * this.tileWidth] = this.random.nextFloat() * this.random.nextFloat() * this.random.nextFloat() * var2 + this.random.nextFloat() * 0.1F + 0.2F;
                    }
                }
            }

            float[] var15 = this.buf2;
            this.buf2 = this.buf1;
            this.buf1 = var15;
            var4 = this.tileWidth * this.tileWidth;

            for (var5 = 0; var5 < var4; ++var5) {
                var6 = this.buf1[var5] * 1.8F;
                if (var6 > 1.0F) {
                    var6 = 1.0F;
                }

                if (var6 < 0.0F) {
                    var6 = 0.0F;
                }

                var8 = (int) (var6 * 155.0F + 100.0F);
                int var9 = (int) (var6 * var6 * 255.0F);
                int var10 = (int) (var6 * var6 * var6 * var6 * var6 * var6 * var6 * var6 * var6 * var6 * 255.0F);
                short var11 = 255;
                if (var6 < 0.5F) {
                    var11 = 0;
                }

                float var16 = (var6 - 0.5F) * 2.0F;
                int var12;
                if (this.render3d) {
                    var12 = (var8 * 30 + var9 * 59 + var10 * 11) / 100;
                    int var13 = (var8 * 30 + var9 * 70) / 100;
                    int var14 = (var8 * 30 + var10 * 70) / 100;
                    var8 = var12;
                    var9 = var13;
                    var10 = var14;
                }

                var12 = var5 * 4;
                this.grid[var12] = (byte) var8;
                this.grid[var12 + 1] = (byte) var9;
                this.grid[var12 + 2] = (byte) var10;
                this.grid[var12 + 3] = (byte) var11;
            }

        }
    }
}
