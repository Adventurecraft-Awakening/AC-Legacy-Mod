package dev.adventurecraft.awakening.mixin.world.gen;

import dev.adventurecraft.awakening.common.AC_TerrainImage;
import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BiomeSource.class)
public abstract class MixinBiomeSource {

    @Shadow
    private PerlinSimplexNoise temperatureNoise;
    @Shadow
    private PerlinSimplexNoise rainfallNoise;
    @Shadow
    private PerlinSimplexNoise detailNoise;
    @Shadow
    public double[] temperatureNoises;
    @Shadow
    public double[] rainfallNoises;
    @Shadow
    public double[] detailNoises;

    private Level world;

    @Inject(method = "<init>(Lnet/minecraft/world/World;)V", at = @At("TAIL"))
    private void init(Level world, CallbackInfo ci) {
        this.world = world;
    }

    @Overwrite
    public double[] getTemperatures(double[] var1, int var2, int var3, int var4, int var5) {
        if (var1 == null || var1.length < var4 * var5) {
            var1 = new double[var4 * var5];
        }

        boolean useImages = ((ExWorldProperties) this.world.levelData).getWorldGenProps().useImages;

        if (!useImages) {
            var1 = this.temperatureNoise.getRegion(var1, var2, var3, var4, var5, 0.025F, 0.025F, 0.25D);
            this.detailNoises = this.detailNoise.getRegion(this.detailNoises, var2, var3, var4, var5, 0.25D, 0.25D, 0.5882352941176471D);
        }

        int var6 = 0;

        for (int var7 = 0; var7 < var4; ++var7) {
            for (int var8 = 0; var8 < var5; ++var8) {
                if (useImages) {
                    int var9 = var2 + var7;
                    int var10 = var3 + var8;
                    var1[var6] = AC_TerrainImage.getTerrainTemperature(var9, var10);
                } else {
                    double var17 = this.detailNoises[var6] * 1.1D + 0.5D;
                    double var11 = 0.01D;
                    double var13 = 1.0D - var11;
                    double var15 = (var1[var6] * 0.15D + 0.7D) * var13 + var17 * var11;
                    var15 = 1.0D - (1.0D - var15) * (1.0D - var15);
                    if (var15 < 0.0D) {
                        var15 = 0.0D;
                    }

                    if (var15 > 1.0D) {
                        var15 = 1.0D;
                    }

                    var1[var6] = var15;
                }

                ++var6;
            }
        }

        return var1;
    }

    @Overwrite
    public Biome[] getBiomes(Biome[] var1, int var2, int var3, int var4, int var5) {
        if (var1 == null || var1.length < var4 * var5) {
            var1 = new Biome[var4 * var5];
        }

        boolean useImages = ((ExWorldProperties) this.world.levelData).getWorldGenProps().useImages;

        this.temperatureNoises = this.temperatureNoise.getRegion(this.temperatureNoises, var2, var3, var4, var4, 0.025F, 0.025F, 0.25D);
        this.rainfallNoises = this.rainfallNoise.getRegion(this.rainfallNoises, var2, var3, var4, var4, 0.05F, 0.05F, 1.0D / 3.0D);
        this.detailNoises = this.detailNoise.getRegion(this.detailNoises, var2, var3, var4, var4, 0.25D, 0.25D, 0.5882352941176471D);
        int var6 = 0;
        double var7;
        double var9;

        for (int var11 = 0; var11 < var4; ++var11) {
            for (int var12 = 0; var12 < var5; ++var12) {
                if (useImages) {
                    int var13 = var2 + var11;
                    int var14 = var3 + var12;
                    var7 = AC_TerrainImage.getTerrainTemperature(var13, var14);
                    var9 = AC_TerrainImage.getTerrainHumidity(var13, var14);
                } else {
                    double var19 = this.detailNoises[var6] * 1.1D + 0.5D;
                    double var15 = 0.01D;
                    double var17 = 1.0D - var15;
                    var7 = (this.temperatureNoises[var6] * 0.15D + 0.7D) * var17 + var19 * var15;
                    var15 = 0.002D;
                    var17 = 1.0D - var15;
                    var9 = (this.rainfallNoises[var6] * 0.15D + 0.5D) * var17 + var19 * var15;
                    var7 = 1.0D - (1.0D - var7) * (1.0D - var7);
                    if (var7 < 0.0D) {
                        var7 = 0.0D;
                    }

                    if (var9 < 0.0D) {
                        var9 = 0.0D;
                    }

                    if (var7 > 1.0D) {
                        var7 = 1.0D;
                    }

                    if (var9 > 1.0D) {
                        var9 = 1.0D;
                    }
                }

                this.temperatureNoises[var6] = var7;
                this.rainfallNoises[var6] = var9;
                var1[var6++] = Biome.getBiome(var7, var9);
            }
        }

        return var1;
    }
}

