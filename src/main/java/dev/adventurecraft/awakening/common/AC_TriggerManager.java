package dev.adventurecraft.awakening.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import dev.adventurecraft.awakening.extension.block.ExBlock;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.util.io.AbstractTag;
import net.minecraft.util.io.CompoundTag;
import net.minecraft.world.World;

public class AC_TriggerManager {
    public World world;
    public HashMap<AC_CoordBlock, Int2ObjectOpenHashMap<AC_TriggerArea>> triggerAreas;

    public AC_TriggerManager(World var1) {
        this.world = var1;
        this.triggerAreas = new HashMap<>();
    }

    public void addArea(int var1, int var2, int var3, AC_TriggerArea var4) {
        this.addArea(var1, var2, var3, 0, var4);
    }

    public void addArea(int var1, int var2, int var3, int var4, AC_TriggerArea var5) {
        AC_CoordBlock var6 = new AC_CoordBlock(var1, var2, var3);
        Int2ObjectOpenHashMap<AC_TriggerArea> var7 = this.triggerAreas.get(var6);
        if (var7 == null) {
            var7 = new Int2ObjectOpenHashMap<>();
            this.triggerAreas.put(var6, var7);
        }

        AC_TriggerArea var8 = var7.get(var4);
        ArrayList<AC_CoordBlock> var9 = this.findBlocksToActivate(var5);
        var7.put(var4, var5);
        this.activateBlocks(var9);
        if (var8 != null) {
            this.deactivateArea(var8);
        }
    }

    public void removeArea(int var1, int var2, int var3) {
        AC_CoordBlock var4 = new AC_CoordBlock(var1, var2, var3);
        Int2ObjectOpenHashMap<AC_TriggerArea> var5 = this.triggerAreas.get(var4);
        if (var5 != null) {
            for (int var7 : var5.clone().keySet()) {
                this.removeArea(var4, var5, var7);
            }
        }
    }

    public void removeArea(int var1, int var2, int var3, int var4) {
        AC_CoordBlock var5 = new AC_CoordBlock(var1, var2, var3);
        Int2ObjectOpenHashMap<AC_TriggerArea> var6 = this.triggerAreas.get(var5);
        if (var6 != null) {
            this.removeArea(var5, var6, var4);
        }
    }

    private void removeArea(AC_CoordBlock var1, Int2ObjectOpenHashMap<AC_TriggerArea> var2, int var3) {
        AC_TriggerArea var4 = var2.get(var3);
        if (var4 != null) {
            var2.remove(var3);
            if (var2.isEmpty()) {
                this.triggerAreas.remove(var1);
            }

            this.deactivateArea(var4);
        }
    }

    public int getTriggerAmount(int var1, int var2, int var3) {
        int var4 = 0;

        for (Int2ObjectOpenHashMap<AC_TriggerArea> var6 : this.triggerAreas.values()) {
            for (AC_TriggerArea var8 : var6.values()) {
                if (var8.isPointInside(var1, var2, var3)) {
                    ++var4;
                }
            }
        }

        return var4;
    }

