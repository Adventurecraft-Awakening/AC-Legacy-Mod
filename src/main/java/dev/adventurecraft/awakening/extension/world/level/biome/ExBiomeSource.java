package dev.adventurecraft.awakening.extension.world.level.biome;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;

public interface ExBiomeSource {

    void doInit(
        Level level,
        PerlinSimplexNoise temperatureMap,
        PerlinSimplexNoise downfallMap,
        PerlinSimplexNoise noiseMap
    );

    short[] getTemperatureBlockF16(short[] temps, int x, int z, int w, int d) ;

    BiomeSource copy();
}
