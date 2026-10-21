package dev.adventurecraft.awakening.world.level.chunk;

import net.minecraft.world.level.chunk.LevelChunk;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public record SupplierChunkTicket(Supplier<LevelChunk> supplier, BooleanSupplier readiness) implements ChunkTicket {
    public SupplierChunkTicket(Supplier<LevelChunk> supplier) {
        this(supplier, () -> true);
    }

    public @Override boolean isReady() {
        return this.readiness.getAsBoolean();
    }

    public @Override LevelChunk get() {
        return this.supplier.get();
    }
}
