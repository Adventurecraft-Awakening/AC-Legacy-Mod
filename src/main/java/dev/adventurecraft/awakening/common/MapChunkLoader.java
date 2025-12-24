package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.world.level.storage.AsyncChunkStorage;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.storage.ChunkStorage;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class MapChunkLoader implements AsyncChunkStorage {

    private final ChunkStorage mapRegion;
    private final ChunkStorage saveRegion;

    public MapChunkLoader(ChunkStorage mapStorage, ChunkStorage saveStorage) {
        this.mapRegion = mapStorage;
        this.saveRegion = saveStorage;
    }

    @Override
    public LevelChunk load(Level level, int x, int z) {
        LevelChunk chunk = this.saveRegion.load(level, x, z);
        if (chunk != null) {
            return chunk;
        }
        return this.mapRegion.load(level, x, z);
    }

    @Override
    public void save(Level level, LevelChunk chunk) {
        this.saveRegion.save(level, chunk);
        if (AC_DebugMode.levelEditing) {
            this.mapRegion.save(level, chunk);
        }
    }

    @Override
    public CompletionStage<LevelChunk> loadAsync(Level level, int x, int z) {
        return this.loadStage(this.saveRegion, level, x, z).thenCompose(chunk -> {
            if (chunk != null) {
                return CompletableFuture.completedFuture(chunk);
            }
            return this.loadStage(this.mapRegion, level, x, z);
        });
    }

    private CompletionStage<LevelChunk> loadStage(ChunkStorage storage, Level level, int x, int z) {
        if (storage instanceof AsyncChunkStorage asyncStorage) {
            return asyncStorage.loadAsync(level, x, z);
        }
        return CompletableFuture.completedFuture(storage.load(level, x, z));
    }

    public void saveEntities(Level var1, LevelChunk var2) {
    }

    public void tick() {
    }

    public void flush() {
    }
}
