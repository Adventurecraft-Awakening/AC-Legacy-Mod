package dev.adventurecraft.awakening.collections.booleans;

import it.unimi.dsi.fastutil.booleans.BooleanIterator;
import it.unimi.dsi.fastutil.booleans.BooleanPredicate;
import it.unimi.dsi.fastutil.booleans.BooleanSpliterator;
import it.unimi.dsi.fastutil.booleans.BooleanSpliterators;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;

public interface BooleanCollection extends Collection<Boolean> {

    @Override
    @NotNull BooleanIterator iterator();

    @Override
    default @NotNull BooleanSpliterator spliterator() {
        return BooleanSpliterators.asSpliterator(
            this.iterator(),
            it.unimi.dsi.fastutil.Size64.sizeOf(this),
            BooleanSpliterators.COLLECTION_SPLITERATOR_CHARACTERISTICS
        );
    }

    boolean add(boolean key);

    boolean contains(boolean key);

    boolean remove(boolean key);

    @Deprecated
    default @Override boolean add(Boolean key) {
        return add((key).booleanValue());
    }

    @Deprecated
    default @Override boolean contains(Object key) {
        if (key == null) {
            return false;
        }
        return contains(((Boolean) (key)).booleanValue());
    }

    @Deprecated
    default @Override boolean remove(Object key) {
        if (key == null) {
            return false;
        }
        return this.remove(((Boolean) key).booleanValue());
    }

    boolean[] toBooleanArray();

    boolean[] toArray(boolean[] a);

    boolean addAll(BooleanCollection c);

    boolean containsAll(BooleanCollection c);

    boolean removeAll(BooleanCollection c);

    @Deprecated
    default @Override boolean removeIf(@NotNull Predicate<? super Boolean> filter) {
        return this.removeIf(filter instanceof BooleanPredicate f ? f : filter::test);
    }

    default boolean removeIf(final BooleanPredicate filter) {
        Objects.requireNonNull(filter);
        boolean removed = false;
        BooleanIterator i = this.iterator();
        while (i.hasNext()) {
            if (filter.test(i.nextBoolean())) {
                i.remove();
                removed = true;
            }
        }
        return removed;
    }

    boolean retainAll(BooleanCollection c);
}
