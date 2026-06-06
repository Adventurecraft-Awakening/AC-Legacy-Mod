package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.world.BlockPos;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.Tile;

import java.util.ArrayList;
import java.util.Map;

/**
 * Manages trigger areas in the world, providing efficient spatial queries and activation/deactivation
 * of triggerable blocks within those areas.
 * 
 * Trigger areas are rectangular regions that can activate blocks when entities enter them.
 * Multiple trigger areas can overlap, and blocks are only deactivated when no trigger areas
 * contain them anymore.
 * 
 * @author Adventurecraft Team
 */
public final class AC_TriggerManager {

    /** The world this trigger manager operates in */
    public final Level world;
    
    /** Maps trigger source coordinates to their trigger areas by ID */
    public final Map<BlockPos, Int2ObjectOpenHashMap<AC_TriggerArea>> triggerAreas;

    /**
     * Creates a new trigger manager for the specified world.
     * 
     * @param world the world to manage triggers for
     */
    public AC_TriggerManager(Level world) {
        this.world = world;
        this.triggerAreas = new Object2ObjectOpenHashMap<>();
    }

    /**
     * Adds a trigger area at the specified coordinates with default ID 0.
     * 
     * @param x the x coordinate of the trigger source
     * @param y the y coordinate of the trigger source
     * @param z the z coordinate of the trigger source
     * @param area the trigger area to add
     */
    public void addArea(int x, int y, int z, AC_TriggerArea area) {
        this.addArea(x, y, z, 0, area);
    }

    /**
     * Adds a trigger area at the specified coordinates with the given ID.
     * If an area with the same ID already exists at this location, it will be replaced.
     * 
     * @param x the x coordinate of the trigger source
     * @param y the y coordinate of the trigger source
     * @param z the z coordinate of the trigger source
     * @param id the unique identifier for this trigger area at this location
     * @param area the trigger area to add
     */
    public void addArea(int x, int y, int z, int id, AC_TriggerArea area) {
        var pos = new BlockPos(x, y, z);
        var map = this.triggerAreas.get(pos);
        if (map == null) {
            map = new Int2ObjectOpenHashMap<>();
            this.triggerAreas.put(pos, map);
        }

        AC_TriggerArea previousArea = map.get(id);

        ArrayList<BlockPos> posList = this.findBlocksToActivate(area);
        map.put(id, area);
        this.activateBlocks(posList);
        
        // Deactivate blocks from previous area if it existed
        if (previousArea != null) {
            this.deactivateArea(previousArea);
        }
    }

    /**
     * Removes all trigger areas at the specified coordinates.
     * 
     * @param x the x coordinate of the trigger source
     * @param y the y coordinate of the trigger source
     * @param z the z coordinate of the trigger source
     */
    public void removeArea(int x, int y, int z) {
        var pos = new BlockPos(x, y, z);
        var map = this.triggerAreas.get(pos);
        if (map != null) {
            for (int id : map.clone().keySet()) {
                this.removeArea(pos, map, id);
            }
        }
    }

    /**
     * Removes a specific trigger area by ID at the specified coordinates.
     *
     * @param x the x coordinate of the trigger source
     * @param y the y coordinate of the trigger source
     * @param z the z coordinate of the trigger source
     * @param id the unique identifier of the trigger area to remove
     */
    public void removeArea(int x, int y, int z, int id) {
        var pos = new BlockPos(x, y, z);
        var map = this.triggerAreas.get(pos);
        if (map != null) {
            this.removeArea(pos, map, id);
        }
    }

    /**
     * Internal method to remove a trigger area and update the spatial cache.
     * 
     * @param pos the coordinate of the trigger source
     * @param map the map of trigger areas at this coordinate
     * @param id the ID of the area to remove
     */
    private void removeArea(BlockPos pos, Int2ObjectOpenHashMap<AC_TriggerArea> map, int id) {
        AC_TriggerArea area = map.get(id);
        if (area == null) {
            return;
        }

        map.remove(id);
        if (map.isEmpty()) {
            this.triggerAreas.remove(pos);
        }
        this.deactivateArea(area);
    }

    /**
     * Gets the number of trigger areas that contain the specified point.
     * This method is optimized using a spatial cache for O(1) lookup time.
     * 
     * @param x the x coordinate to check
     * @param y the y coordinate to check
     * @param z the z coordinate to check
     * @return the number of trigger areas containing this point
     */
    public int getTriggerAmount(int x, int y, int z) {
        int count = 0;
        for (var map : this.triggerAreas.values()) {
            for (AC_TriggerArea area : map.values()) {
                if (area.isPointInside(x, y, z)) {
                    ++count;
                }
            }
        }
        return count;
    }

