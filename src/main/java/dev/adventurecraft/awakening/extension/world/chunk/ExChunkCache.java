package dev.adventurecraft.awakening.extension.world.chunk;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.storage.ChunkStorage;

public interface ExChunkCache {

    void init(Level var1, ChunkStorage var2, ChunkSource var3);

    int getCapacity();

    void updateVeryFar();
}
