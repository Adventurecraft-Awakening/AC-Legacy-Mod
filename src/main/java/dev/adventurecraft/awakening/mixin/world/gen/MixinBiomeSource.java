package dev.adventurecraft.awakening.mixin.world.gen;

import dev.adventurecraft.awakening.common.AC_TerrainImage;
import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import dev.adventurecraft.awakening.util.MathF;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BiomeSource.class)
public abstract class MixinBiomeSource {

    @Shadow private PerlinSimplexNoise temperatureMap;
    @Shadow private PerlinSimplexNoise downfallMap;
    @Shadow private PerlinSimplexNoise noiseMap;
    @Shadow public double[] temperatures;
    @Shadow public double[] downfalls;
    @Shadow public double[] noises;

    private @Unique Level world;

    @Inject(
        method = "<init>(Lnet/minecraft/world/level/Level;)V",
        at = @At("TAIL")
    )
    private void init(Level world, CallbackInfo ci) {
        this.world = world;
    }

    @Overwrite
    public double[] getTemperatureBlock(double[] temps, int x, int z, int width, int depth) {
        if (temps == null || temps.length < width * depth) {
            temps = new double[width * depth];
        }

        boolean useImages = ((ExWorldProperties) this.world.levelData).getWorldGenProps().useImages;

        if (!useImages) {
            temps = this.temperatureMap.getRegion(temps, x, z, width, depth, 0.025F, 0.025F, 0.25D);
            this.noises = this.noiseMap.getRegion(this.noises, x, z, width, depth, 0.25D, 0.25D, 0.5882352941176471D);
        }

        int idx = 0;

        for (int iX = 0; iX < width; ++iX) {
            for (int iZ = 0; iZ < depth; ++iZ) {
                double temp;
                if (useImages) {
                    int infoX = x + iX;
                    int infoZ = z + iZ;
                    temp = AC_TerrainImage.getTerrainTemperature(infoX, infoZ);
                }
                else {
                    double n0 = this.noises[idx] * 1.1D + 0.5D;
                    double n1 = 0.01D;
                    double n2 = 1.0D - n1;
                    temp = (temps[idx] * 0.15D + 0.7D) * n2 + n0 * n1;
                    temp = 1.0D - (1.0D - temp) * (1.0D - temp);
                    temp = MathF.clamp(temp, 0.0D, 1.0D);
                }

                temps[idx] = temp;
                ++idx;
            }
        }

        return temps;
    }

    @Overwrite
    public Biome[] getBiomeBlock(Biome[] biomes, int x, int z, int width, int depth) {
        if (biomes == null || biomes.length < width * depth) {
            biomes = new Biome[width * depth];
        }

        boolean useImages = ((ExWorldProperties) this.world.levelData).getWorldGenProps().useImages;

        this.temperatures = this.temperatureMap.getRegion(this.temperatures, x, z, width, width, 0.025F, 0.025F, 0.25D);
        this.downfalls = this.downfallMap.getRegion(this.downfalls, x, z, width, width, 0.05F, 0.05F, 1.0D / 3.0D);
        this.noises = this.noiseMap.getRegion(this.noises, x, z, width, width, 0.25D, 0.25D, 0.5882352941176471D);
        int idx = 0;

        for (int iX = 0; iX < width; ++iX) {
            for (int iZ = 0; iZ < depth; ++iZ) {
                double temp;
                double humidity;

                if (useImages) {
                    int infoX = x + iX;
                    int infoZ = z + iZ;
                    temp = AC_TerrainImage.getTerrainTemperature(infoX, infoZ);
                    humidity = AC_TerrainImage.getTerrainHumidity(infoX, infoZ);
                }
                else {
                    double n0 = this.noises[idx] * 1.1D + 0.5D;
                    double n1 = 0.01D;
                    double n2 = 1.0D - n1;
                    temp = (this.temperatures[idx] * 0.15D + 0.7D) * n2 + n0 * n1;
                    n1 = 0.002D;
                    n2 = 1.0D - n1;

                    humidity = (this.downfalls[idx] * 0.15D + 0.5D) * n2 + n0 * n1;
                    humidity = MathF.clamp(humidity, 0.0D, 1.0D);

                    temp = 1.0D - (1.0D - temp) * (1.0D - temp);
                    temp = MathF.clamp(temp, 0.0D, 1.0D);
                }

                this.temperatures[idx] = temp;
                this.downfalls[idx] = humidity;
                biomes[idx++] = Biome.getBiome(temp, humidity);
            }
        }

        return biomes;
    }
}

