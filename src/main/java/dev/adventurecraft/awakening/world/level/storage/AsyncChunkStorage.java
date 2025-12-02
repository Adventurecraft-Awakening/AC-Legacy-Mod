package dev.adventurecraft.awakening.world.level.storage;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.storage.ChunkStorage;

public interface AsyncChunkStorage extends ChunkStorage {

    boolean requestAsync(Level level, int x, int z);
}
