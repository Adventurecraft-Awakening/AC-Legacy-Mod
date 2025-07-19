package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.block.ExBlock;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.Tile;

import java.util.ArrayList;
import java.util.Map;

public final class AC_TriggerManager {

    public final Level world;
    public final Map<Coord, Int2ObjectOpenHashMap<AC_TriggerArea>> triggerAreas;

    public AC_TriggerManager(Level world) {
        this.world = world;
        this.triggerAreas = new Object2ObjectOpenHashMap<>();
    }

    public void addArea(int x, int y, int z, AC_TriggerArea area) {
        this.addArea(x, y, z, 0, area);
    }

    public void addArea(int x, int y, int z, int id, AC_TriggerArea area) {
        var coord = new Coord(x, y, z);
        var map = this.triggerAreas.get(coord);
        if (map == null) {
            map = new Int2ObjectOpenHashMap<>();
            this.triggerAreas.put(coord, map);
        }

        AC_TriggerArea previousArea = map.get(id);
        ArrayList<Coord> coords = this.findBlocksToActivate(area);
        map.put(id, area);
        this.activateBlocks(coords);
        if (previousArea != null) {
            this.deactivateArea(previousArea);
        }
    }

    public void removeArea(int x, int y, int z) {
        var coord = new Coord(x, y, z);
        var map = this.triggerAreas.get(coord);
        if (map != null) {
            for (int id : map.clone().keySet()) {
                this.removeArea(coord, map, id);
            }
        }
    }

    public void removeArea(int x, int y, int z, int id) {
        var coord = new Coord(x, y, z);
        var map = this.triggerAreas.get(coord);
        if (map != null) {
            this.removeArea(coord, map, id);
        }
    }

    private void removeArea(Coord coord, Int2ObjectOpenHashMap<AC_TriggerArea> map, int id) {
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

    // TODO: optimize this calculation and respective callsites
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

    private void activateBlocks(ArrayList<Coord> coords) {
        for (Coord coord : coords) {
            int id = this.world.getTile(coord.x, coord.y, coord.z);
            var block = (ExBlock) Tile.tiles[id];
            if (id != 0 && block.canBeTriggered()) {
                block.onTriggerActivated(this.world, coord.x, coord.y, coord.z);
            }
        }
    }

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

    public void loadFromTagCompound(CompoundTag managerTag) {
        this.triggerAreas.clear();
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
            }
        }
    }
}
