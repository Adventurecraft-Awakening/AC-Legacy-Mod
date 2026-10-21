package dev.adventurecraft.awakening.world.trigger;

import dev.adventurecraft.awakening.common.AC_TriggerArea;
import dev.adventurecraft.awakening.world.BlockPos;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;

import java.util.Map;
import java.util.function.Consumer;

/** Trigger registration and callback ordering, independent of block lookup and client UI. */
public final class TriggerAreaTracker {

    private final Map<BlockPos, Int2ObjectOpenHashMap<AC_TriggerArea>> sources = new Object2ObjectOpenHashMap<>();
    private final TriggerAreaIndex index = new TriggerAreaIndex();
    private final TriggerAreaTraversal.BlockConsumer activate;
    private final TriggerAreaTraversal.BlockConsumer deactivate;

    public TriggerAreaTracker(TriggerAreaTraversal.BlockConsumer activate, TriggerAreaTraversal.BlockConsumer deactivate) {
        this.activate = activate;
        this.deactivate = deactivate;
    }

    public void addArea(int x, int y, int z, int id, AC_TriggerArea area) {
        var pos = new BlockPos(x, y, z);
        var map = this.sources.get(pos);
        if (map == null) {
            map = new Int2ObjectOpenHashMap<>();
            this.sources.put(pos, map);
        }
        var previous = map.get(id);
        var activation = this.index.planActivation(area);
        map.put(id, area);
        this.index.put(pos, id, area);
        activation.forEach(this.activate);
        if (previous != null) {
            this.deactivateArea(previous);
        }
    }

    public void removeArea(int x, int y, int z) {
        var pos = new BlockPos(x, y, z);
        var map = this.sources.get(pos);
        if (map != null) {
            for (int id : map.clone().keySet()) {
                this.removeArea(pos, map, id);
            }
        }
    }

    public void removeArea(int x, int y, int z, int id) {
        var pos = new BlockPos(x, y, z);
        var map = this.sources.get(pos);
        if (map != null) {
            this.removeArea(pos, map, id);
        }
    }

    private void removeArea(BlockPos pos, Int2ObjectOpenHashMap<AC_TriggerArea> map, int id) {
        var area = map.remove(id);
        if (area == null) {
            return;
        }
        if (map.isEmpty()) {
            this.sources.remove(pos);
            this.index.removeSource(pos);
        }
        else if (this.sources.get(pos) == map) {
            // A prior callback may have reloaded/recreated this source, detaching the old map.
            this.index.remove(pos, id);
        }
        this.deactivateArea(area);
    }

    private void deactivateArea(AC_TriggerArea area) {
        TriggerAreaTraversal.forEach(area, (x, y, z) -> {
            // Deactivation eligibility stays live: earlier scripts may add/remove coverage.
            if (!this.index.contains(x, y, z)) {
                this.deactivate.accept(x, y, z);
            }
        });
    }

    public int getTriggerAmount(int x, int y, int z) {
        return this.index.count(x, y, z);
    }

    public boolean isActivated(int x, int y, int z) {
        return this.index.contains(x, y, z);
    }

    /** Preserves authoritative map traversal order and duplicate source messages. */
    public void forEachSourceContaining(int x, int y, int z, Consumer<BlockPos> consumer) {
        for (var source : this.sources.entrySet()) {
            for (var area : source.getValue().values()) {
                if (area.isPointInside(x, y, z)) {
                    consumer.accept(source.getKey());
                }
            }
        }
    }

    public CompoundTag getTagCompound() {
        return TriggerAreaCodec.write(this.sources);
    }

    public void loadFromTagCompound(CompoundTag managerTag) {
        try {
            TriggerAreaCodec.read(managerTag, this.sources);
        }
        finally {
            // The codec retains successfully read sources if malformed NBT fails partway through.
            // Queries must reflect that same authoritative state even if a caller catches the failure.
            this.index.clear();
            for (var source : this.sources.entrySet()) {
                for (var area : source.getValue().int2ObjectEntrySet()) {
                    this.index.put(source.getKey(), area.getIntKey(), area.getValue());
                }
            }
        }
    }

    public TriggerAreaIndex.Statistics queryStatistics() {
        return this.index.statistics();
    }
}
