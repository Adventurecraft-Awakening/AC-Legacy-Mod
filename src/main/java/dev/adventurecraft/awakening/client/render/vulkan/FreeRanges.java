package dev.adventurecraft.awakening.client.render.vulkan;

import java.util.ArrayList;
import java.util.List;

/** Ordered first-fit byte ranges shared by buffer and image memory pools. */
final class FreeRanges {
    private final long capacity;
    private final List<Range> ranges = new ArrayList<>();

    FreeRanges(long capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Range capacity must be positive");
        }
        this.capacity = capacity;
        ranges.add(new Range(0, capacity));
    }

    /** Returns an aligned offset, or -1 when no free range can satisfy the request. */
    long allocate(long size, long alignment) {
        if (size <= 0 || alignment <= 0) {
            throw new IllegalArgumentException("Allocation size and alignment must be positive");
        }
        for (int i = 0; i < ranges.size(); i++) {
            Range range = ranges.get(i);
            long remainder = range.offset % alignment;
            long padding = remainder == 0 ? 0 : alignment - remainder;
            // Compare lengths before adding offsets so oversized requests cannot overflow.
            if (size > range.size || padding > range.size - size) {
                continue;
            }
            long offset = range.offset + padding;
            long end = offset + size;
            long rangeEnd = range.offset + range.size;
            ranges.remove(i);
            if (padding > 0) {
                ranges.add(i++, new Range(range.offset, padding));
            }
            if (end < rangeEnd) {
                ranges.add(i, new Range(end, rangeEnd - end));
            }
            return offset;
        }
        return -1;
    }

    /** Returns an allocated range and joins any immediately adjacent free ranges. */
    void release(long offset, long size) {
        if (offset < 0 || size <= 0 || size > capacity || offset > capacity - size) {
            throw new IllegalArgumentException("Released range exceeds its allocation pool");
        }
        int index = 0;
        while (index < ranges.size() && ranges.get(index).offset < offset) {
            index++;
        }
        Range before = index > 0 ? ranges.get(index - 1) : null;
        Range after = index < ranges.size() ? ranges.get(index) : null;
        long end = offset + size;
        if ((before != null && before.offset + before.size > offset) || (after != null && end > after.offset)) {
            throw new IllegalArgumentException("Released range overlaps a free range");
        }
        if (before != null && before.offset + before.size == offset) {
            offset = before.offset;
            size += before.size;
            ranges.remove(--index);
        }
        if (after != null && end == after.offset) {
            size += after.size;
            ranges.remove(index);
        }
        ranges.add(index, new Range(offset, size));
    }

    private record Range(long offset, long size) { }
}
