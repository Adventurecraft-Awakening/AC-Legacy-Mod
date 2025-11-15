package dev.adventurecraft.awakening.world.entity;

import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.world.entity.SynchedEntityData;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;

import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;

public record DataItemArrayMap(SynchedEntityData.DataItem... items)
    implements Int2ObjectMap<SynchedEntityData.DataItem> {

    public DataItemArrayMap(Collection<SynchedEntityData.DataItem> items, SynchedEntityData.DataItem item) {
        this(items.toArray(new SynchedEntityData.DataItem[items.size() + 1]));
        this.items[this.items.length - 1] = item;
    }

    @Override
    public int size() {
        return this.items.length;
    }

    @Override
    public boolean isEmpty() {
        return this.size() == 0;
    }

    @Override
    public boolean containsValue(Object o) {
        return ArrayUtils.contains(this.items, o);
    }

    @Override
    public void putAll(@NotNull Map<? extends Integer, ? extends SynchedEntityData.DataItem> map) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void defaultReturnValue(SynchedEntityData.DataItem rv) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SynchedEntityData.DataItem defaultReturnValue() {
        return null;
    }

    @Override
    public ObjectSet<Entry<SynchedEntityData.DataItem>> int2ObjectEntrySet() {
        // this depends on SynchedEntityData.DataItem implementing Entry
        return new ObjectArraySet<>(this.items);
    }

    @Override
    public @NotNull IntSet keySet() {
        return new AbstractIntSet() {
            SynchedEntityData.DataItem[] items() {
                return DataItemArrayMap.this.items;
            }

            @Override
            public int size() {
                return this.items().length;
            }

            @Override
            public @NotNull IntIterator iterator() {
                return new IntIterator() {
                    int pos;

                    @Override
                    public boolean hasNext() {
                        return pos < items().length;
                    }

                    @Override
                    public int nextInt() {
                        if (!hasNext()) {
                            throw new NoSuchElementException();
                        }
                        return items()[pos++].getId();
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    @Override
    public @NotNull ObjectCollection<SynchedEntityData.DataItem> values() {
        return new ObjectArraySet<>(this.items);
    }

    @Override
    public SynchedEntityData.DataItem get(int key) {
        for (SynchedEntityData.DataItem item : this.items) {
            if (item.getId() == key) {
                return item;
            }
        }
        return null;
    }

    @Override
    public boolean containsKey(int key) {
        for (SynchedEntityData.DataItem item : this.items) {
            if (item.getId() == key) {
                return true;
            }
        }
        return false;
    }
}