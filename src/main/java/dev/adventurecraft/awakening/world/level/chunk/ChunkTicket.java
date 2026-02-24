package dev.adventurecraft.awakening.world.level.chunk;


import net.minecraft.world.level.chunk.LevelChunk;

import java.util.function.Function;
import java.util.function.Supplier;

public interface ChunkTicket extends Supplier<LevelChunk> {
    default ChunkTicket thenApply(Function<LevelChunk, LevelChunk> func) {
        return new SupplierChunkTicket(() -> func.apply(this.get()));
    }
}

