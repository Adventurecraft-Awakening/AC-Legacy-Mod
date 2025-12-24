package dev.adventurecraft.awakening.world.level.storage;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.concurrent.CompletionStage;

public interface AsyncChunkSource extends ChunkSource {

    CompletionStage<LevelChunk> loadAsync(Level level, int x, int z);
}
