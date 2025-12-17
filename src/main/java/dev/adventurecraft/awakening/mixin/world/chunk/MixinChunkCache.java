package dev.adventurecraft.awakening.mixin.world.chunk;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.extension.client.options.ExGameOptions;
import dev.adventurecraft.awakening.extension.world.chunk.ExChunkCache;
import dev.adventurecraft.awakening.util.MathF;
import dev.adventurecraft.awakening.world.level.storage.AsyncChunkSource;
import dev.adventurecraft.awakening.world.level.storage.AsyncChunkStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ProgressListener;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkCache;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChunkCache.class)
public abstract class MixinChunkCache implements ExChunkCache, AsyncChunkSource {

    @Shadow int xLast;
    @Shadow int zLast;

    @Shadow private LevelChunk emptyChunk;
    @Shadow private Level level;

    @Shadow private ChunkStorage storage;
    @Shadow private ChunkSource source;

    @Shadow private LevelChunk[] chunks;

    @Unique int mask;
    @Unique int chunksWide;

    @Shadow
    public abstract boolean save(boolean bl, ProgressListener arg);

    @Shadow
    public abstract boolean fits(int i, int j);

    @Shadow
    public abstract LevelChunk getChunk(int x, int z);

    @Override
    public void init(Level level, ChunkStorage storage, ChunkSource source) {
        this.resize();
        this.emptyChunk = new EmptyLevelChunk(level, new byte[32768], 0, 0);
        this.level = level;
        this.storage = storage;
        this.source = source;
    }

    @Override
    public void resize() {
        var options = (ExGameOptions) Minecraft.instance.options;
        int dist = options.ofChunkLoadDistance() * 2;
        int newMask = MathF.roundUpToPow2Mask(dist);
        if (this.mask == newMask) {
            return;
        }
        ACMod.LOGGER.info("Resizing chunk cache from {} to {}", this.mask + 1, newMask + 1);

        this.xLast = -999999999;
        this.zLast = -999999999;

        LevelChunk[] chunks = this.chunks;
        if (chunks != null) {
            this.save(true, null);
        }

        this.mask = newMask;
        this.chunksWide = newMask + 1;
        this.chunks = new LevelChunk[this.chunksWide * this.chunksWide];

        if (chunks != null) {
            for (LevelChunk chunk : chunks) {
                if (chunk != null && this.fits(chunk.x, chunk.z)) {
                    int cx = chunk.x & this.mask;
                    int cz = chunk.z & this.mask;
                    int ci = cx + cz * this.chunksWide;
                    this.chunks[ci] = chunk;
                }
            }
        }
    }

    @Override
    public int getCapacity() {
        if (this.chunks == null) {
            return 0;
        }
        return this.chunks.length;
    }

    @ModifyConstant(
        method = "fits",
        constant = @Constant(intValue = 15)
    )
    private int useMask0(int value) {
        return this.mask;
    }

    @ModifyConstant(
        method = {"hasChunk", "getChunk"},
        constant = @Constant(intValue = 31)
    )
    private int useMask1(int value) {
        return this.mask;
    }

    @ModifyConstant(
        method = {"hasChunk", "getChunk"},
        constant = @Constant(intValue = 32)
    )
    private int useChunksWide0(int value) {
        return this.chunksWide;
    }

    @Redirect(
        method = "postProcess",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/chunk/ChunkSource;postProcess(Lnet/minecraft/world/level/chunk/ChunkSource;II)V"
        )
    )
    private void setChunkPopulatingOnDecorate(ChunkSource instance, ChunkSource worldSource, int x, int z) {
        ACMod.chunkIsNotPopulating = false;
        instance.postProcess(worldSource, x, z);
        ACMod.chunkIsNotPopulating = true;
    }

    public void ac$requestChunks(int x0, int z0, int x1, int z1, boolean wait) {
        if (!(this.storage instanceof AsyncChunkStorage asyncStorage)) {
            return;
        }

        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                if (!this.level.isFindingSpawn && !this.fits(x, z)) {
                    continue;
                }
                if (this.hasChunk(x, z)) {
                    continue;
                }
                asyncStorage.requestAsync(this.level, x, z);
            }
        }

        if (wait) {
            for (int x = x0; x <= x1; x++) {
                for (int z = z0; z <= z1; z++) {
                    this.getChunk(x, z);
                }
            }
        }
    }
}