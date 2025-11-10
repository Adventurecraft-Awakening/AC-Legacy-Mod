package dev.adventurecraft.awakening.mixin.world.gen;

import dev.adventurecraft.awakening.common.AC_TerrainImage;
import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import dev.adventurecraft.awakening.util.MathF;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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
    @Shadow public Biome[] biomes;

    @Unique private Level world;
    @Unique private int prevX;
    @Unique private int prevZ;
    @Unique private int prevW;
    @Unique private int prevD;

    @Inject(
        method = "<init>(Lnet/minecraft/world/level/Level;)V",
        at = @At("TAIL")
    )
    private void init(Level world, CallbackInfo ci) {
        this.world = world;
    }

    @Environment(value = EnvType.CLIENT)
    @Overwrite
    public double getTemperature(int x, int z) {
        if (this.needsBiomeCacheUpgrade(x, z, 1, 1)) {
            this.temperatures = this.temperatureMap.getRegion(this.temperatures, x, z, 1, 1, 0.025f, 0.025f, 0.5);
            this.invalidateBiomeCache();
        }
        return this.temperatures[0];
    }

    @Overwrite
    public double[] getTemperatureBlock(double[] temps, int x, int z, int w, int d) {
        boolean useImages = ((ExWorldProperties) this.world.levelData).getWorldGenProps().useImages;
        if (useImages) {
            return this.getTemperatureBlockFromImage(temps, x, z, w, d);
        }
        return this.getTemperatureBlockFromRegion(temps, x, z, w, d);
    }

    @Unique
    private double[] getTemperatureBlockFromRegion(double[] temps, int x, int z, int w, int d) {
        if (temps == null || temps.length < w * d) {
            temps = new double[w * d];
        }

        double[] noise = this.noises;
        temps = this.temperatureMap.getRegion(temps, x, z, w, d, 0.025F, 0.025F, 0.25D);
        noise = this.noiseMap.getRegion(noise, x, z, w, d, 0.25D, 0.25D, 0.5882352941176471D);
        this.noises = noise;

        for (int iX = 0; iX < w; ++iX) {
            for (int iZ = 0; iZ < d; ++iZ) {
                final int idx = iZ * w + iX;
                double temp;

                double n0 = noise[idx] * 1.1D + 0.5D;
                double n1 = 0.01D;
                double n2 = 1.0D - n1;
                temp = (temps[idx] * 0.15D + 0.7D) * n2 + n0 * n1;
                temp = 1.0D - (1.0D - temp) * (1.0D - temp);
                temp = MathF.saturate(temp);

                temps[idx] = temp;
            }
        }
        return temps;
    }

    @Unique
    private double[] getTemperatureBlockFromImage(double[] temps, int x, int z, int w, int d) {
        if (temps == null || temps.length < w * d) {
            temps = new double[w * d];
        }

        for (int iX = 0; iX < w; ++iX) {
            for (int iZ = 0; iZ < d; ++iZ) {
                final int idx = iZ * w + iX;

                int infoX = x + iX;
                int infoZ = z + iZ;
                temps[idx] = AC_TerrainImage.getTerrainTemperature(infoX, infoZ);
            }
        }
        return temps;
    }

    @Overwrite
    public Biome[] getBiomeBlock(int x, int z, int w, int d) {
        if (this.needsBiomeCacheUpgrade(x, z, w, d)) {
            this.biomes = this.getBiomeBlock(this.biomes, x, z, w, d);
            this.upgradeBiomeCache(x, z, w, d);
        }
        return this.biomes;
    }

    @Overwrite
    public Biome[] getBiomeBlock(Biome[] biomes, int x, int z, int w, int d) {
        boolean useImages = ((ExWorldProperties) this.world.levelData).getWorldGenProps().useImages;
        if (useImages) {
            return this.getBiomeBlockFromImage(biomes, x, z, w, d);
        }
        return this.getBiomeBlockFromRegion(biomes, x, z, w, d);
    }

    @Unique
    private Biome[] getBiomeBlockFromRegion(Biome[] biomes, int x, int z, int w, int d) {
        if (biomes == null || biomes.length < w * d) {
            biomes = new Biome[w * d];
        }

        double[] temps = this.temperatures;
        double[] humids = this.downfalls;
        double[] noise = this.noises;
        temps = this.temperatureMap.getRegion(temps, x, z, w, d, 0.025F, 0.025F, 0.25D);
        humids = this.downfallMap.getRegion(humids, x, z, w, d, 0.05F, 0.05F, 1.0D / 3.0D);
        noise = this.noiseMap.getRegion(noise, x, z, w, d, 0.25D, 0.25D, 0.5882352941176471D);
        this.temperatures = temps;
        this.downfalls = humids;
        this.noises = noise;

        for (int iX = 0; iX < w; ++iX) {
            for (int iZ = 0; iZ < d; ++iZ) {
                final int idx = iZ * w + iX;
                double temp;
                double humidity;

                double n0 = noise[idx] * 1.1D + 0.5D;
                double n1 = 0.01D;
                double n2 = 1.0D - n1;
                temp = (temps[idx] * 0.15D + 0.7D) * n2 + n0 * n1;
                n1 = 0.002D;
                n2 = 1.0D - n1;

                humidity = (humids[idx] * 0.15D + 0.5D) * n2 + n0 * n1;
                humidity = MathF.saturate(humidity);

                temp = 1.0D - (1.0D - temp) * (1.0D - temp);
                temp = MathF.saturate(temp);

                temps[idx] = temp;
                humids[idx] = humidity;
                biomes[idx] = Biome.getBiome(temp, humidity);
            }
        }
        return biomes;
    }

    @Unique
    private Biome[] getBiomeBlockFromImage(Biome[] biomes, int x, int z, int w, int d) {
        final int area = w * d;
        if (biomes == null || biomes.length < area) {
            biomes = new Biome[area];
        }

        double[] temps = this.temperatures;
        double[] humids = this.downfalls;
        if (temps == null || temps.length < area) {
            temps = new double[area];
            this.temperatures = temps;
        }
        if (humids == null || humids.length < area) {
            humids = new double[area];
            this.downfalls = humids;
        }

        for (int iX = 0; iX < w; ++iX) {
            for (int iZ = 0; iZ < d; ++iZ) {
                final int idx = iZ * w + iX;

                int infoX = x + iX;
                int infoZ = z + iZ;
                double temp = AC_TerrainImage.getTerrainTemperature(infoX, infoZ);
                double humidity = AC_TerrainImage.getTerrainHumidity(infoX, infoZ);

                temps[idx] = temp;
                humids[idx] = humidity;
                biomes[idx] = Biome.getBiome(temp, humidity);
            }
        }
        return biomes;
    }

    @Unique
    private boolean needsBiomeCacheUpgrade(int x, int z, int w, int d) {
        return this.prevX != x || this.prevZ != z || this.prevW < w || this.prevD < d;
    }

    @Unique
    private void upgradeBiomeCache(int x, int z, int w, int d) {
        this.prevX = x;
        this.prevZ = z;
        this.prevW = Math.max(this.prevW, w);
        this.prevD = Math.max(this.prevD, d);
    }

    @Unique
    private void invalidateBiomeCache() {
        this.prevW = 0;
        this.prevD = 0;
    }
}

