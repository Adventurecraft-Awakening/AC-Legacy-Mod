package dev.adventurecraft.awakening.mixin.world;

import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.common.AC_PlayerTorch;
import dev.adventurecraft.awakening.extension.world.chunk.ExChunk;
import dev.adventurecraft.awakening.extension.world.level.ExRegion;
import dev.adventurecraft.awakening.util.MathF;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Region;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.nio.ByteBuffer;

@Mixin(Region.class)
public abstract class MixinWorldPopulationRegion implements ExRegion {

    @Shadow private int xc1;
    @Shadow private int zc1;
    @Shadow private LevelChunk[][] chunks;
    @Shadow private Level level;

    @Shadow
    public abstract int getRawBrightness(int x, int y, int z);

    @Shadow
    public abstract int getTile(int i, int j, int k);

    @Environment(EnvType.CLIENT)
    @Overwrite
    public float getBrightness(int x, int y, int z, int max) {
        int rawValue = Math.max(this.getRawBrightness(x, y, z), max);

        float[] ramp = this.level.dimension.brightnessRamp;
        float torchValue = AC_PlayerTorch.getTorchLight(this.level, x, y, z);
        if (rawValue >= torchValue) {
            return ramp[rawValue];
        }
        return remapTorchValue(ramp, torchValue);
    }

    @Environment(EnvType.CLIENT)
    @Overwrite
    public float getBrightness(int x, int y, int z) {
        return getBrightness(x, y, z, 0);
    }

    private static @Unique float remapTorchValue(float[] ramp, float torchValue) {
        int low = (int) Math.floor(torchValue);
        if (low == 15) {
            return ramp[15];
        }
        int high = (int) Math.ceil(torchValue);
        float delta = torchValue - low;
        return MathF.lerp(delta, ramp[low], ramp[high]);
    }

    private static @Unique boolean outOfBounds(int x, int z) {
        return Math.max(Math.abs(x), Math.abs(z)) > 32000000;
    }

    @Environment(EnvType.CLIENT)
    @Overwrite
    public int getRawBrightness(int x, int y, int z, boolean checkNeighbors) {
        if (outOfBounds(x, z)) {
            return 15;
        }

        if (checkNeighbors) {
            int n = this.getNeighborLight(x, y, z);
            if (n != -1) {
                return n;
            }
        }
        return this.getChunkOrSkyLight(x, y, z);
    }

    private @Unique int getNeighborLight(int x, int y, int z) {
        int id = this.getTile(x, y, z);
        if (!ExBlock.neighborLit[id]) {
            return -1;
        }

        int light = this.getClampedChunkLight(0, x, y + 1, z);
        light = this.getClampedChunkLight(light, x + 1, y, z);
        light = this.getClampedChunkLight(light, x - 1, y, z);
        light = this.getClampedChunkLight(light, x, y, z + 1);
        light = this.getClampedChunkLight(light, x, y, z - 1);
        return light;
    }

    private @Unique int getClampedChunkLight(int light, int x, int y, int z) {
        if (outOfBounds(x, z) || light >= 15) {
            return 15;
        }
        return Math.max(light, this.getChunkOrSkyLight(x, y, z));
    }

    private @Unique int getChunkOrSkyLight(int x, int y, int z) {
        if (y < 0) {
            return 0;
        }
        if (y >= 128) {
            int above = 15 - this.level.skyDarken;
            return Math.max(0, above);
        }
        return this.getRawChunkLight(x, y, z);
    }

    private @Unique int getRawChunkLight(int x, int y, int z) {
        int cX = (x >> 4) - this.xc1;
        int cZ = (z >> 4) - this.zc1;
        return this.chunks[cX][cZ].getRawBrightness(x & 15, y, z & 15, this.level.skyDarken);
    }

    public @Override void getTileColumn(ByteBuffer buffer, int x, int y0, int z, int y1) {
        int cX = (x >> 4) - this.xc1;
        if (cX < 0 || cX >= this.chunks.length) {
            return;
        }
        var row = this.chunks[cX];
        int cZ = (z >> 4) - this.zc1;
        if (cZ < 0 || cZ >= row.length) {
            return;
        }
        LevelChunk chunk = row[cZ];
        if (chunk == null) {
            return;
        }
        ((ExChunk) chunk).getTileColumn(buffer, x & 0xF, Math.min(y0, y1), z & 0xF, Math.max(y0, y1));
    }
}
