package dev.adventurecraft.awakening.mixin.world.level.biome;

import dev.adventurecraft.awakening.mixin.world.gen.MixinBiomeSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.FixedBiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Arrays;

@Mixin(FixedBiomeSource.class)
public abstract class MixinFixedBiomeSource extends MixinBiomeSource {

    @Shadow private Biome biome;
    @Shadow private double field_555;
    @Shadow private double field_556;

    @Override
    public BiomeSource copy() {
        return new FixedBiomeSource(this.biome, this.field_555, this.field_556);
    }

    @Override
    public short[] getTemperatureBlockF16(short[] temps, int x, int z, int w, int d) {
        if (temps == null || temps.length < w * d) {
            temps = new short[w * d];
        }
        Arrays.fill(temps, 0, w * d, Float.floatToFloat16((float) this.field_555));
        return temps;
    }
}
