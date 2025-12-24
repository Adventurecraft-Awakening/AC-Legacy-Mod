package dev.adventurecraft.awakening.mixin.world.chunk;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.extension.client.options.ExGameOptions;
import dev.adventurecraft.awakening.extension.world.chunk.ExChunkCache;
import dev.adventurecraft.awakening.primitives.ChunkCoord;
import dev.adventurecraft.awakening.util.MathF;
import dev.adventurecraft.awakening.world.level.storage.AsyncChunkSource;
import dev.adventurecraft.awakening.world.level.storage.AsyncChunkStorage;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ProgressListener;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkCache;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Mixin(ChunkCache.class)
public abstract class MixinChunkCache implements ExChunkCache, AsyncChunkSource {

    @Shadow int xLast;
    @Shadow int zLast;
    @Shadow private LevelChunk last;

    @Shadow private LevelChunk emptyChunk;
    @Shadow private Level level;

    @Shadow private ChunkStorage storage;
    @Shadow private ChunkSource source;

    @Shadow private LevelChunk[] chunks;

    @Unique int mask;
    @Unique int chunksWide;

    @Unique private AsyncChunkStorage asyncStorage;
    @Unique private AsyncChunkSource asyncSource;

    @Unique private Long2ObjectMap<CompletionStage<LevelChunk>> loadQueue;

    @Shadow
    public abstract boolean save(boolean bl, ProgressListener arg);

    @Shadow
    public abstract boolean fits(int i, int j);

    @Shadow
    protected abstract void saveChunk(LevelChunk chunk);

    @Shadow
    protected abstract void saveEntities(LevelChunk chunk);

    @Shadow
    public abstract boolean hasChunk(int x, int z);

    @Override
    public void init(Level level, ChunkStorage storage, ChunkSource source) {
        this.resize();
        this.emptyChunk = new EmptyLevelChunk(level, new byte[32768], 0, 0);
        this.level = level;
        this.storage = storage;
        this.source = source;

        this.asyncStorage = storage instanceof AsyncChunkStorage acs ? acs : null;
        this.asyncSource = source instanceof AsyncChunkSource acs ? acs : null;

        this.loadQueue = new Long2ObjectOpenHashMap<>();
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

    @Overwrite
    public LevelChunk getChunk(int x, int z) {
        if (x == this.xLast && z == this.zLast && this.last != null) {
            return this.last;
        }
        if (!this.level.isFindingSpawn && !this.fits(x, z)) {
            return this.emptyChunk;
        }
        if (!this.hasChunk(x, z)) {
            this.putChunk(x, z);
        }
        int cx = x & this.mask;
        int cz = z & this.mask;
        int ci = cx + cz * this.chunksWide;
        this.xLast = x;
        this.zLast = z;
        this.last = this.chunks[ci];
        return this.chunks[ci];
    }

    @Unique
    private void putChunk(int x, int z) {
        int cx = x & this.mask;
        int cz = z & this.mask;
        int ci = cx + cz * this.chunksWide;

        LevelChunk prevChunk = this.chunks[ci];
        if (prevChunk != null) {
            prevChunk.unload();
            this.saveChunk(prevChunk);
            this.saveEntities(prevChunk);
        }

        long chunkKey = ChunkCoord.pack(x, z);
        var chunkTask = this.loadQueue.remove(chunkKey);
        if (chunkTask == null) {
            chunkTask = this.createTicket(chunkKey);
        }
        LevelChunk newChunk = chunkTask.toCompletableFuture().join();
        this.chunks[ci] = newChunk;
        newChunk.lightLava();
        newChunk.load();

        if (!newChunk.terrainPopulated && this.hasChunk(x + 1, z + 1) && this.hasChunk(x, z + 1) &&
            this.hasChunk(x + 1, z)) {
            this.postProcess(this, x, z);
        }
        if (this.hasChunk(x - 1, z) && !this.getChunk(x - 1, z).terrainPopulated && this.hasChunk(x - 1, z + 1) &&
            this.hasChunk(x, z + 1) && this.hasChunk(x - 1, z)) {
            this.postProcess(this, x - 1, z);
        }
        if (this.hasChunk(x, z - 1) && !this.getChunk(x, z - 1).terrainPopulated && this.hasChunk(x + 1, z - 1) &&
            this.hasChunk(x, z - 1) && this.hasChunk(x + 1, z)) {
            this.postProcess(this, x, z - 1);
        }
        if (this.hasChunk(x - 1, z - 1) && !this.getChunk(x - 1, z - 1).terrainPopulated &&
            this.hasChunk(x - 1, z - 1) && this.hasChunk(x, z - 1) && this.hasChunk(x - 1, z)) {
            this.postProcess(this, x - 1, z - 1);
        }
    }

    @Override
    public void ac$requestChunks(int x0, int z0, int x1, int z1, boolean wait) {
        if (this.asyncStorage == null) {
            return;
        }

        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                this.requestChunk(x, z);
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

    @Unique
    private void requestChunk(int x, int z) {
        if (this.asyncStorage == null) {
            return;
        }
        if (!this.level.isFindingSpawn && !this.fits(x, z)) {
            return;
        }
        if (this.hasChunk(x, z)) {
            return;
        }
        this.loadQueue.computeIfAbsent(ChunkCoord.pack(x, z), this::createTicket);
    }

    @Unique
    private CompletionStage<LevelChunk> createTicket(long key) {
        int x = ChunkCoord.unpackX(key);
        int z = ChunkCoord.unpackZ(key);
        var loadStage = this.asyncStorage.loadAsync(this.level, x, z);

        if (this.asyncSource == null) {
            return loadStage.thenApply(c -> {
                if (c != null) {
                    return c;
                }
                if (this.source == null) {
                    return this.emptyChunk;
                }
                return onSourceLoad(this.source.loadChunk(x, z));
            });
        }

        return loadStage.thenCompose(c -> {
            if (c != null) {
                return CompletableFuture.completedStage(c);
            }
            return this.asyncSource.loadAsync(this.level, x, z).thenApply(MixinChunkCache::onSourceLoad);
        });
    }

    @Unique
    private static LevelChunk onSourceLoad(LevelChunk chunk) {
        chunk.onLoad();
        return chunk;
    }
}