    public boolean isActivated(int var1, int var2, int var3) {
        for (Int2ObjectOpenHashMap<AC_TriggerArea> var5 : this.triggerAreas.values()) {
            for (AC_TriggerArea var7 : var5.values()) {
                if (var7.isPointInside(var1, var2, var3)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void outputTriggerSources(int var1, int var2, int var3) {
        Minecraft.instance.overlay.addChatMessage(String.format("Outputting active triggerings for (%d, %d, %d)", var1, var2, var3));

        for (Entry<AC_CoordBlock, Int2ObjectOpenHashMap<AC_TriggerArea>> var5 : this.triggerAreas.entrySet()) {
            for (AC_TriggerArea var8 : var5.getValue().values()) {
                if (var8.isPointInside(var1, var2, var3)) {
                    AC_CoordBlock var9 = var5.getKey();
                    Minecraft.instance.overlay.addChatMessage(String.format("Triggered by (%d, %d, %d)", var9.x, var9.y, var9.z));
                }
            }
        }
    }

    private ArrayList<AC_CoordBlock> findBlocksToActivate(AC_TriggerArea var1) {
        ArrayList<AC_CoordBlock> var2 = new ArrayList<>();

        for (int var3 = var1.minX; var3 <= var1.maxX; ++var3) {
            for (int var4 = var1.minY; var4 <= var1.maxY; ++var4) {
                for (int var5 = var1.minZ; var5 <= var1.maxZ; ++var5) {
                    if (this.getTriggerAmount(var3, var4, var5) == 0) {
                        var2.add(new AC_CoordBlock(var3, var4, var5));
                    }
                }
            }
        }

        return var2;
    }

    private void activateBlocks(ArrayList<AC_CoordBlock> var1) {
        for (AC_CoordBlock var3 : var1) {
            int var4 = this.world.getBlockId(var3.x, var3.y, var3.z);
            ExBlock block = (ExBlock) Block.BY_ID[var4];
            if (var4 != 0 && block.canBeTriggered()) {
                block.onTriggerActivated(this.world, var3.x, var3.y, var3.z);
            }
        }
    }

    private void deactivateArea(AC_TriggerArea var1) {
        for (int var2 = var1.minX; var2 <= var1.maxX; ++var2) {
            for (int var3 = var1.minY; var3 <= var1.maxY; ++var3) {
                for (int var4 = var1.minZ; var4 <= var1.maxZ; ++var4) {
                    if (this.getTriggerAmount(var2, var3, var4) == 0) {
                        int var5 = this.world.getBlockId(var2, var3, var4);
                        ExBlock block = (ExBlock)Block.BY_ID[var5];
                        if (var5 != 0 && block.canBeTriggered()) {
                            block.onTriggerDeactivated(this.world, var2, var3, var4);
                        }
                    }
                }
            }
        }

    }

    public CompoundTag getTagCompound() {
        CompoundTag var1 = new CompoundTag();
        int var2 = 0;

        for (Entry<AC_CoordBlock, Int2ObjectOpenHashMap<AC_TriggerArea>> var4 : this.triggerAreas.entrySet()) {
            CompoundTag var5 = new CompoundTag();
            AC_CoordBlock var6 = var4.getKey();
            var5.put("x", var6.x);
            var5.put("y", var6.y);
            var5.put("z", var6.z);
            int var7 = 0;

            for (Int2ObjectMap.Entry<AC_TriggerArea> var9 : var4.getValue().int2ObjectEntrySet()) {
                CompoundTag var10 = var9.getValue().getTagCompound();
                var10.put("areaID", var9.getIntKey());
                var5.put(String.format("area%d", var7++), (AbstractTag) var10);
            }

            var5.put("numAreas", var7);
            var1.put(String.format("coord%d", var2++), (AbstractTag) var5);
        }

        var1.put("numCoords", var2);
        return var1;
    }

    public void loadFromTagCompound(CompoundTag var1) {
        this.triggerAreas.clear();
        int var2 = var1.getInt("numCoords");

        for (int var3 = 0; var3 < var2; ++var3) {
            CompoundTag var4 = var1.getCompoundTag(String.format("coord%d", var3));
            AC_CoordBlock var5 = new AC_CoordBlock(var4.getInt("x"), var4.getInt("y"), var4.getInt("z"));
            Int2ObjectOpenHashMap<AC_TriggerArea> var6 = new Int2ObjectOpenHashMap<>();
            this.triggerAreas.put(var5, var6);
            int var7 = var4.getInt("numAreas");

            for (int var8 = 0; var8 < var7; ++var8) {
                CompoundTag var9 = var4.getCompoundTag(String.format("area%d", var8));
                AC_TriggerArea var10 = AC_TriggerArea.getFromTagCompound(var9);
                var6.put(var4.getInt("areaID"), var10);
            }
        }
    }
}
