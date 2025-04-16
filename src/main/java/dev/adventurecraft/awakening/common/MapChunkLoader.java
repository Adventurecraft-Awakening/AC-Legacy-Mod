package dev.adventurecraft.awakening.common;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.storage.ChunkStorage;

public class MapChunkLoader implements ChunkStorage {
    private final ChunkStorage mapRegion;
    private final ChunkStorage saveRegion;

    public MapChunkLoader(ChunkStorage var1, ChunkStorage var2) {
        this.mapRegion = var1;
        this.saveRegion = var2;
    }

    public LevelChunk load(Level var1, int var2, int var3) {
        LevelChunk var4 = this.saveRegion.load(var1, var2, var3);
        if (var4 == null) {
            var4 = this.mapRegion.load(var1, var2, var3);
        }
        return var4;
    }

    public void save(Level var1, LevelChunk var2) {
        this.saveRegion.save(var1, var2);
        if (AC_DebugMode.levelEditing) {
            this.mapRegion.save(var1, var2);
        }
    }

    public void saveEntities(Level var1, LevelChunk var2) {
    }

    public void tick() {
    }

    public void flush() {
    }
}
