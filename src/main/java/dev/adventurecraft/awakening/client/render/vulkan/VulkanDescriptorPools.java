package dev.adventurecraft.awakening.client.render.vulkan;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import static dev.adventurecraft.awakening.client.render.vulkan.VulkanDevice.check;
import static org.lwjgl.vulkan.VK10.*;

/** Fixed uniform storage and growable texture pools, with fence-safe descriptor recycling. */
final class VulkanDescriptorPools implements AutoCloseable {
    private final VulkanDevice context;
    private final long uniformPool;
    private final List<Long> texturePools = new ArrayList<>();
    private final ArrayDeque<Long> recycled = new ArrayDeque<>();
    private int capacity = 128;
    private int remaining;

    VulkanDescriptorPools(VulkanDevice context, int uniformSets) {
        this.context = context;
        uniformPool = createPool(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC, uniformSets);
    }

    long uniform(long layout) {
        return allocate(uniformPool, layout);
    }

    long texture(long layout) {
        if (!recycled.isEmpty()) {
            return recycled.pop();
        }
        if (remaining == 0) {
            long pool = createPool(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, capacity);
            texturePools.add(pool);
            remaining = capacity;
            capacity = Math.min(capacity * 2, 4096);
        }
        long set = allocate(texturePools.getLast(), layout);
        remaining--;
        return set;
    }

    void recycleTexture(long descriptor) {
        recycled.push(descriptor);
    }

    private long createPool(int type, int count) {
        try (var stack = MemoryStack.stackPush()) {
            var sizes = VkDescriptorPoolSize.calloc(1, stack).type(type).descriptorCount(count);
            var info = VkDescriptorPoolCreateInfo.calloc(stack).sType$Default().maxSets(count).pPoolSizes(sizes);
            var handle = stack.mallocLong(1);
            check(vkCreateDescriptorPool(context.device(), info, null, handle), "create descriptor pool");
            return handle.get(0);
        }
    }

    private long allocate(long pool, long layout) {
        try (var stack = MemoryStack.stackPush()) {
            var info = VkDescriptorSetAllocateInfo.calloc(stack).sType$Default()
                .descriptorPool(pool).pSetLayouts(stack.longs(layout));
            var handle = stack.mallocLong(1);
            check(vkAllocateDescriptorSets(context.device(), info, handle), "allocate descriptor set");
            return handle.get(0);
        }
    }

    @Override
    public void close() {
        recycled.clear();
        for (long pool : texturePools) {
            vkDestroyDescriptorPool(context.device(), pool, null);
        }
        texturePools.clear();
        vkDestroyDescriptorPool(context.device(), uniformPool, null);
    }
}
