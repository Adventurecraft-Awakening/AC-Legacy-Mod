package dev.adventurecraft.awakening.collections.booleans;

import it.unimi.dsi.fastutil.booleans.BooleanIterator;
import it.unimi.dsi.fastutil.booleans.BooleanSpliterator;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface BooleanSet extends Set<Boolean>, BooleanCollection {

    @Override
    @NotNull BooleanIterator iterator();

    @Override
    default @NotNull BooleanSpliterator spliterator() {
        return BooleanCollection.super.spliterator();
    }

    @Deprecated
    default @Override boolean remove(Object o) {
        return BooleanCollection.super.remove(o);
    }

    @Deprecated
    default @Override boolean add(Boolean o) {
        return BooleanCollection.super.add(o);
    }

    @Deprecated
    default @Override boolean contains(Object o) {
        return BooleanCollection.super.contains(o);
    }
}
