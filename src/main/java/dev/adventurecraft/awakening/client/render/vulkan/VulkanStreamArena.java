package dev.adventurecraft.awakening.client.render.vulkan;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import static org.lwjgl.vulkan.VK10.*;

/** Mapped frame storage. The owning frame resets its bump pointers only after its fence completes. */
final class VulkanStreamArena implements AutoCloseable {
    private static final int PAGE_BYTES=1024*1024;
    private final VulkanDevice context;
    private final List<Page> pages=new ArrayList<>();
    private int page;

    VulkanStreamArena(VulkanDevice context) { this.context=context; }
    record Allocation(long buffer,long offset,int bytes,ByteBuffer mapping) { }

    Allocation reserve(int bytes,int alignment) {
        if(bytes<=0) throw new IllegalArgumentException("Stream allocation must be positive");
        while(true) {
            if(page==pages.size()) {
                int capacity=Math.max(PAGE_BYTES,Math.addExact(bytes,alignment));
                var buffer=context.hostBuffer(capacity,VK_BUFFER_USAGE_VERTEX_BUFFER_BIT|VK_BUFFER_USAGE_INDEX_BUFFER_BIT);
                pages.add(new Page(buffer,buffer.mapped()));
            }
            Page target=pages.get(page);
            int offset=Math.toIntExact((target.cursor+(long)alignment-1)/alignment*alignment);
            if(bytes<=target.mapping.capacity()-offset) {
                target.cursor=offset+bytes;
                return new Allocation(target.buffer.handle(),offset,bytes,
                    target.mapping.slice(offset,bytes).order(ByteOrder.nativeOrder()));
            }
            page++;
        }
    }

    void reset() {
        page=0;
        for(var target:pages) target.cursor=0;
    }
    int pageCount() { return pages.size(); }
    private static final class Page {
        final VulkanBuffer buffer;
        final ByteBuffer mapping;
        int cursor;
        Page(VulkanBuffer buffer,ByteBuffer mapping) { this.buffer=buffer; this.mapping=mapping; }
    }
    @Override public void close() {
        for(var target:pages) target.buffer.close();
        pages.clear();
    }
}
