package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.Tile;

import java.util.ArrayList;
import java.util.Map;

public class AC_TriggerManager {

    public final Level world;
    private final Map<AC_CoordBlock, Int2ObjectMap<AC_TriggerArea>> triggerAreas;

    public AC_TriggerManager(Level world) {
        this.world = world;
        this.triggerAreas = ((ExWorldProperties) world.levelData).getTriggerAreas();
    }

    public void addArea(int x, int y, int z, AC_TriggerArea area) {
        this.addArea(x, y, z, 0, area);
    }

    public void addArea(int x, int y, int z, int id, AC_TriggerArea area) {
        var coord = new AC_CoordBlock(x, y, z);
        var map = this.triggerAreas.computeIfAbsent(coord, key -> new Int2ObjectOpenHashMap<>());

        AC_TriggerArea previousArea = map.get(id);
        ArrayList<AC_CoordBlock> coords = this.findBlocksToActivate(area);
        map.put(id, area);
        this.activateBlocks(coords);
        if (previousArea != null) {
            this.deactivateArea(previousArea);
        }
    }

    public void removeArea(int x, int y, int z) {
        var coord = new AC_CoordBlock(x, y, z);
        var map = this.triggerAreas.remove(coord);
        if (map != null) {
            for (AC_TriggerArea area : map.values()) {
                this.deactivateArea(area);
            }
        }
    }

    public void removeArea(int x, int y, int z, int id) {
        var coord = new AC_CoordBlock(x, y, z);
        var map = this.triggerAreas.get(coord);
        if (map != null) {
            this.removeArea(coord, map, id);
        }
    }

    private void removeArea(AC_CoordBlock coord, Int2ObjectMap<AC_TriggerArea> map, int id) {
        AC_TriggerArea area = map.get(id);
        if (area == null) {
            return;
        }

        map.remove(id);
        if (map.isEmpty()) {
            this.triggerAreas.remove(coord);
        }

        this.deactivateArea(area);
    }

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

    public void outputTriggerSources(int x, int y, int z) {
        Minecraft.instance.gui.addMessage(String.format("Outputting active triggerings for (%d, %d, %d)", x, y, z));

        for (var entry : this.triggerAreas.entrySet()) {
            for (AC_TriggerArea area : entry.getValue().values()) {
                if (area.isPointInside(x, y, z)) {
                    AC_CoordBlock coord = entry.getKey();
                    Minecraft.instance.gui.addMessage(String.format(
                        "Triggered by (%d, %d, %d)",
                        coord.x,
                        coord.y,
                        coord.z
                    ));
                }
            }
        }
    }

    private ArrayList<AC_CoordBlock> findBlocksToActivate(AC_TriggerArea area) {
        ArrayList<AC_CoordBlock> coords = new ArrayList<>();

        for (int x = area.minX; x <= area.maxX; ++x) {
            for (int y = area.minY; y <= area.maxY; ++y) {
                for (int z = area.minZ; z <= area.maxZ; ++z) {
                    if (this.getTriggerAmount(x, y, z) == 0) {
                        coords.add(new AC_CoordBlock(x, y, z));
                    }
                }
            }
        }

        return coords;
    }

    private void activateBlocks(ArrayList<AC_CoordBlock> coords) {
        for (AC_CoordBlock coord : coords) {
            int id = this.world.getTile(coord.x, coord.y, coord.z);
            var block = (ExBlock) Tile.tiles[id];
            if (id != 0 && block.canBeTriggered()) {
                block.onTriggerActivated(this.world, coord.x, coord.y, coord.z);
            }
        }
    }

    private void deactivateArea(AC_TriggerArea area) {
        for (int x = area.minX; x <= area.maxX; ++x) {
            for (int y = area.minY; y <= area.maxY; ++y) {
                for (int z = area.minZ; z <= area.maxZ; ++z) {
                    if (this.getTriggerAmount(x, y, z) == 0) {
                        int id = this.world.getTile(x, y, z);
                        var block = (ExBlock) Tile.tiles[id];
                        if (id != 0 && block.canBeTriggered()) {
                            block.onTriggerDeactivated(this.world, x, y, z);
                        }
                    }
                }
            }
        }
    }

    public static CompoundTag getTagCompound(Map<AC_CoordBlock, Int2ObjectMap<AC_TriggerArea>> areas) {
        var managerTag = new CompoundTag();
        int coordCount = 0;

        for (var entry : areas.entrySet()) {
            var coordTag = new CompoundTag();
            AC_CoordBlock coord = entry.getKey();
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

    public static void loadFromTagCompound(Map<AC_CoordBlock, Int2ObjectMap<AC_TriggerArea>> areas, CompoundTag managerTag) {
        int coordCount = managerTag.getInt("numCoords");

        for (int i = 0; i < coordCount; ++i) {
            CompoundTag coordTag = managerTag.getCompoundTag(String.format("coord%d", i));
            var coord = new AC_CoordBlock(coordTag.getInt("x"), coordTag.getInt("y"), coordTag.getInt("z"));
            var areaMap = new Int2ObjectOpenHashMap<AC_TriggerArea>();
            areas.put(coord, areaMap);

            int areaCount = coordTag.getInt("numAreas");
            for (int j = 0; j < areaCount; ++j) {
                CompoundTag areaTag = coordTag.getCompoundTag(String.format("area%d", j));
                AC_TriggerArea area = AC_TriggerArea.getFromTagCompound(areaTag);
                areaMap.put(coordTag.getInt("areaID"), area);
            }
        }
    }
}
