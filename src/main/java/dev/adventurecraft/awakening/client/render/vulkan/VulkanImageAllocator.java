package dev.adventurecraft.awakening.client.render.vulkan;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.util.ArrayList;
import java.util.List;

import static dev.adventurecraft.awakening.client.render.vulkan.VulkanDevice.check;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK11.*;

/** Pools optimal images separately from buffers, honoring alignment and dedicated-allocation requirements. */
final class VulkanImageAllocator implements AutoCloseable {
    private static final long BLOCK_BYTES = 16L * 1024 * 1024;
    private final VulkanDevice context;
    private final List<Block> blocks = new ArrayList<>();

    VulkanImageAllocator(VulkanDevice context) {
        this.context = context;
    }

    Allocation allocate(long image) {
        try (var stack = MemoryStack.stackPush()) {
            var requirements = VkMemoryRequirements.malloc(stack);
            boolean dedicated = false;
            if (context.device().getCapabilities().Vulkan11) {
                var dedicatedRequirements = VkMemoryDedicatedRequirements.calloc(stack).sType$Default();
                var info = VkImageMemoryRequirementsInfo2.calloc(stack).sType$Default().image(image);
                var result = VkMemoryRequirements2.calloc(stack).sType$Default().pNext(dedicatedRequirements.address());
                vkGetImageMemoryRequirements2(context.device(), info, result);
                requirements.set(result.memoryRequirements());
                dedicated = dedicatedRequirements.requiresDedicatedAllocation()
                    || (dedicatedRequirements.prefersDedicatedAllocation() && requirements.size() >= BLOCK_BYTES / 2);
            } else {
                vkGetImageMemoryRequirements(context.device(), image, requirements);
            }
            int type = context.memoryType(requirements.memoryTypeBits(), VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
            long size = requirements.size();
            long alignment = requirements.alignment();
            dedicated |= size >= BLOCK_BYTES;
            if (!dedicated) {
                for (var block : blocks) {
                    if (!block.dedicated && block.type == type) {
                        Allocation allocation = block.allocate(size, alignment);
                        if (allocation != null) {
                            return allocation;
                        }
                    }
                }
            }
            Block block = create(type, dedicated ? size : BLOCK_BYTES, dedicated, image, size);
            blocks.add(block);
            return block.allocate(size, alignment);
        }
    }

    private Block create(int type, long size, boolean dedicated, long image, long requestedSize) {
        try (var stack = MemoryStack.stackPush()) {
            var info = VkMemoryAllocateInfo.calloc(stack).sType$Default().allocationSize(size).memoryTypeIndex(type);
            if (dedicated && context.device().getCapabilities().Vulkan11) {
                var dedicatedInfo = VkMemoryDedicatedAllocateInfo.calloc(stack).sType$Default().image(image);
                info.pNext(dedicatedInfo.address());
            }
            var handle = stack.mallocLong(1);
            int result = vkAllocateMemory(context.device(), info, null, handle);
            if (!dedicated && (result == VK_ERROR_OUT_OF_DEVICE_MEMORY || result == VK_ERROR_OUT_OF_HOST_MEMORY)) {
                return create(type, requestedSize, true, image, requestedSize);
            }
            check(result, "allocate optimal image memory");
            return new Block(handle.get(0), type, size, dedicated);
        }
    }

    final class Allocation implements AutoCloseable {
        private final Block block;
        final long offset;
        final long size;
        private boolean closed;

        Allocation(Block block, long offset, long size) {
            this.block = block;
            this.offset = offset;
            this.size = size;
        }

        long memory() {
            return block.memory;
        }

        @Override
        public void close() {
            if (closed) {
                return;
            }
            closed = true;
            block.release(offset, size);
            if (block.live == 0 && (block.dedicated || spareBlock(block))) {
                blocks.remove(block);
                vkFreeMemory(context.device(), block.memory, null);
            }
        }
    }

    private boolean spareBlock(Block empty) {
        for (var block : blocks) {
            if (block != empty && !block.dedicated && block.type == empty.type) {
                return true;
            }
        }
        return false;
    }

    private final class Block {
        final long memory;
        final int type;
        final boolean dedicated;
        final FreeRanges free;
        int live;

        Block(long memory, int type, long size, boolean dedicated) {
            this.memory = memory;
            this.type = type;
            this.dedicated = dedicated;
            free = new FreeRanges(size);
        }

        Allocation allocate(long size, long alignment) {
            long offset = free.allocate(size, alignment);
            if (offset < 0) {
                return null;
            }
            live++;
            return new Allocation(this, offset, size);
        }

        void release(long offset, long size) {
            free.release(offset, size);
            live--;
        }
    }

    @Override
    public void close() {
        for (var block : blocks) {
            vkFreeMemory(context.device(), block.memory, null);
        }
        blocks.clear();
    }
}
