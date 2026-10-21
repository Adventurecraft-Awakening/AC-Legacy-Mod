package dev.adventurecraft.awakening.world.level.storage;

import dev.adventurecraft.awakening.extension.world.level.chunk.storage.ExRegionFileCache;
import dev.adventurecraft.awakening.extension.nbt.ExTag;
import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.primitives.ChunkCoord;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.storage.OldChunkStorage;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.level.chunk.storage.RegionFileCache;
import net.minecraft.world.level.storage.LevelData;

import javax.annotation.Nullable;
import java.io.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

public class AsyncMcRegionChunkStorage implements AsyncChunkStorage {

    private static final CompletableFuture<LevelChunk> NULL_CHUNK_FUTURE = CompletableFuture.completedFuture(null);

    private final ExecutorService executor;
    private final File basePath;

    private final ReadWriteLock saveQueueLock = new ReentrantReadWriteLock();
    private final Long2ObjectMap<CompletableFuture<Void>> saveQueue = new Long2ObjectOpenHashMap<>();

    public AsyncMcRegionChunkStorage(ExecutorService executor, File path) {
        this.executor = executor;
        this.basePath = path;
    }

    @Override
    public CompletionStage<LevelChunk> loadAsync(Level level, int x, int z) {
        var loader = new LevelChunkLoader(level, x, z);
        var saveFuture = this.getSaveFuture(x, z);
        if (saveFuture != null) {
            return saveFuture.thenCompose(_ -> {
                if (!loader.hasChunk()) {
                    return NULL_CHUNK_FUTURE;
                }
                // TODO: rehydrate saved chunk instead of loading from disk?
                return CompletableFuture.supplyAsync(loader, this.executor);
            });
        }
        if (loader.hasRegionButNotChunk()) {
            return NULL_CHUNK_FUTURE;
        }
        return CompletableFuture.supplyAsync(loader, this.executor);
    }

    @Override
    public LevelChunk load(Level level, int x, int z) {
        var saveFuture = this.getSaveFuture(x, z);
        if (saveFuture != null) {
            saveFuture.join();
        }
        return new LevelChunkLoader(level, x, z).get();
    }

    private @Nullable CompletableFuture<Void> getSaveFuture(int x, int z) {
        this.saveQueueLock.readLock().lock();
        try {
            return this.saveQueue.get(ChunkCoord.pack(x, z));
        } finally {
            this.saveQueueLock.readLock().unlock();
        }
    }

    public void save(Level level, LevelChunk chunk) {
        // Capture live entities, script scopes and arrays on the world thread.
        // The I/O worker must never enter a world's Rhino context or read mutable chunks.
        level.checkSession();
        var levelTag = new CompoundTag();
        OldChunkStorage.save(chunk, level, levelTag);
        var root = new CompoundTag();
        root.putTag("Level", levelTag);
        var snapshot = (CompoundTag) ((ExTag) root).copy();
        var saver = new LevelChunkSaver(chunk.x, chunk.z, snapshot, level.getLevelData());
        long key = ChunkCoord.pack(chunk.x, chunk.z);
        CompletableFuture<Void> queued;
        this.saveQueueLock.writeLock().lock();
        try {
            CompletableFuture<Void> previous = this.saveQueue.get(key);
            // A newer full snapshot can recover after a failed write of an older snapshot.
            queued = previous == null ? CompletableFuture.runAsync(saver, this.executor)
                : previous.handle((ignored, error) -> null).thenRunAsync(saver, this.executor);
            this.saveQueue.put(key, queued);
        } finally {
            this.saveQueueLock.writeLock().unlock();
        }
        queued.whenComplete((ignored, error) -> {
            if (error != null) {
                ACMod.LOGGER.error("Failed to save chunk {}, {} in {}", saver.x, saver.z, this.basePath, error);
                return; // Retain the failed tail so flush/load report the failure.
            }
            this.saveQueueLock.writeLock().lock();
            try {
                // An earlier save must not remove a newer tail for the same coordinates.
                this.saveQueue.remove(key, queued);
            } finally {
                this.saveQueueLock.writeLock().unlock();
            }
        });
    }

