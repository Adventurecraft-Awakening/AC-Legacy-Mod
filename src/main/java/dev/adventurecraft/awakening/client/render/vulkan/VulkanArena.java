package dev.adventurecraft.awakening.client.render.vulkan;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/** Suballocates pooled Vulkan buffers; slices are returned only after their last GPU submission. */
final class VulkanArena implements AutoCloseable {
    private static final long PAGE_BYTES = 4L * 1024 * 1024;
    private final VulkanDevice context;
    private final int usage;
    private final int memoryFlags;
    private final List<Page> pages = new ArrayList<>();

    VulkanArena(VulkanDevice context, int usage, int memoryFlags) {
        this.context = context;
        this.usage = usage;
        this.memoryFlags = memoryFlags;
    }

    Slice allocate(long bytes, long alignment) {
        if (bytes <= 0 || alignment <= 0) {
            throw new IllegalArgumentException("Buffer slice size and alignment must be positive");
        }
        for (Page page : pages) {
            Slice slice = page.allocate(bytes, alignment);
            if (slice != null) {
                return slice;
            }
        }
        long capacity = Math.max(PAGE_BYTES, Long.highestOneBit(Math.addExact(bytes, alignment) - 1) << 1);
        Page page = new Page(context.buffer(capacity, usage, memoryFlags));
        pages.add(page);
        return page.allocate(bytes, alignment);
    }

    int pageCount() {
        return pages.size();
    }

    @Override
    public void close() {
        for (Page page : pages) {
            page.buffer.close();
        }
        pages.clear();
    }

    final class Slice implements AutoCloseable {
        private final Page page;
        private final long offset;
        private final long size;
        private boolean released;

        private Slice(Page page, long offset, long size) {
            this.page = page;
            this.offset = offset;
            this.size = size;
        }

        long handle() {
            return page.buffer.handle();
        }

        long offset() {
            return offset;
        }

        long size() {
            return size;
        }

        long deviceAddress() {
            ensureAvailable();
            return page.buffer.deviceAddress() + offset;
        }

        /** Direct access to this slice only; the arena must use host-visible coherent memory. */
        ByteBuffer mapped() {
            ensureAvailable();
            if (!hostCoherent()) {
                throw new IllegalStateException("Direct Vulkan slice mapping requires host coherent memory");
            }
            return page.buffer.mapped().slice(Math.toIntExact(offset), Math.toIntExact(size))
                .order(ByteOrder.nativeOrder());
        }

        boolean hostCoherent() {
            return page.buffer.hostCoherent();
        }

        void upload(ByteBuffer bytes) {
            if (bytes.remaining() > size) {
                throw new IllegalArgumentException("Upload exceeds buffer slice");
            }
            page.buffer.upload(offset, bytes);
        }

        void pageUpload(long relativeOffset, ByteBuffer bytes) {
            if (relativeOffset < 0 || relativeOffset + bytes.remaining() > size) {
                throw new IllegalArgumentException("Upload exceeds slice");
            }
            page.buffer.upload(offset + relativeOffset, bytes);
        }

        private void ensureAvailable() {
            if (released) throw new IllegalStateException("Vulkan buffer slice is released");
        }

        @Override
        public void close() {
            if (released) {
                return;
            }
            released = true;
            page.release(offset, size);
        }
    }

    private final class Page {
        private final VulkanBuffer buffer;
        private final FreeRanges free;

        private Page(VulkanBuffer buffer) {
            this.buffer = buffer;
            free = new FreeRanges(buffer.size());
        }

        private Slice allocate(long bytes, long alignment) {
            long offset = free.allocate(bytes, alignment);
            return offset < 0 ? null : new Slice(this, offset, bytes);
        }

        private void release(long offset, long size) {
            free.release(offset, size);
        }
    }
}
