package dev.adventurecraft.awakening.world.level.chunk;

import net.minecraft.world.level.chunk.LevelChunk;

import java.util.concurrent.CompletionStage;

public record CompletionChunkTicket(CompletionStage<LevelChunk> stage) implements ChunkTicket {
    public @Override boolean isReady() {
        // Exceptional completion is ready too, so the caller observes the original failure in get.
        return this.stage.toCompletableFuture().isDone();
    }

    public @Override LevelChunk get() {
        return this.stage.toCompletableFuture().join();
    }
}
