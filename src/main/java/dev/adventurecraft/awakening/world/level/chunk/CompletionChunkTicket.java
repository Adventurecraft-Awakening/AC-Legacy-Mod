package dev.adventurecraft.awakening.world.level.chunk;

import net.minecraft.world.level.chunk.LevelChunk;

import java.util.concurrent.CompletionStage;

public record CompletionChunkTicket(CompletionStage<LevelChunk> stage) implements ChunkTicket {
    public @Override LevelChunk get() {
        return this.stage.toCompletableFuture().join();
    }
}
