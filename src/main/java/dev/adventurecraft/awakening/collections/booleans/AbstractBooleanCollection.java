package dev.adventurecraft.awakening.collections.booleans;

import it.unimi.dsi.fastutil.booleans.BooleanArrays;
import it.unimi.dsi.fastutil.booleans.BooleanIterator;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractCollection;
import java.util.Collection;

public abstract class AbstractBooleanCollection extends AbstractCollection<Boolean> implements BooleanCollection {

    @Override
    public abstract @NotNull BooleanIterator iterator();

    public @Override boolean add(boolean k) {
        throw new UnsupportedOperationException();
    }

    public @Override boolean contains(boolean k) {
        BooleanIterator i = this.iterator();
        while (i.hasNext()) {
            if (k == i.nextBoolean()) {
                return true;
            }
        }
        return false;
    }

    public @Override boolean remove(boolean k) {
        BooleanIterator i = this.iterator();
        while (i.hasNext()) {
            if (k == i.nextBoolean()) {
                i.remove();
                return true;
            }
        }
        return false;
    }


    @Deprecated
    public @Override boolean add(Boolean key) {
        return BooleanCollection.super.add(key);
    }

    @Deprecated
    public @Override boolean contains(Object key) {
        return BooleanCollection.super.contains(key);
    }

    @Deprecated
    public @Override boolean remove(Object key) {
        return BooleanCollection.super.remove(key);
    }

    public
    @Override boolean[] toArray(boolean[] a) {
        int size = this.size();
        if (a == null) {
            a = new boolean[size];
        }
        else if (a.length < size) {
            a = java.util.Arrays.copyOf(a, size);
        }
        BooleanIterators.unwrap(this.iterator(), a);
        return a;
    }

    public
    @Override  boolean[] toBooleanArray() {
        int size = this.size();
        if (size == 0) {
            return BooleanArrays.EMPTY_ARRAY;
        }
        boolean[] a = new boolean[size];
        BooleanIterators.unwrap(this.iterator(), a);
        return a;
    }

    public @Override boolean addAll(final BooleanCollection c) {
        boolean added = false;
        BooleanIterator i = c.iterator();
        while (i.hasNext()) {
            if (this.add(i.nextBoolean())) {
                added = true;
            }
        }
        return added;
    }

    public @Override boolean addAll(@NotNull Collection<? extends Boolean> c) {
        return c instanceof BooleanCollection o ? this.addAll(o) : super.addAll(c);
    }

    public @Override boolean containsAll(BooleanCollection c) {
        BooleanIterator i = c.iterator();
        while (i.hasNext()) {
            if (!this.contains(i.nextBoolean())) {
                return false;
            }
        }
        return true;
    }

    public @Override boolean containsAll(@NotNull Collection<?> c) {
        return c instanceof BooleanCollection o ? this.containsAll(o) : super.containsAll(c);
    }

    public @Override boolean removeAll(final BooleanCollection c) {
        boolean removed = false;
        BooleanIterator i = c.iterator();
        while (i.hasNext()) {
            if (this.remove(i.nextBoolean())) {
                removed = true;
            }
        }
        return removed;
    }

    public @Override boolean removeAll(@NotNull Collection<?> c) {
        return c instanceof BooleanCollection o ? removeAll(o) : super.removeAll(c);
    }

    public @Override boolean retainAll(BooleanCollection c) {
        boolean removed = false;
        BooleanIterator i = iterator();
        while (i.hasNext()) {
            if (!c.contains(i.nextBoolean())) {
                i.remove();
                removed = true;
            }
        }
        return removed;
    }

    public @Override boolean retainAll(@NotNull Collection<?> c) {
        return c instanceof BooleanCollection o ? retainAll(o) : super.retainAll(c);
    }

    public @Override String toString() {
        var s = new StringBuilder();
        BooleanIterator i = this.iterator();
        boolean first = true;
        s.append("{");
        while (i.hasNext()) {
            if (first) {
                first = false;
            }
            else {
                s.append(", ");
            }
            s.append(i.nextBoolean());
        }
        s.append("}");
        return s.toString();
    }
}
