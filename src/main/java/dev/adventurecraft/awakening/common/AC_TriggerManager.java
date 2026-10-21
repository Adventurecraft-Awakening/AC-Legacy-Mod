package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.world.trigger.TriggerAreaIndex;
import dev.adventurecraft.awakening.world.trigger.TriggerAreaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.Tile;

/** World-facing trigger callbacks, debug output, and persistence. */
public final class AC_TriggerManager {

    public final Level world;
    private final TriggerAreaTracker areas;

    public AC_TriggerManager(Level world) {
        this.world = world;
        this.areas = new TriggerAreaTracker(this::activateBlock, this::deactivateBlock);
    }

    public void addArea(int x, int y, int z, AC_TriggerArea area) {
        this.addArea(x, y, z, 0, area);
    }

    /** Publishes the replacement before activation, then deactivates the previous area's remainder. */
    public void addArea(int x, int y, int z, int id, AC_TriggerArea area) {
        this.areas.addArea(x, y, z, id, area);
    }

    public void removeArea(int x, int y, int z) {
        this.areas.removeArea(x, y, z);
    }

    public void removeArea(int x, int y, int z, int id) {
        this.areas.removeArea(x, y, z, id);
    }

    public int getTriggerAmount(int x, int y, int z) {
        return this.areas.getTriggerAmount(x, y, z);
    }

    public boolean isActivated(int x, int y, int z) {
        return this.areas.isActivated(x, y, z);
    }

    public void outputTriggerSources(int x, int y, int z) {
        var gui = Minecraft.instance.gui;
        gui.addMessage(String.format("Outputting active triggerings for (%d, %d, %d)", x, y, z));
        this.areas.forEachSourceContaining(x, y, z, pos ->
            gui.addMessage(String.format("Triggered by (%d, %d, %d)", pos.x(), pos.y(), pos.z())));
    }

    private void activateBlock(int x, int y, int z) {
        // Scripts may change later blocks during this traversal. Resolve each block at callback time.
        int id = this.world.getTile(x, y, z);
        if (id != 0) {
            var block = (ExBlock) Tile.tiles[id];
            if (block.canBeTriggered()) {
                block.onTriggerActivated(this.world, x, y, z);
            }
        }
    }

    private void deactivateBlock(int x, int y, int z) {
        int id = this.world.getTile(x, y, z);
        if (id != 0) {
            var block = (ExBlock) Tile.tiles[id];
            if (block.canBeTriggered()) {
                block.onTriggerDeactivated(this.world, x, y, z);
            }
        }
    }

    public CompoundTag getTagCompound() {
        return this.areas.getTagCompound();
    }

    /** Rebuilds derived membership state without invoking trigger callbacks. */
    public void loadFromTagCompound(CompoundTag managerTag) {
        this.areas.loadFromTagCompound(managerTag);
    }

    public TriggerAreaIndex.Statistics getQueryStatistics() {
        return this.areas.queryStatistics();
    }
}
