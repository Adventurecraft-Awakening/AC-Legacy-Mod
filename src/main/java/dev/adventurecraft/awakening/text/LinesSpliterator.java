package dev.adventurecraft.awakening.text;

import dev.adventurecraft.awakening.primitives.IntRange;

import java.util.Spliterator;
import java.util.function.Consumer;

public class LinesSpliterator implements Spliterator<IntRange> {

    private final CharSequence value;
    private int index;

    public LinesSpliterator(CharSequence value, int start) {
        this.value = value;
        this.index = start;
    }

    public LinesSpliterator(CharSequence value) {
        this(value, 0);
    }

    protected int fence() {
        return this.value.length();
    }

    private int indexOfLineSeparator(int start) {
        for (int i = start; i < this.fence(); ++i) {
            char ch = this.value.charAt(i);
            if (ch == '\n' || ch == '\r') {
                return i;
            }
        }
        return this.fence();
    }

    private int skipLineSeparator(int start) {
        if (start >= this.fence()) {
            return this.fence();
        }
        if (this.value.charAt(start) == '\r') {
            int next = start + 1;
            if (next < this.fence() && this.value.charAt(next) == '\n') {
                return next + 1;
            }
        }
        return start + 1;
    }

    private IntRange next() {
        int start = this.index;
        int end = this.indexOfLineSeparator(start);
        this.index = this.skipLineSeparator(end);
        return new IntRange(start, end);
    }

    @Override
    public boolean tryAdvance(Consumer<? super IntRange> action) {
        if (this.index != this.fence()) {
            action.accept(this.next());
            return true;
        }
        return false;
    }

    @Override
    public void forEachRemaining(Consumer<? super IntRange> action) {
        while (this.index != this.fence()) {
            action.accept(this.next());
        }
    }

    @Override
    public Spliterator<IntRange> trySplit() {
        int half = this.fence() + this.index >>> 1;
        int mid = this.skipLineSeparator(this.indexOfLineSeparator(half));
        if (mid >= this.fence()) {
            return null;
        }
        int start = this.index;
        this.index = mid;
        return new Limited(this.value, start, mid);
    }

    @Override
    public long estimateSize() {
        return this.fence() - this.index + 1;
    }

    @Override
    public int characteristics() {
        return Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.IMMUTABLE;
    }

    public static final class Limited extends LinesSpliterator {
        private final int fence;

        public Limited(CharSequence value, int start, int end) {
            super(value, start);
            this.fence = end;
        }

        @Override
        protected int fence() {
            return this.fence;
        }
    }
}
