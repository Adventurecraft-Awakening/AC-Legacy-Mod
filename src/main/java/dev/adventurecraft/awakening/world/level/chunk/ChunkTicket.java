package dev.adventurecraft.awakening.world.level.chunk;


import net.minecraft.world.level.chunk.LevelChunk;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public interface ChunkTicket extends Supplier<LevelChunk> {

    /** True once asynchronous prerequisites finish; get may still perform synchronous work. */
    boolean isReady();

    default ChunkTicket thenApply(Function<LevelChunk, LevelChunk> func) {
        Objects.requireNonNull(func);
        // Keep publication/generation lazy: neither may run on the storage executor.
        return new SupplierChunkTicket(() -> func.apply(this.get()), this::isReady);
    }
}