    public void saveEntities(Level level, LevelChunk chunk) { }
    public void tick() { }

    public void flush() {
        while (true) {
            CompletableFuture<?>[] pending;
            this.saveQueueLock.readLock().lock();
            try {
                pending = this.saveQueue.values().toArray(CompletableFuture<?>[]::new);
            } finally {
                this.saveQueueLock.readLock().unlock();
            }
            if (pending.length == 0) return;
            // Completion takes the write lock to remove its queue entry.
            CompletableFuture.allOf(pending).join();
            this.saveQueueLock.writeLock().lock();
            try {
                this.saveQueue.values().removeIf(future -> future.isDone() && !future.isCompletedExceptionally());
            } finally {
                this.saveQueueLock.writeLock().unlock();
            }
        }
    }

    private class LevelChunkLoader implements Supplier<LevelChunk> {
        private final Level level;
        private final int x;
        private final int z;

        public LevelChunkLoader(Level level, int x, int z) {
            this.x = x;
            this.z = z;
            this.level = level;
        }

        /**
         * Opens the region and checks if it has the chunk.
         */
        public boolean hasChunk() {
            RegionFile regionFile = RegionFileCache.getRegionFile(basePath, x, z);
            return regionFile.hasChunk(x & 0x1F, z & 0x1F);
        }

        /**
         * Checks if the chunk exists only if the region is already open.
         */
        public boolean hasRegionButNotChunk() {
            RegionFile regionFile = ExRegionFileCache.tryGetRegionFile(basePath, x, z);
            return regionFile != null && !regionFile.hasChunk(x & 0x1F, z & 0x1F);
        }

        @Override
        public LevelChunk get() {
            var storage = AsyncMcRegionChunkStorage.this;
            CompoundTag root;
            try (DataInputStream input = RegionFileCache.getChunkDataInputStream(storage.basePath, x, z)) {
                if (input == null) return null;
                root = NbtIo.read((DataInput) input);
            } catch (IOException error) {
                throw new UncheckedIOException("Failed to read chunk " + x + ", " + z, error);
            }
            if (!root.hasKey("Level")) {
                ACMod.LOGGER.warn("Chunk file at {}, {} is missing level data, skipping", x, z);
                return null;
            }
            CompoundTag data = root.getCompoundTag("Level");
            if (!data.hasKey("Blocks")) {
                ACMod.LOGGER.warn("Chunk file at {}, {} is missing block data, skipping", x, z);
                return null;
            }
            LevelChunk chunk = OldChunkStorage.load(level, data);
            if (!chunk.isAt(x, z)) {
                ACMod.LOGGER.warn("Relocating chunk {}, {} to {}, {}", chunk.x, chunk.z, x, z);
                data.putInt("xPos", x);
                data.putInt("zPos", z);
                chunk = OldChunkStorage.load(level, data);
            }
            chunk.onLoad();
            return chunk;
        }
    }

    private final class LevelChunkSaver implements Runnable {
        private final int x, z;
        private final CompoundTag snapshot;
        private final LevelData levelData;

        private LevelChunkSaver(int x, int z, CompoundTag snapshot, LevelData levelData) {
            this.x = x;
            this.z = z;
            this.snapshot = snapshot;
            this.levelData = levelData;
        }

        @Override
        public void run() {
            try (DataOutputStream output = RegionFileCache.getChunkDataOutputStream(basePath, this.x, this.z)) {
                NbtIo.write(this.snapshot, (DataOutput) output);
            } catch (Exception error) {
                throw new IllegalStateException("Failed to write chunk " + this.x + ", " + this.z, error);
            }
            long sizeDelta = RegionFileCache.getSizeDelta(basePath, this.x, this.z);
            synchronized (this.levelData) {
                this.levelData.setSize(this.levelData.getSize() + sizeDelta);
            }
        }
    }
}
