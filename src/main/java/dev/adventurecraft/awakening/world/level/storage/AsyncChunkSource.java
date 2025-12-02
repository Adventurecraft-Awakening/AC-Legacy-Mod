package dev.adventurecraft.awakening.world.level.storage;

import net.minecraft.world.level.chunk.ChunkSource;

public interface AsyncChunkSource extends ChunkSource {

    void ac$requestChunks(int x0, int z0, int x1, int z1, boolean wait);
}
