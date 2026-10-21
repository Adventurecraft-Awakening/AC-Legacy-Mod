package dev.adventurecraft.awakening.world.level.storage;

import dev.adventurecraft.awakening.common.MapChunkLoader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.dimension.Dimension;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelIO;

import java.util.List;
import java.util.function.BooleanSupplier;

/** Save overlays take precedence; editing additionally writes back to the map. */
public final class WorldSavePolicy {
    private final LevelIO map;
    private final LevelIO save;
    private final BooleanSupplier editing;

    public WorldSavePolicy(LevelIO map, LevelIO save, BooleanSupplier editing) {
        this.map = map;
        this.save = save;
        this.editing = editing;
    }

    public boolean writesMap() { return this.save == null || this.editing.getAsBoolean(); }

    public ChunkStorage chunkStorage(Dimension dimension) {
        if (this.save == null) return this.map.readDimension(dimension);
        ChunkStorage overlay = this.save.readDimension(dimension);
        return this.map == null ? overlay : new MapChunkLoader(this.map.readDimension(dimension), overlay, this::writesMap);
    }

    public void save(LevelData data, List<Player> players) {
        if (this.save != null) this.save.saveWithPlayers(data, players);
        if (this.map != null && this.map != this.save && this.writesMap()) this.map.saveWithPlayers(data, players);
    }
}
