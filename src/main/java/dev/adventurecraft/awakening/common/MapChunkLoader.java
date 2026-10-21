package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.world.level.storage.AsyncChunkStorage;
import dev.adventurecraft.awakening.editor.EditorSession;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.storage.ChunkStorage;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BooleanSupplier;

public class MapChunkLoader implements AsyncChunkStorage {

    private final ChunkStorage mapRegion;
    private final ChunkStorage saveRegion;
    private final BooleanSupplier writesMap;

    public MapChunkLoader(ChunkStorage mapStorage, ChunkStorage saveStorage) {
        this(mapStorage, saveStorage, null);
    }

    public MapChunkLoader(ChunkStorage mapStorage, ChunkStorage saveStorage, BooleanSupplier writesMap) {
        this.mapRegion = mapStorage;
        this.saveRegion = saveStorage;
        this.writesMap = writesMap;
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
        if (this.mapRegion != this.saveRegion && this.writesMap(level)) {
            this.mapRegion.save(level, chunk);
        }
    }

    private boolean writesMap(Level level) {
        return this.writesMap != null ? this.writesMap.getAsBoolean() : EditorSession.forLevel(level).levelEditing;
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

    public void saveEntities(Level level, LevelChunk chunk) {
        this.saveRegion.saveEntities(level, chunk);
        if (this.mapRegion != this.saveRegion && this.writesMap(level)) this.mapRegion.saveEntities(level, chunk);
    }

    public void tick() {
        this.saveRegion.tick();
        if (this.mapRegion != this.saveRegion) this.mapRegion.tick();
    }

    public void flush() {
        try {
            this.saveRegion.flush();
        } catch (RuntimeException | Error failure) {
            if (this.mapRegion != this.saveRegion) {
                try { this.mapRegion.flush(); }
                catch (RuntimeException | Error secondary) {
                    if (secondary != failure) failure.addSuppressed(secondary);
                }
            }
            throw failure;
        }
        if (this.mapRegion != this.saveRegion) this.mapRegion.flush();
    }
}
