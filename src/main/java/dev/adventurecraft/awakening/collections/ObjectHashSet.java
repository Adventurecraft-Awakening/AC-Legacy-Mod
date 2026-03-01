package dev.adventurecraft.awakening.collections;

public final class ObjectHashSet<K> extends ObjectCustomHashSet<K> {

    public @Override int keyHashCode(K o) {
        return o.hashCode();
    }

    public @Override boolean keyEquals(K a, K b) {
        return a.equals(b);
    }
}
