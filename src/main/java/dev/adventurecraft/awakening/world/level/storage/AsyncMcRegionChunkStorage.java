package dev.adventurecraft.awakening.world.level.storage;

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
    private final Long2ObjectMap<CompletableFuture<LevelChunk>> loadQueue = new Long2ObjectOpenHashMap<>();

    public AsyncMcRegionChunkStorage(ExecutorService executor, File path) {
        this.executor = executor;
        this.basePath = path;
    }

    private static long chunkKey(int x, int z) {
        return (Integer.toUnsignedLong(z) << 32) | Integer.toUnsignedLong(x);
    }

    @Override
    public boolean requestAsync(Level level, int x, int z) {
        var ticket = this.loadQueue.computeIfAbsent(chunkKey(x, z), key -> this.createTicket(level, x, z));
        if (ticket.isDone()) {
            return ticket.resultNow() != null;
        }
        return false;
    }

    private CompletableFuture<LevelChunk> createTicket(Level level, int x, int z) {
        var loader = new LevelChunkLoader(level, x, z);
        var saveFuture = this.getSaveFuture(x, z);
        if (saveFuture != null) {
            return saveFuture.thenCompose(v -> {
                if (!loader.hasChunk()) {
                    return NULL_CHUNK_FUTURE;
                }
                // TODO: rehydrate saved chunk instead of loading from disk
                return CompletableFuture.supplyAsync(loader, this.executor);
            });
        }
        if (!loader.hasChunk()) {
            return NULL_CHUNK_FUTURE;
        }
        return CompletableFuture.supplyAsync(loader, this.executor);
    }

    @Override
    public LevelChunk load(Level level, int x, int z) {
        var ticket = this.loadQueue.remove(chunkKey(x, z));
        try {
            if (ticket != null) {
                return ticket.toCompletableFuture().get();
            }

            var saveFuture = this.getSaveFuture(x, z);
            if (saveFuture != null) {
                saveFuture.get();
            }
        }
        catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        return new LevelChunkLoader(level, x, z).get();
    }

    private @Nullable CompletableFuture<Void> getSaveFuture(int x, int z) {
        this.saveQueueLock.readLock().lock();
        CompletableFuture<Void> saveFuture = this.saveQueue.get(chunkKey(x, z));
        this.saveQueueLock.readLock().unlock();
        return saveFuture;
    }

    public void save(Level level, LevelChunk chunk) {
        var saver = new LevelChunkSaver(level, chunk);
        this.saveQueueLock.writeLock().lock();
        this.saveQueue.compute(
            chunkKey(chunk.x, chunk.z), (key, entry) -> {
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
        try {
            this.executor.wait();
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
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

        public boolean hasChunk() {
            RegionFile regionFile = RegionFileCache.getRegionFile(basePath, x, z);
            return regionFile.hasChunk(x & 0x1F, z & 0x1F);
        }

        @Override
        public LevelChunk get() {
            // TODO: async IO?
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
                storage.saveQueue.remove(chunkKey(chunk.x, chunk.z));
                storage.saveQueueLock.writeLock().unlock();
            }
        }
    }
}