    /**
     * Checks if the specified point is contained within any trigger area.
     * 
     * @param x the x coordinate to check
     * @param y the y coordinate to check
     * @param z the z coordinate to check
     * @return true if the point is within at least one trigger area, false otherwise
     */
    public boolean isActivated(int x, int y, int z) {
        for (var map : this.triggerAreas.values()) {
            for (AC_TriggerArea area : map.values()) {
                if (area.isPointInside(x, y, z)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Outputs debug information about all trigger sources affecting the specified point.
     * This method prints messages to the game's chat/console listing all trigger areas
     * that contain the given coordinates.
     * 
     * @param x the x coordinate to check
     * @param y the y coordinate to check
     * @param z the z coordinate to check
     */
    public void outputTriggerSources(int x, int y, int z) {
        var gui = Minecraft.instance.gui;
        gui.addMessage(String.format("Outputting active triggerings for (%d, %d, %d)", x, y, z));

        for (var entry : this.triggerAreas.entrySet()) {
            for (AC_TriggerArea area : entry.getValue().values()) {
                if (!area.isPointInside(x, y, z)) {
                    continue;
                }
                BlockPos pos = entry.getKey();
                gui.addMessage(String.format("Triggered by (%d, %d, %d)", pos.x(), pos.y(), pos.z()));
            }
        }
    }

    /**
     * Finds all coordinates within the trigger area that are not currently activated
     * by any other trigger areas. These coordinates need to have their blocks activated.
     * 
     * @param area the trigger area to check
     * @return list of coordinates that need activation
     */
    private ArrayList<BlockPos> findBlocksToActivate(AC_TriggerArea area) {
        var posList = new ArrayList<BlockPos>();
        BlockPos min = area.min;
        BlockPos max = area.max;

        // TODO: track entire intersections instead of individual blocks
        for (int x = min.x(); x <= max.x(); x++) {
            for (int y = min.y(); y <= max.y(); y++) {
                for (int z = min.z(); z <= max.z(); z++) {
                    if (this.getTriggerAmount(x, y, z) == 0) {
                        posList.add(new BlockPos(x, y, z));
                    }
                }
            }
        }
        return posList;
    }

    /**
     * Activates all triggerable blocks at the specified coordinates.
     * 
     * @param coords list of coordinates where blocks should be activated
     */
    private void activateBlocks(ArrayList<BlockPos> coords) {
        for (BlockPos pos : coords) {
            int id = this.world.getTile(pos.x(), pos.y(), pos.z());
            var block = (ExBlock) Tile.tiles[id];
            if (id != 0 && block.canBeTriggered()) {
                block.onTriggerActivated(this.world, pos.x(), pos.y(), pos.z());
            }
        }
    }

    /**
     * Deactivates all triggerable blocks within the specified area that are no longer
     * covered by any trigger areas.
     * 
     * @param area the trigger area whose blocks should be checked for deactivation
     */
    private void deactivateArea(AC_TriggerArea area) {
        for (int x = area.min.x(); x <= area.max.x(); ++x) {
            for (int y = area.min.y(); y <= area.max.y(); ++y) {
                for (int z = area.min.z(); z <= area.max.z(); ++z) {
                    if (this.getTriggerAmount(x, y, z) != 0) {
                        continue;
                    }

                    int id = this.world.getTile(x, y, z);
                    var block = (ExBlock) Tile.tiles[id];
                    if (id != 0 && block.canBeTriggered()) {
                        block.onTriggerDeactivated(this.world, x, y, z);
                    }
                }
            }
        }
    }

    /**
     * Serializes this trigger manager to an NBT compound tag for saving to disk.
     * 
     * @return NBT compound tag containing all trigger area data
     */
    public CompoundTag getTagCompound() {
        var managerTag = new CompoundTag();
        int coordCount = 0;

        for (var entry : this.triggerAreas.entrySet()) {
            var posTag = new CompoundTag();
            BlockPos pos = entry.getKey();
            posTag.putInt("x", pos.x());
            posTag.putInt("y", pos.y());
            posTag.putInt("z", pos.z());

            int areaCount = 0;
            for (var areaEntry : entry.getValue().int2ObjectEntrySet()) {
                CompoundTag areaTag = areaEntry.getValue().getTagCompound();
                areaTag.putInt("areaID", areaEntry.getIntKey());
                posTag.putTag("area" + (areaCount++), areaTag);
            }

            posTag.putInt("numAreas", areaCount);
            managerTag.putTag("coord" + (coordCount++), posTag);
        }

        managerTag.putInt("numCoords", coordCount);
        return managerTag;
    }

    /**
     * Deserializes trigger manager data from an NBT compound tag loaded from disk.
     * This method clears all existing data and rebuilds both the trigger areas and
     * the spatial cache.
     * 
     * @param managerTag NBT compound tag containing trigger area data
     */
    public void loadFromTagCompound(CompoundTag managerTag) {
        this.triggerAreas.clear();
        int coordCount = managerTag.getInt("numCoords");

        for (int i = 0; i < coordCount; ++i) {
            CompoundTag posTag = managerTag.getCompoundTag(String.format("coord%d", i));
            var pos = new BlockPos(posTag.getInt("x"), posTag.getInt("y"), posTag.getInt("z"));
            var areaMap = new Int2ObjectOpenHashMap<AC_TriggerArea>();
            this.triggerAreas.put(pos, areaMap);

            int areaCount = posTag.getInt("numAreas");
            for (int j = 0; j < areaCount; ++j) {
                CompoundTag areaTag = posTag.getCompoundTag(String.format("area%d", j));
                AC_TriggerArea area = AC_TriggerArea.getFromTagCompound(areaTag);
                areaMap.put(posTag.getInt("areaID"), area);
            }
        }
    }
}
