package dev.adventurecraft.awakening.client.render.vulkan;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import java.nio.ByteBuffer;
import java.util.Map;

import static org.lwjgl.vulkan.VK10.*;

/** Aligned mapped records and their dynamic descriptor, reused only after the owning slot's fence. */
final class VulkanUniformStorage implements AutoCloseable {
    private final VulkanDevice context;
    private final VulkanBindings bindings;
    private final int set;
    private final int bytes;
    final int stride;
    private VulkanBuffer buffer;
    private int capacity;
    long descriptor;

    VulkanUniformStorage(VulkanDevice context, VulkanBindings bindings, int set, int bytes) {
        this.context = context;
        this.bindings = bindings;
        this.set = set;
        this.bytes = bytes;
        long alignment = bindings.uniformAlignment();
        stride = Math.toIntExact((bytes + alignment - 1) / alignment * alignment);
    }

    void write(Map<VulkanDrawConstants.Block, Integer> blocks) {
        int count = Math.max(blocks.size(), 1);
        if (buffer == null || count > capacity) grow(count);
        ByteBuffer mapping = buffer.mapped();
        for (var entry : blocks.entrySet()) {
            mapping.position(Math.multiplyExact(entry.getValue(), stride));
            entry.getKey().writeTo(mapping);
        }
    }

    private void grow(int count) {
        int newCapacity = Integer.highestOneBit(Math.max(count - 1, 63)) << 1;
        VulkanBuffer replacement = context.hostBuffer(Math.multiplyExact((long) newCapacity, stride), VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT);
        try (var stack = MemoryStack.stackPush()) {
            if (descriptor == VK_NULL_HANDLE) descriptor = bindings.allocateUniform(set);
            var info = VkDescriptorBufferInfo.calloc(1, stack).buffer(replacement.handle()).offset(0).range(bytes);
            var write = VkWriteDescriptorSet.calloc(1, stack).sType$Default().dstSet(descriptor).dstBinding(0)
                .descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC).descriptorCount(1).pBufferInfo(info);
            vkUpdateDescriptorSets(context.device(), write, null);
        } catch (RuntimeException | Error error) {
            replacement.close();
            throw error;
        }
        if (buffer != null) buffer.close();
        buffer = replacement;
        capacity = newCapacity;
    }

    @Override public void close() {
        if (buffer != null) buffer.close();
        buffer = null;
    }
}
