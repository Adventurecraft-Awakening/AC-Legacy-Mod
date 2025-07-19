package dev.adventurecraft.awakening.collections;

public final class IdentityHashSet<K> extends ObjectCustomHashSet<K> {

    public @Override int keyHashCode(K o) {
        return System.identityHashCode(o);
    }

    public @Override boolean keyEquals(K a, K b) {
        return a == b;
    }
}
