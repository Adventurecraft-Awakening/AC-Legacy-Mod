package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.block.ExBlock;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
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
 * @since 0.5.5
 */
public final class AC_TriggerManager {

    /** The world this trigger manager operates in */
    public final Level world;
    
    /** Maps trigger source coordinates to their trigger areas by ID */
    public final Map<Coord, Int2ObjectOpenHashMap<AC_TriggerArea>> triggerAreas;
    
    /** Spatial cache mapping coordinates to the number of trigger areas containing them */
    private final Object2IntOpenHashMap<Coord> triggerCounts;

    /**
     * Creates a new trigger manager for the specified world.
     * 
     * @param world the world to manage triggers for
     */
    public AC_TriggerManager(Level world) {
        this.world = world;
        this.triggerAreas = new Object2ObjectOpenHashMap<>();
        this.triggerCounts = new Object2IntOpenHashMap<>();
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
        var coord = new Coord(x, y, z);
        var map = this.triggerAreas.get(coord);
        if (map == null) {
            map = new Int2ObjectOpenHashMap<>();
            this.triggerAreas.put(coord, map);
        }

        AC_TriggerArea previousArea = map.get(id);
        
        // Remove previous area from cache if it exists
        if (previousArea != null) {
            this.updateTriggerCounts(previousArea, -1);
        }
        
        // Add new area to cache and find blocks to activate
        this.updateTriggerCounts(area, 1);
        ArrayList<Coord> coords = this.findBlocksToActivate(area);
        map.put(id, area);
        this.activateBlocks(coords);
        
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
        var coord = new Coord(x, y, z);
        var map = this.triggerAreas.get(coord);
        if (map != null) {
            for (int id : map.clone().keySet()) {
                this.removeArea(coord, map, id);
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
        var coord = new Coord(x, y, z);
        var map = this.triggerAreas.get(coord);
        if (map != null) {
            this.removeArea(coord, map, id);
        }
    }

    /**
     * Internal method to remove a trigger area and update the spatial cache.
     * 
     * @param coord the coordinate of the trigger source
     * @param map the map of trigger areas at this coordinate
     * @param id the ID of the area to remove
     */
    private void removeArea(Coord coord, Int2ObjectOpenHashMap<AC_TriggerArea> map, int id) {
        AC_TriggerArea area = map.get(id);
        if (area == null) {
            return;
        }

        // Remove from cache and deactivate area
        this.updateTriggerCounts(area, -1);
        map.remove(id);
        if (map.isEmpty()) {
            this.triggerAreas.remove(coord);
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
        return this.triggerCounts.getInt(new Coord(x, y, z));
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
        return this.getTriggerAmount(x, y, z) > 0;
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
                Coord coord = entry.getKey();
                gui.addMessage(String.format("Triggered by (%d, %d, %d)", coord.x, coord.y, coord.z));
            }
        }
    }

    /**
     * Updates the trigger count cache for all coordinates within the specified area.
     * 
     * @param area the trigger area to update counts for
     * @param delta the change in count (+1 for adding, -1 for removing)
     */
    private void updateTriggerCounts(AC_TriggerArea area, int delta) {
        for (int x = area.min.x; x <= area.max.x; x++) {
            for (int y = area.min.y; y <= area.max.y; y++) {
                for (int z = area.min.z; z <= area.max.z; z++) {
                    var coord = new Coord(x, y, z);
                    int currentCount = this.triggerCounts.getInt(coord);
                    int newCount = currentCount + delta;
                    
                    if (newCount <= 0) {
                        this.triggerCounts.removeInt(coord);
                    } else {
                        this.triggerCounts.put(coord, newCount);
                    }
                }
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
    private ArrayList<Coord> findBlocksToActivate(AC_TriggerArea area) {
        var coords = new ArrayList<Coord>();
        Coord min = area.min;
        Coord max = area.max;

        for (int x = min.x; x <= max.x; x++) {
            for (int y = min.y; y <= max.y; y++) {
                for (int z = min.z; z <= max.z; z++) {
                    if (this.getTriggerAmount(x, y, z) == 0) {
                        coords.add(new Coord(x, y, z));
                    }
                }
            }
        }
        return coords;
    }

    /**
     * Activates all triggerable blocks at the specified coordinates.
     * 
     * @param coords list of coordinates where blocks should be activated
     */
    private void activateBlocks(ArrayList<Coord> coords) {
        for (Coord coord : coords) {
            int id = this.world.getTile(coord.x, coord.y, coord.z);
            var block = (ExBlock) Tile.tiles[id];
            if (id != 0 && block.canBeTriggered()) {
                block.onTriggerActivated(this.world, coord.x, coord.y, coord.z);
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
        for (int x = area.min.x; x <= area.max.x; ++x) {
            for (int y = area.min.y; y <= area.max.y; ++y) {
                for (int z = area.min.z; z <= area.max.z; ++z) {
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
            var coordTag = new CompoundTag();
            Coord coord = entry.getKey();
            coordTag.putInt("x", coord.x);
            coordTag.putInt("y", coord.y);
            coordTag.putInt("z", coord.z);

            int areaCount = 0;
            for (var areaEntry : entry.getValue().int2ObjectEntrySet()) {
                CompoundTag areaTag = areaEntry.getValue().getTagCompound();
                areaTag.putInt("areaID", areaEntry.getIntKey());
                coordTag.putTag("area" + (areaCount++), areaTag);
            }

            coordTag.putInt("numAreas", areaCount);
            managerTag.putTag("coord" + (coordCount++), coordTag);
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
        this.triggerCounts.clear();
        int coordCount = managerTag.getInt("numCoords");

        for (int i = 0; i < coordCount; ++i) {
            CompoundTag coordTag = managerTag.getCompoundTag(String.format("coord%d", i));
            var coord = new Coord(coordTag.getInt("x"), coordTag.getInt("y"), coordTag.getInt("z"));
            var areaMap = new Int2ObjectOpenHashMap<AC_TriggerArea>();
            this.triggerAreas.put(coord, areaMap);

            int areaCount = coordTag.getInt("numAreas");
            for (int j = 0; j < areaCount; ++j) {
                CompoundTag areaTag = coordTag.getCompoundTag(String.format("area%d", j));
                AC_TriggerArea area = AC_TriggerArea.getFromTagCompound(areaTag);
                areaMap.put(coordTag.getInt("areaID"), area);
                
                // Rebuild spatial cache for this area
                this.updateTriggerCounts(area, 1);
            }
        }
    }
}
