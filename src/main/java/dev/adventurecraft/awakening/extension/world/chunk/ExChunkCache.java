package dev.adventurecraft.awakening.extension.world.chunk;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.storage.ChunkStorage;

public interface ExChunkCache {

    void init(Level level, ChunkStorage storage, ChunkSource source);

    int getCapacity();

    void resize();

    void ac$requestChunks(int x0, int z0, int x1, int z1, boolean wait);
}
