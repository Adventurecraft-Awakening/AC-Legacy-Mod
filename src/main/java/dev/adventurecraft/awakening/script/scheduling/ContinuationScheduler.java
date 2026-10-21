package dev.adventurecraft.awakening.script.scheduling;

import java.util.ArrayList;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.function.Consumer;

/**
 * Publishes captured work at the start of a tick and resumes due work in capture order.
 * Indexed deadlines must stay immutable while waiting. Work without that guarantee retains
 * the old ordered scan, including state changes made by an earlier callback in the scan.
 */
public final class ContinuationScheduler<T> {
    @FunctionalInterface
    public interface DueTest<T> {
        boolean isDue(T continuation, long tick);
    }

    private final DueTest<T> fallbackDueTest;
    private final Consumer<T> resume;
    private final ArrayList<Entry<T>> pending = new ArrayList<>();
    private final PriorityQueue<Entry<T>> waiting = new PriorityQueue<>((left, right) -> {
        int deadline = Long.compare(left.wakeUp, right.wakeUp);
        return deadline != 0 ? deadline : Long.compareUnsigned(left.sequence, right.sequence);
    });
    // An intrusive capture-order list is only traversed when unknown states require a scan.
    private Entry<T> first;
    private Entry<T> last;
    private int activeCount;
    private int unknownCount;
    private long nextSequence;
    private long epoch;
    private long deadlineChecks;
    private long fallbackChecks;
    private long resumed;

    public ContinuationScheduler(DueTest<T> fallbackDueTest, Consumer<T> resume) {
        this.fallbackDueTest = Objects.requireNonNull(fallbackDueTest);
        this.resume = Objects.requireNonNull(resume);
    }

    public void schedule(T continuation, long wakeUp) {
        this.pending.add(new Entry<>(Objects.requireNonNull(continuation), wakeUp, this.nextSequence++, true));
    }

    /** Retains unknown application states until they become resumable or the owner clears them. */
    public void scheduleUnknown(T continuation) {
        this.pending.add(new Entry<>(Objects.requireNonNull(continuation), 0, this.nextSequence++, false));
    }

    public void tick(long tick) {
        for (int index = 0; index < this.pending.size(); ++index) {
            Entry<T> entry = this.pending.get(index);
            entry.previous = this.last;
            if (this.last == null) this.first = entry;
            else this.last.next = entry;
            this.last = entry;
            entry.active = true;
            ++this.activeCount;
            if (entry.indexed) this.waiting.add(entry);
            else ++this.unknownCount;
        }
        this.pending.clear();

        if (this.unknownCount != 0) {
            this.scan(tick);
            return;
        }

        ArrayList<Entry<T>> due = null;
        while (!this.waiting.isEmpty()) {
            ++this.deadlineChecks;
            if (this.waiting.peek().wakeUp > tick) break;
            if (due == null) due = new ArrayList<>();
            Entry<T> entry = this.waiting.remove();
            entry.detached = true;
            due.add(entry);
        }
        if (due == null) return;

        // Deadlines decide eligibility; registration order still decides callback order.
        due.sort((left, right) -> Long.compareUnsigned(left.sequence, right.sequence));
        long generation = this.epoch;
        try {
            for (Entry<T> entry : due) {
                if (this.epoch != generation) break;
                if (entry.active) this.deliver(entry);
            }
        } finally {
            // An unexpected Java failure must not lose callbacks that the old list retained.
            if (this.epoch == generation) {
                for (Entry<T> entry : due) {
                    if (entry.active) {
                        entry.detached = false;
                        this.waiting.add(entry);
                    }
                }
            }
        }
    }

    private void scan(long tick) {
        long generation = this.epoch;
        for (Entry<T> entry = this.first; entry != null && this.epoch == generation;) {
            Entry<T> next = entry.next;
            if (entry.active && !entry.detached) {
                ++this.fallbackChecks;
                if (this.fallbackDueTest.isDue(entry.value, tick)) {
                    if (entry.indexed) this.waiting.remove(entry);
                    this.deliver(entry);
                }
            }
            entry = next;
        }
    }

    private void deliver(Entry<T> entry) {
        T continuation = entry.value;
        if (entry.previous == null) this.first = entry.next;
        else entry.previous.next = entry.next;
        if (entry.next == null) this.last = entry.previous;
        else entry.next.previous = entry.previous;
        --this.activeCount;
        if (!entry.indexed) --this.unknownCount;
        entry.release();
        ++this.resumed;
        this.resume.accept(continuation);
    }

    public void clear() {
        ++this.epoch;
        for (Entry<T> entry : this.pending) entry.release();
        this.pending.clear();
        this.waiting.clear();
        for (Entry<T> entry = this.first; entry != null;) {
            Entry<T> next = entry.next;
            entry.release();
            entry = next;
        }
        this.first = this.last = null;
        this.activeCount = this.unknownCount = 0;
        this.nextSequence = 0;
    }

    public Statistics statistics() {
        return new Statistics(this.activeCount, this.pending.size(), this.unknownCount,
            this.deadlineChecks, this.fallbackChecks, this.resumed);
    }

    public record Statistics(int waiting, int pending, int unknown, long deadlineChecks, long fallbackChecks, long resumed) { }

    private static final class Entry<T> {
        private T value;
        private final long wakeUp;
        private final long sequence;
        private final boolean indexed;
        private Entry<T> previous;
        private Entry<T> next;
        private boolean active;
        private boolean detached;

        private Entry(T value, long wakeUp, long sequence, boolean indexed) {
            this.value = value;
            this.wakeUp = wakeUp;
            this.sequence = sequence;
            this.indexed = indexed;
        }

        private void release() {
            this.value = null;
            this.previous = this.next = null;
            this.active = false;
        }
    }
}
