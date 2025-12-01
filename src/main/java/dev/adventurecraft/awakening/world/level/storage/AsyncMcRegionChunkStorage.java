package dev.adventurecraft.awakening.world.level.storage;

import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.chunk.storage.OldChunkStorage;
import net.minecraft.world.level.chunk.storage.RegionFileCache;
import net.minecraft.world.level.storage.LevelData;

import java.io.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AsyncMcRegionChunkStorage implements ChunkStorage {

    private final ExecutorService executor = Executors.newWorkStealingPool();

    private final ReadWriteLock saveQueueLock = new ReentrantReadWriteLock();
    private final Long2ObjectMap<Future<Void>> saveQueue = new Long2ObjectArrayMap<>();

    private final File basePath;

    public AsyncMcRegionChunkStorage(File path) {
        this.basePath = path;
    }

    private static long chunkKey(int x, int z) {
        return ((long) z << 32) | x;
    }

    public LevelChunk load(Level level, int x, int z) {
        try {
            return this.executor.submit(() -> {
                // Wait for saves to finish here to not stall the caller.
                this.saveQueueLock.readLock().lock();
                Future<Void> saveFuture = this.saveQueue.get(chunkKey(x, z));
                this.saveQueueLock.readLock().unlock();
                if (saveFuture != null) {
                    saveFuture.get();
                }

                DataInputStream dataInputStream = RegionFileCache.getChunkDataInputStream(this.basePath, x, z);
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
                LevelChunk levelChunk = OldChunkStorage.load(level, compoundTag.getCompoundTag("Level"));
                if (!levelChunk.isAt(x, z)) {
                    System.out.println(
                        "Chunk file at " + x + "," + z + " is in the wrong location; relocating. (Expected " + x +
                            ", " + z + ", got " + levelChunk.x + ", " + levelChunk.z + ")");
                    compoundTag.putInt("xPos", x);
                    compoundTag.putInt("zPos", z);
                    levelChunk = OldChunkStorage.load(level, compoundTag.getCompoundTag("Level"));
                }
                levelChunk.onLoad();
                return levelChunk;
            }).get();
        }
        catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public void save(Level level, LevelChunk chunk) {
        var future = this.executor.submit(() -> {
            level.checkSession();
            try {
                DataOutputStream dataOutputStream = RegionFileCache.getChunkDataOutputStream(
                    this.basePath,
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
                long sizeDelta = RegionFileCache.getSizeDelta(this.basePath, chunk.x, chunk.z);
                levelData.setSize(levelData.getSize() + sizeDelta);
            }
            catch (Exception exception) {
                exception.printStackTrace();
            } finally {
                this.saveQueueLock.writeLock().lock();
                this.saveQueue.remove(chunkKey(chunk.x, chunk.z));
                this.saveQueueLock.writeLock().unlock();
            }
        });

        this.saveQueueLock.writeLock().lock();
        this.saveQueue.put(chunkKey(chunk.x, chunk.z), (Future<Void>) future);
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
}

