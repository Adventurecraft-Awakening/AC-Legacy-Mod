package dev.adventurecraft.awakening.collections;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectSet;

import java.util.Collection;
import java.util.Set;

public class StringArraySet extends ObjectArraySet<String> implements StringSet {

    public StringArraySet(String[] a) {
        super(a);
    }

    public StringArraySet() {
    }

    public StringArraySet(int capacity) {
        super(capacity);
    }

    public StringArraySet(ObjectCollection<String> c) {
        super(c);
    }

    public StringArraySet(Collection<? extends String> c) {
        super(c);
    }

    public StringArraySet(ObjectSet<String> c) {
        super(c);
    }

    public StringArraySet(Set<? extends String> c) {
        super(c);
    }

    public StringArraySet(String[] a, int size) {
        super(a, size);
    }
}
