package dev.adventurecraft.awakening.world.level.chunk;

import net.minecraft.world.level.chunk.LevelChunk;

import java.util.function.Supplier;

public record SupplierChunkTicket(Supplier<LevelChunk> supplier) implements ChunkTicket {
    public @Override LevelChunk get() {
        return this.supplier.get();
    }
}
