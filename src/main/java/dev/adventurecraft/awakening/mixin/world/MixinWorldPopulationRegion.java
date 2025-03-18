package dev.adventurecraft.awakening.mixin.world;

import dev.adventurecraft.awakening.tile.AC_BlockStairMulti;
import dev.adventurecraft.awakening.common.AC_LightCache;
import dev.adventurecraft.awakening.common.AC_PlayerTorch;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Region;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.tile.Tile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Region.class)
public abstract class MixinWorldPopulationRegion {

    @Shadow
    private Level level;

    @Shadow
    public abstract int getRawBrightness(int i, int j, int k);

    @Shadow
    public abstract int getTile(int i, int j, int k);

    @Shadow
    private int xc1;

    @Shadow
    private int zc1;

    @Shadow
    private LevelChunk[][] chunks;

    @Environment(EnvType.CLIENT)
    @Overwrite
    public float getBrightness(int var1, int var2, int var3, int var4) {
        float var5 = AC_LightCache.cache.getLightValue(var1, var2, var3);
        if (var5 >= 0.0F) {
            return var5;
        }

        int var6 = this.getRawBrightness(var1, var2, var3);
        if (var6 < var4) {
            var6 = var4;
        }

        float var7 = AC_PlayerTorch.getTorchLight(this.level, var1, var2, var3);
        if ((float) var6 < var7) {
            int var8 = (int) Math.floor(var7);
            if (var8 == 15) {
                return this.level.dimension.brightnessRamp[15];
            } else {
                int var9 = (int) Math.ceil(var7);
                float var10 = var7 - (float) var8;
                return (1.0F - var10) * this.level.dimension.brightnessRamp[var8] + var10 * this.level.dimension.brightnessRamp[var9];
            }
        } else {
            var5 = this.level.dimension.brightnessRamp[var6];
            AC_LightCache.cache.setLightValue(var1, var2, var3, var5);
            return var5;
        }
    }

    @Environment(EnvType.CLIENT)
    @Overwrite
    public float getBrightness(int var1, int var2, int var3) {
        float var4 = AC_LightCache.cache.getLightValue(var1, var2, var3);
        if (var4 >= 0.0F) {
            return var4;
        }

        int var5 = this.getRawBrightness(var1, var2, var3);
        float var6 = AC_PlayerTorch.getTorchLight(this.level, var1, var2, var3);
        if ((float) var5 < var6) {
            int var7 = (int) Math.floor(var6);
            if (var7 == 15) {
                return this.level.dimension.brightnessRamp[15];
            } else {
                int var8 = (int) Math.ceil(var6);
                float var9 = var6 - (float) var7;
                return (1.0F - var9) * this.level.dimension.brightnessRamp[var7] + var9 * this.level.dimension.brightnessRamp[var8];
            }
        } else {
            var4 = this.level.dimension.brightnessRamp[var5];
            AC_LightCache.cache.setLightValue(var1, var2, var3, var4);
            return var4;
        }
    }

    @Environment(EnvType.CLIENT)
    @Overwrite
    public int getRawBrightness(int var1, int var2, int var3, boolean var4) {
        if (var1 < -32000000 || var3 < -32000000 || var1 >= 32000000 || var3 > 32000000) {
            return 15;
        }

        if (var4) {
            int var5 = this.getTile(var1, var2, var3);
            if (var5 != 0) {
                if (var5 == Tile.SLAB.id ||
                    var5 == Tile.FARMLAND.id ||
                    var5 == Tile.WOOD_STAIRS.id ||
                    var5 == Tile.COBBLESTONE_STAIRS.id ||
                    Tile.tiles[var5] instanceof AC_BlockStairMulti) {

                    int var6 = this.getRawBrightness(var1, var2 + 1, var3, false);
                    int var7 = this.getRawBrightness(var1 + 1, var2, var3, false);
                    int var8 = this.getRawBrightness(var1 - 1, var2, var3, false);
                    int var9 = this.getRawBrightness(var1, var2, var3 + 1, false);
                    int var10 = this.getRawBrightness(var1, var2, var3 - 1, false);
                    if (var7 > var6) {
                        var6 = var7;
                    }

                    if (var8 > var6) {
                        var6 = var8;
                    }

                    if (var9 > var6) {
                        var6 = var9;
                    }

                    if (var10 > var6) {
                        var6 = var10;
                    }

                    return var6;
                }
            }
        }

        if (var2 < 0) {
            return 0;
        } else if (var2 >= 128) {
            int var5 = 15 - this.level.skyDarken;
            if (var5 < 0) {
                var5 = 0;
            }
            return var5;
        } else {
            int var5 = (var1 >> 4) - this.xc1;
            int var6 = (var3 >> 4) - this.zc1;
            return this.chunks[var5][var6].getRawBrightness(var1 & 15, var2, var3 & 15, this.level.skyDarken);
        }
    }
}
