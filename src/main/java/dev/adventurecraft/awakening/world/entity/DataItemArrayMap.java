package dev.adventurecraft.awakening.world.entity;

import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;

public record DataItemArrayMap(DataItem... items) implements Int2ObjectMap<DataItem> {

    public DataItemArrayMap(Collection<DataItem> items, DataItem item) {
        this(items.toArray(new DataItem[items.size() + 1]));
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
    public void putAll(@NotNull Map<? extends Integer, ? extends DataItem> map) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void defaultReturnValue(DataItem rv) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DataItem defaultReturnValue() {
        return null;
    }

    @Override
    public ObjectSet<Entry<DataItem>> int2ObjectEntrySet() {
        // this depends on DataItem implementing Entry
        return new ObjectArraySet<>(this.items);
    }

    @Override
    public @NotNull IntSet keySet() {
        return new AbstractIntSet() {
            @Override
            public int size() {
                return DataItemArrayMap.this.items.length;
            }

            @Override
            public @NotNull IntIterator iterator() {
                return new IntIterator() {
                    int pos;

                    @Override
                    public boolean hasNext() {
                        return pos < DataItemArrayMap.this.items.length;
                    }

                    @Override
                    public int nextInt() {
                        if (!hasNext()) {
                            throw new NoSuchElementException();
                        }
                        return DataItemArrayMap.this.items[pos++].getIntKey();
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
    public @NotNull ObjectCollection<DataItem> values() {
        return new ObjectArraySet<>(this.items);
    }

    @Override
    public DataItem get(int key) {
        for (DataItem item : this.items) {
            if (key == item.getIntKey()) {
                return item;
            }
        }
        return null;
    }

    @Override
    public boolean containsKey(int key) {
        for (DataItem item : this.items) {
            if (key == item.getIntKey()) {
                return true;
            }
        }
        return false;
    }
}