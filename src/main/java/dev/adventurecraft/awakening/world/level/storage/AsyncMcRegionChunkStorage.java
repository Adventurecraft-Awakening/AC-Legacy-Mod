package dev.adventurecraft.awakening.world.level.storage;

import dev.adventurecraft.awakening.extension.world.level.chunk.storage.ExRegionFileCache;
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
        CompletableFuture<Void> saveFuture = this.saveQueue.get(ChunkCoord.pack(x, z));
        this.saveQueueLock.readLock().unlock();
        return saveFuture;
    }

    public void save(Level level, LevelChunk chunk) {
        var saver = new LevelChunkSaver(level, chunk);
        this.saveQueueLock.writeLock().lock();
        this.saveQueue.compute(
            ChunkCoord.pack(chunk.x, chunk.z), (key, entry) -> {
                if (entry != null) {
                    return entry.thenRunAsync(saver, this.executor);
                }
                return CompletableFuture.runAsync(saver, this.executor);
            }
        );
        this.saveQueueLock.writeLock().unlock();
    }

    public void saveEntities(Level level, LevelChunk chunk) {
    }

    public void tick() {
    }

    public void flush() {
        // TODO: flush saveQueue here?
        //       could be problematic to write-lock here in case chunks are requested to save

        this.saveQueueLock.writeLock().lock();
        this.saveQueue.values().forEach(CompletableFuture::join);
        this.saveQueue.clear();
        this.saveQueueLock.writeLock().unlock();
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
            DataInputStream dataInputStream = RegionFileCache.getChunkDataInputStream(storage.basePath, x, z);
            if (dataInputStream == null) {
                return null;
            }
            CompoundTag compoundTag = NbtIo.read((DataInput) dataInputStream);
            if (!compoundTag.hasKey("Level")) {
                System.out.println("Chunk file at " + x + "," + z + " is missing level data, skipping");
                return null;
            }
            if (!compoundTag.getCompoundTag("Level").hasKey("Blocks")) {
                System.out.println("Chunk file at " + x + "," + z + " is missing block data, skipping");
                return null;
            }
            LevelChunk chunk = OldChunkStorage.load(level, compoundTag.getCompoundTag("Level"));
            if (!chunk.isAt(x, z)) {
                System.out.println(
                    "Chunk file at " + x + "," + z + " is in the wrong location; relocating. (Expected " + x + ", " +
                        z + ", got " + chunk.x + ", " + chunk.z + ")");
                compoundTag.putInt("xPos", x);
                compoundTag.putInt("zPos", z);
                chunk = OldChunkStorage.load(level, compoundTag.getCompoundTag("Level"));
            }
            chunk.onLoad();
            return chunk;
        }
    }

    private class LevelChunkSaver implements Runnable {
        private final Level level;
        private final LevelChunk chunk;

        public LevelChunkSaver(Level level, LevelChunk chunk) {
            this.level = level;
            this.chunk = chunk;
        }

        @Override
        public void run() {
            level.checkSession();
            var storage = AsyncMcRegionChunkStorage.this;
            try {
                DataOutputStream dataOutputStream = RegionFileCache.getChunkDataOutputStream(
                    storage.basePath,
                    chunk.x,
                    chunk.z
                );
                CompoundTag chunkTag = new CompoundTag();
                CompoundTag levelTag = new CompoundTag();
                chunkTag.putTag("Level", levelTag);
                OldChunkStorage.save(chunk, level, levelTag);
                NbtIo.write(chunkTag, (DataOutput) dataOutputStream);
                dataOutputStream.close();

                LevelData levelData = level.getLevelData();
                long sizeDelta = RegionFileCache.getSizeDelta(storage.basePath, chunk.x, chunk.z);
                levelData.setSize(levelData.getSize() + sizeDelta);
            }
            catch (Exception exception) {
                exception.printStackTrace();
            }
            finally {
                storage.saveQueueLock.writeLock().lock();
                storage.saveQueue.remove(ChunkCoord.pack(chunk.x, chunk.z));
                storage.saveQueueLock.writeLock().unlock();
            }
        }
    }
}

