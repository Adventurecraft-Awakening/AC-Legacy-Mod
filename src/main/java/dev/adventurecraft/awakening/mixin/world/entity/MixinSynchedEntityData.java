package dev.adventurecraft.awakening.mixin.world.entity;

import dev.adventurecraft.awakening.world.entity.DataItem;
import dev.adventurecraft.awakening.world.entity.DataItemArrayMap;
import it.unimi.dsi.fastutil.ints.*;
import net.minecraft.util.Vec3i;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.SynchedEntityData;
import org.spongepowered.asm.mixin.*;

import java.util.Map;

@Mixin(SynchedEntityData.class)
public abstract class MixinSynchedEntityData {

    @Unique private static final Map<Integer, DataItem> EMPTY_MAP = Int2ObjectMaps.emptyMap();

    // Most entities have only one DATA_FLAGS byte entry.
    // Overhead is reduced by using specialized hash-maps.
    @Shadow @Final @Mutable private Map<Integer, DataItem> itemsById = EMPTY_MAP;

    @Shadow private boolean isDirty;

    @Overwrite
    public void define(int id, Object data) {
        if (id > 31) {
            throw new IllegalArgumentException("Data value id is too big with " + id + "! (Max is " + 31 + ")");
        }
        if (this.itemsById.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate id value for " + id + "!");
        }

        var item = switch (data) {
            case Byte d0 -> new DataItem.Byte(id, d0);
            case Short d1 -> new DataItem.Short(id, d1);
            case Integer d2 -> new DataItem.Int(id, d2);
            case Float d3 -> new DataItem.Float(id, d3);
            case String d4 -> new DataItem.JString(id, d4);
            case ItemInstance d5 -> new DataItem.McItemInstance(id, d5);
            case Vec3i d6 -> new DataItem.McVec3i(id, d6);
            default -> throw new IllegalArgumentException("Unknown data type: " + data.getClass());
        };

        // TODO: linked list?
        if (this.itemsById == EMPTY_MAP) {
            this.itemsById = item;
        }
        else {
            this.itemsById = new DataItemArrayMap(this.itemsById.values(), item);
        }
    }

    @Unique
    private DataItem getItem(int id) {
        return this.itemsById.get(id);
    }

    @Overwrite
    public byte getByte(int id) {
        return (byte) this.getItem(id).getData();
    }

    @Overwrite
    public int getInt(int id) {
        return (int) this.getItem(id).getData();
    }

    @Overwrite
    public String getString(int id) {
        return (String) this.getItem(id).getData();
    }

    @Overwrite
    public void set(int index, Object data) {
        DataItem item = this.getItem(index);
        item.setData(data);
        this.isDirty |= item.isDirty();
    }
}