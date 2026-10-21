package dev.adventurecraft.awakening.extension.world.chunk;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.storage.ChunkStorage;

public interface ExChunkCache {

    void init(Level level, ChunkStorage storage, ChunkSource source);

    int getCapacity();

    void resize();

    /**
     * Requests one mesh dependency and publishes it on the main thread once storage is ready.
     * Returns false while asynchronous storage is pending; generation remains on the main thread.
     */
    default boolean ac$prepareChunk(int x, int z) {
        return this.ac$prepareChunk(x, z, true);
    }

    /** Request-only calls queue storage lazily and return false until the dependency is published. */
    boolean ac$prepareChunk(int x, int z, boolean publish);

    void ac$requestChunks(int x0, int z0, int x1, int z1, boolean wait);
}
