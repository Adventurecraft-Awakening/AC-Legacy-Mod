package dev.adventurecraft.awakening.script.scheduling;

import java.util.ArrayList;
import java.util.function.Consumer;

/** A minimum heap whose entries can be removed without scanning the queue. */
public final class IndexedMinHeap<E extends IndexedMinHeap.Entry<E>> {

    public abstract static class Entry<E> implements Comparable<E> {
        private int heapIndex = -1;
    }

    private final ArrayList<E> entries = new ArrayList<>();
    private long comparisons;
    private long removals;
    private long equalityTests;

    public int size() { return this.entries.size(); }
    public long comparisons() { return this.comparisons; }
    public long removals() { return this.removals; }
    public long equalityTests() { return this.equalityTests; }
    public E peek() { return this.entries.isEmpty() ? null : this.entries.getFirst(); }

    public void add(E entry) {
        if (this.index(entry) >= 0) throw new IllegalArgumentException("Entry is already queued");
        int index = this.entries.size();
        this.entries.add(entry);
        this.siftUp(index, entry);
    }

    public E poll() {
        E entry = this.peek();
        if (entry != null) this.removeAt(0);
        return entry;
    }

    /** Detached entries, including callbacks collected by a caller, need no heap work. */
    public boolean remove(E entry) {
        int index = this.index(entry);
        if (index < 0 || index >= this.entries.size() || this.entries.get(index) != entry) return false;
        this.removeAt(index);
        return true;
    }

    /** Matches PriorityQueue.remove when wrapped identifiers make entries value-equal. */
    public E removeFirstEqual(E entry) {
        for (int i = 0; i < this.entries.size(); i++) {
            E candidate = this.entries.get(i);
            this.equalityTests++;
            if (entry.equals(candidate)) {
                this.removeAt(i);
                return candidate;
            }
        }
        return null;
    }

    public void clear(Consumer<? super E> release) {
        for (E entry : this.entries) {
            this.setIndex(entry, -1);
            release.accept(entry);
        }
        this.entries.clear();
    }

    private void removeAt(int index) {
        this.removals++;
        E removed = this.entries.get(index);
        E last = this.entries.removeLast();
        this.setIndex(removed, -1);
        if (index == this.entries.size()) return;
        this.siftDown(index, last);
        if (this.entries.get(index) == last) this.siftUp(index, last);
    }

    private void siftUp(int index, E entry) {
        while (index > 0) {
            int parentIndex = (index - 1) >>> 1;
            E parent = this.entries.get(parentIndex);
            if (this.compare(entry, parent) >= 0) break;
            this.set(index, parent);
            index = parentIndex;
        }
        this.set(index, entry);
    }

    private void siftDown(int index, E entry) {
        int half = this.entries.size() >>> 1;
        while (index < half) {
            int childIndex = (index << 1) + 1;
            E child = this.entries.get(childIndex);
            int right = childIndex + 1;
            if (right < this.entries.size() && this.compare(child, this.entries.get(right)) > 0) {
                childIndex = right;
                child = this.entries.get(right);
            }
            if (this.compare(entry, child) <= 0) break;
            this.set(index, child);
            index = childIndex;
        }
        this.set(index, entry);
    }

    private int compare(E left, E right) {
        this.comparisons++;
        return left.compareTo(right);
    }

    private void set(int index, E entry) {
        this.entries.set(index, entry);
        this.setIndex(entry, index);
    }

    private int index(E entry) { return ((Entry<E>) entry).heapIndex; }
    private void setIndex(E entry, int index) { ((Entry<E>) entry).heapIndex = index; }
}
