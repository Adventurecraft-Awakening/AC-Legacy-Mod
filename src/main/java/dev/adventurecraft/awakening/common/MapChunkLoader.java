package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.world.level.storage.AsyncChunkStorage;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.storage.ChunkStorage;

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
    public boolean requestAsync(Level level, int x, int z) {
        return this.requestAsync(this.saveRegion, level, x, z) || this.requestAsync(this.mapRegion, level, x, z);
    }

    private boolean requestAsync(ChunkStorage storage, Level level, int x, int z) {
        if (storage instanceof AsyncChunkStorage asyncStorage) {
            return asyncStorage.requestAsync(level, x, z);
        }
        return false;
    }

    public void saveEntities(Level var1, LevelChunk var2) {
    }

    public void tick() {
    }

    public void flush() {
    }
}
