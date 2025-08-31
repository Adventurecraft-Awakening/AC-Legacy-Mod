package dev.adventurecraft.awakening.collections;

import java.util.Collection;

public final class IdentityHashSet<K> extends ObjectCustomHashSet<K> {

    public IdentityHashSet() {
        super();
    }

    public IdentityHashSet(Collection<? extends K> c) {
        super(c);
    }

    public @Override int keyHashCode(K o) {
        return System.identityHashCode(o);
    }

    public @Override boolean keyEquals(K a, K b) {
        return a == b;
    }
}
