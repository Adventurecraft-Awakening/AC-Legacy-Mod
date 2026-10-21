package dev.adventurecraft.awakening.client.render.vulkan;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;
import java.nio.LongBuffer;
import static dev.adventurecraft.awakening.client.render.vulkan.VulkanDevice.check;
import static org.lwjgl.vulkan.VK10.*;

/** Owns an image allocation and its full-range view; texture snapshots retain references explicitly. */
final class VulkanImage implements AutoCloseable {
    private final VulkanDevice context;
    final int aspect;
    final int format;
    final int mipLevels;
    final int width;
    final int height;
    private int references;
    long handle;
    private VulkanImageAllocator.Allocation allocation;
    long view;

    private VulkanImage(VulkanDevice context, int width, int height, int format, int aspect, int mipLevels) {
        this.context = context;
        this.width = width;
        this.height = height;
        this.format = format;
        this.aspect = aspect;
        this.mipLevels = mipLevels;
    }

    void retain() {
        if (handle == VK_NULL_HANDLE) throw new IllegalStateException("Vulkan image is closed");
        references = Math.incrementExact(references);
    }

    void release() {
        if (references == 0) throw new IllegalStateException("Vulkan image has no retained references");
        if (--references == 0) close();
    }

    @Override
    public void close() {
        if (view != VK_NULL_HANDLE) vkDestroyImageView(context.device(), view, null);
        if (handle != VK_NULL_HANDLE) vkDestroyImage(context.device(), handle, null);
        if (allocation != null) allocation.close();
        allocation = null;
        view = handle = VK_NULL_HANDLE;
    }


    static VulkanImage create(VulkanDevice context, int width, int height, int format, int usage, int aspect, int mipLevels) {
        VulkanImage image = new VulkanImage(context, width, height, format, aspect, mipLevels);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkImageCreateInfo info = VkImageCreateInfo.calloc(stack).sType$Default()
                .imageType(VK_IMAGE_TYPE_2D).format(format).mipLevels(mipLevels).arrayLayers(1)
                .samples(VK_SAMPLE_COUNT_1_BIT).tiling(VK_IMAGE_TILING_OPTIMAL).usage(usage)
                .sharingMode(VK_SHARING_MODE_EXCLUSIVE).initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
            info.extent().set(width, height, 1);
            LongBuffer result = stack.mallocLong(1);
            check(vkCreateImage(context.device(), info, null, result), "create terrain image");
            image.handle = result.get(0);
            image.allocation = context.imageAllocator().allocate(image.handle);
            check(vkBindImageMemory(context.device(),image.handle,image.allocation.memory(),image.allocation.offset),"bind optimal image memory");
            VkImageViewCreateInfo view = VkImageViewCreateInfo.calloc(stack).sType$Default()
                .image(image.handle).viewType(VK_IMAGE_VIEW_TYPE_2D).format(format);
            view.subresourceRange().aspectMask(aspect).baseMipLevel(0).levelCount(mipLevels).baseArrayLayer(0).layerCount(1);
            check(vkCreateImageView(context.device(), view, null, result), "create terrain image view");
            image.view = result.get(0);
            return image;
        } catch (RuntimeException | Error error) {
            image.close();
            throw error;
        }
    }

    static void barrier(VkCommandBuffer command, VulkanImage image, int oldLayout, int newLayout,
                        int srcAccess, int dstAccess, int srcStage, int dstStage) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkImageMemoryBarrier.Buffer barrier = VkImageMemoryBarrier.calloc(1, stack).sType$Default()
                .oldLayout(oldLayout).newLayout(newLayout).srcAccessMask(srcAccess).dstAccessMask(dstAccess)
                .srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED).dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
                .image(image.handle);
            barrier.subresourceRange().aspectMask(image.aspect).baseMipLevel(0).levelCount(image.mipLevels).baseArrayLayer(0).layerCount(1);
            vkCmdPipelineBarrier(command, srcStage, dstStage, 0, null, null, barrier);
        }
    }

}
