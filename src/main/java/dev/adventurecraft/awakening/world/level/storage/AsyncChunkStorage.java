package dev.adventurecraft.awakening.world.level.storage;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.storage.ChunkStorage;

import java.util.concurrent.CompletionStage;

public interface AsyncChunkStorage extends ChunkStorage {

    CompletionStage<LevelChunk> loadAsync(Level level, int x, int z);
}
