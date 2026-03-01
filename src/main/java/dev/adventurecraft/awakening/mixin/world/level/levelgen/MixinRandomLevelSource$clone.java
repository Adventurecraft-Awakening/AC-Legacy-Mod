package dev.adventurecraft.awakening.mixin.world.level.levelgen;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.extension.world.level.biome.ExBiomeSource;
import dev.adventurecraft.awakening.extension.world.level.worldgen.ExLargeFeature;
import dev.adventurecraft.awakening.extension.world.level.worldgen.ExRandomLevelSource;
import dev.adventurecraft.awakening.util.RandomUtil;
import dev.adventurecraft.awakening.world.level.storage.AsyncChunkSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.LargeFeature;
import net.minecraft.world.level.levelgen.RandomLevelSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Mixin(RandomLevelSource.class)
public abstract class MixinRandomLevelSource$clone implements Cloneable, ExRandomLevelSource, AsyncChunkSource {

    @Shadow private Random random;
    @Shadow private Level level;

    @Shadow private double[] buffer;
    @Shadow private double[] sandBuffer;
    @Shadow private double[] gravelBuffer;
    @Shadow private double[] depthBuffer;

    @Shadow private LargeFeature caveFeature;
    @Shadow private Biome[] biomes;
    @Shadow double[] pnr;
    @Shadow double[] ar;
    @Shadow double[] br;
    @Shadow double[] sr;
    @Shadow double[] dr;
    @Shadow private double[] temperatures;

    @Unique private BiomeSource biomeSource;

    @Shadow
    public abstract LevelChunk loadChunk(int x, int z);

    // Redirecting "postProcess" is not strictly necessary since it is called on main thread,
    // but it should not hurt in this case.
    @Redirect(
        method = {"getChunk", "getHeights", "postProcess"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;getBiomeSource()Lnet/minecraft/world/level/biome/BiomeSource;"
        )
    )
    private BiomeSource useLocalBiomeSource(Level instance) {
        if (this.biomeSource != null) {
            return this.biomeSource;
        }
        return instance.getBiomeSource();
    }

    @Override
    public void ac$initCopy() {
        this.random = RandomUtil.clone(this.random);
        this.buffer = null;
        this.sandBuffer = new double[256];
        this.gravelBuffer = new double[256];
        this.depthBuffer = new double[256];
        this.caveFeature = ((ExLargeFeature) this.caveFeature).ac$clone();
        this.biomes = null;
        this.pnr = null;
        this.ar = null;
        this.br = null;
        this.sr = null;
        this.dr = null;
        // this.waterDepths = new int[32][32]; // unused in vanilla
        this.temperatures = null;

        this.biomeSource = ((ExBiomeSource) this.level.getBiomeSource()).copy();
    }

    @Override
    public RandomLevelSource ac$clone() {
        RandomLevelSource source;
        try {
            source = (RandomLevelSource) super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new AssertionError(null, e);
        }
        ((ExRandomLevelSource) source).ac$initCopy();
        return source;
    }

    @Override
    public CompletionStage<LevelChunk> loadAsync(Level level, int x, int z) {
        RandomLevelSource self = this.ac$clone();
        return CompletableFuture.supplyAsync(() -> self.getChunk(x, z), ACMod.WORLD_GEN_EXECUTOR);
    }
}
