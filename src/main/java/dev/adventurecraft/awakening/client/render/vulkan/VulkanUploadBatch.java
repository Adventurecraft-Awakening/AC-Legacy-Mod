package dev.adventurecraft.awakening.client.render.vulkan;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;

/** Typed transfers and their staging allocations, retired together after the submitting fence. */
final class VulkanUploadBatch implements AutoCloseable {
    private sealed interface Operation permits BufferCopy, TextureUpload, TexturePatch, InitializeTargets {
        void record(VkCommandBuffer command);
    }

    private record BufferCopy(VulkanArena.Slice source, VulkanArena.Slice target, long targetOffset,
                              long bytes) implements Operation {
        @Override
        public void record(VkCommandBuffer command) {
            try (var stack = MemoryStack.stackPush()) {
                var copy = VkBufferCopy.calloc(1, stack).srcOffset(source.offset())
                    .dstOffset(target.offset() + targetOffset).size(bytes);
                vkCmdCopyBuffer(command, source.handle(), target.handle(), copy);
            }
        }
    }

    private record TextureUpload(VulkanImage image, VulkanArena.Slice staged, int uploadLevels,
                                 boolean explicitLevels) implements Operation {
        @Override
        public void record(VkCommandBuffer command) {
            try (var stack = MemoryStack.stackPush()) {
                imageBarrier(command, image, VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                    0, VK_ACCESS_TRANSFER_WRITE_BIT, VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT, VK_PIPELINE_STAGE_TRANSFER_BIT);
                VkBufferImageCopy.Buffer copies = VkBufferImageCopy.calloc(uploadLevels, stack);
                long levelOffset = 0;
                for (int level = 0; level < uploadLevels; level++) {
                    int mipWidth = Math.max(1, image.width >> level);
                    int mipHeight = Math.max(1, image.height >> level);
                    VkBufferImageCopy copy = copies.get(level).bufferOffset(staged.offset() + levelOffset);
                    copy.imageSubresource().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT).mipLevel(level)
                        .baseArrayLayer(0).layerCount(1);
                    copy.imageExtent().set(mipWidth, mipHeight, 1);
                    levelOffset += Math.multiplyExact(Math.multiplyExact((long) mipWidth, mipHeight), 4);
                }
                vkCmdCopyBufferToImage(command, staged.handle(), image.handle,
                    VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, copies);
                if (explicitLevels) {
                    imageBarrier(command, image, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                        VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL, VK_ACCESS_TRANSFER_WRITE_BIT,
                        VK_ACCESS_SHADER_READ_BIT, VK_PIPELINE_STAGE_TRANSFER_BIT, VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT);
                } else {
                    finishTextureUpload(command, image, image.width, image.height, stack);
                }
            }
        }
    }

    record Patch(int level, int x, int y, int width, int height, VulkanArena.Slice pixels) { }

    private record TexturePatch(VulkanImage source, VulkanImage image, List<Patch> updates, boolean copy,
                                boolean regenerateMipmaps) implements Operation {
        TexturePatch {
            updates = List.copyOf(updates);
        }

        @Override
        public void record(VkCommandBuffer command) {
            try (var stack = MemoryStack.stackPush()) {
                if (copy) {
                    imageBarrier(command, source, VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
                        VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, VK_ACCESS_SHADER_READ_BIT, VK_ACCESS_TRANSFER_READ_BIT,
                        VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT, VK_PIPELINE_STAGE_TRANSFER_BIT);
                    imageBarrier(command, image, VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                        0, VK_ACCESS_TRANSFER_WRITE_BIT, VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT, VK_PIPELINE_STAGE_TRANSFER_BIT);
                    for (int level = 0; level < image.mipLevels; level++) {
                        var region = VkImageCopy.calloc(1, stack);
                        region.srcSubresource().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT).mipLevel(level).layerCount(1);
                        region.dstSubresource().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT).mipLevel(level).layerCount(1);
                        region.extent().set(Math.max(1, source.width >> level), Math.max(1, source.height >> level), 1);
                        vkCmdCopyImage(command, source.handle, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                            image.handle, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, region);
                    }
                    imageBarrier(command, source, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                        VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL, VK_ACCESS_TRANSFER_READ_BIT, VK_ACCESS_SHADER_READ_BIT,
                        VK_PIPELINE_STAGE_TRANSFER_BIT, VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT);
                    // Preserve ordering between copying the old mip chain and patching dirty rectangles.
                    imageBarrier(command, image, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                        VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VK_ACCESS_TRANSFER_WRITE_BIT, VK_ACCESS_TRANSFER_WRITE_BIT,
                        VK_PIPELINE_STAGE_TRANSFER_BIT, VK_PIPELINE_STAGE_TRANSFER_BIT);
                } else {
                    imageBarrier(command, image, VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
                        VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VK_ACCESS_SHADER_READ_BIT, VK_ACCESS_TRANSFER_WRITE_BIT,
                        VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT, VK_PIPELINE_STAGE_TRANSFER_BIT);
                }
                for (int i = 0; i < updates.size(); i++) {
                    if (i > 0) {
                        imageBarrier(command, image, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                            VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VK_ACCESS_TRANSFER_WRITE_BIT, VK_ACCESS_TRANSFER_WRITE_BIT,
                            VK_PIPELINE_STAGE_TRANSFER_BIT, VK_PIPELINE_STAGE_TRANSFER_BIT);
                    }
                    Patch region = updates.get(i);
                    VulkanArena.Slice pixels = region.pixels();
                    var upload = VkBufferImageCopy.calloc(1, stack).bufferOffset(pixels.offset());
                    upload.imageSubresource().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT).mipLevel(region.level()).layerCount(1);
                    upload.imageOffset().set(region.x(), region.y(), 0);
                    upload.imageExtent().set(region.width(), region.height(), 1);
                    vkCmdCopyBufferToImage(command, pixels.handle(), image.handle,
                        VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, upload);
                }
                if (regenerateMipmaps) {
                    finishTextureUpload(command, image, source.width, source.height, stack);
                } else {
                    imageBarrier(command, image, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                        VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL, VK_ACCESS_TRANSFER_WRITE_BIT, VK_ACCESS_SHADER_READ_BIT,
                        VK_PIPELINE_STAGE_TRANSFER_BIT, VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT);
                }
            }
        }
    }

    private record InitializeTargets(VulkanImage[] attachments) implements Operation {
        @Override
        public void record(VkCommandBuffer command) {
            initialize(command, attachments);
        }
    }

    private final List<Operation> operations = new ArrayList<>();
    private final List<VulkanArena.Slice> staging = new ArrayList<>();
    private final List<VulkanImage> images = new ArrayList<>();
    private boolean bufferWrites;

    boolean isEmpty() {
        return operations.isEmpty();
    }

    void retain(VulkanArena.Slice slice) {
        staging.add(slice);
    }

    void discard(VulkanArena.Slice slice) {
        staging.remove(slice);
        slice.close();
    }

    void buffer(VulkanArena.Slice source, VulkanArena.Slice target, long offset, long bytes) {
        operations.add(new BufferCopy(source, target, offset, bytes));
        bufferWrites = true;
    }

    void texture(VulkanImage image, VulkanArena.Slice staged, int levels, boolean explicitLevels) {
        retainImage(image);
        operations.add(new TextureUpload(image, staged, levels, explicitLevels));
    }

    void patch(VulkanImage source, VulkanImage image, List<Patch> patches, boolean copy, boolean regenerateMipmaps) {
        retainImage(source);
        if (image != source) {
            retainImage(image);
        }
        operations.add(new TexturePatch(source, image, patches, copy, regenerateMipmaps));
    }

    private void retainImage(VulkanImage image) {
        image.retain();
        images.add(image);
    }

    void initialize(VulkanImage... attachments) {
        operations.add(new InitializeTargets(attachments.clone()));
    }

    void record(VkCommandBuffer command, MemoryStack stack) {
        for (var operation : operations) {
            operation.record(command);
        }
        if (bufferWrites) {
            var barrier = VkMemoryBarrier.calloc(1, stack).sType$Default().srcAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT)
                .dstAccessMask(VK_ACCESS_VERTEX_ATTRIBUTE_READ_BIT | VK_ACCESS_INDEX_READ_BIT);
            vkCmdPipelineBarrier(command, VK_PIPELINE_STAGE_TRANSFER_BIT, VK_PIPELINE_STAGE_VERTEX_INPUT_BIT,
                0, barrier, null, null);
        }
    }

    void submitted(VulkanCommandSlot slot) {
        for (var slice : staging) {
            slot.defer(slice::close);
        }
        for (var image : images) {
            slot.defer(image::release);
        }
        images.clear();
        staging.clear();
        operations.clear();
        bufferWrites = false;
    }

    @Override
    public void close() {
        for (var slice : staging) {
            slice.close();
        }
        for (var image : images) {
            image.release();
        }
        images.clear();
        staging.clear();
        operations.clear();
        bufferWrites = false;
    }

    private static void initialize(VkCommandBuffer command, VulkanImage[] attachments) {
        try (var stack = MemoryStack.stackPush()) {
            for (var target : attachments) {
                VulkanImage.barrier(command, target, VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                    0, VK_ACCESS_TRANSFER_WRITE_BIT, VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT, VK_PIPELINE_STAGE_TRANSFER_BIT);
                var range = VkImageSubresourceRange.calloc(1, stack).aspectMask(target.aspect).levelCount(1).layerCount(1);
                if (target.aspect == VK_IMAGE_ASPECT_COLOR_BIT) {
                    var clear = VkClearColorValue.calloc(stack);
                    clear.float32(3, 1);
                    vkCmdClearColorImage(command, target.handle, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, clear, range);
                } else {
                    var clear = VkClearDepthStencilValue.calloc(stack).depth(1).stencil(0);
                    vkCmdClearDepthStencilImage(command, target.handle, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, clear, range);
                }
                VulkanImage.barrier(command, target, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                    VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, VK_ACCESS_TRANSFER_WRITE_BIT, VK_ACCESS_TRANSFER_READ_BIT,
                    VK_PIPELINE_STAGE_TRANSFER_BIT, VK_PIPELINE_STAGE_TRANSFER_BIT);
            }
        }
    }

    private static void finishTextureUpload(VkCommandBuffer command, VulkanImage image, int width, int height,
                                            MemoryStack stack) {
        VkImageMemoryBarrier.Buffer barrier = VkImageMemoryBarrier.calloc(1, stack).sType$Default()
            .srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED).dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED).image(image.handle);
        barrier.subresourceRange().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT).levelCount(1).baseArrayLayer(0).layerCount(1);
        VkImageBlit.Buffer blit = VkImageBlit.calloc(1, stack);
        blit.srcSubresource().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT).baseArrayLayer(0).layerCount(1);
        blit.dstSubresource().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT).baseArrayLayer(0).layerCount(1);
        int mipWidth = width;
        int mipHeight = height;
        for (int level = 1; level < image.mipLevels; level++) {
            barrier.subresourceRange().baseMipLevel(level - 1);
            barrier.oldLayout(VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL).newLayout(VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL)
                .srcAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT).dstAccessMask(VK_ACCESS_TRANSFER_READ_BIT);
            vkCmdPipelineBarrier(command, VK_PIPELINE_STAGE_TRANSFER_BIT, VK_PIPELINE_STAGE_TRANSFER_BIT,
                0, null, null, barrier);
            int nextWidth = Math.max(1, mipWidth / 2);
            int nextHeight = Math.max(1, mipHeight / 2);
            blit.srcSubresource().mipLevel(level - 1);
            blit.dstSubresource().mipLevel(level);
            blit.srcOffsets(0).set(0, 0, 0);
            blit.srcOffsets(1).set(mipWidth, mipHeight, 1);
            blit.dstOffsets(0).set(0, 0, 0);
            blit.dstOffsets(1).set(nextWidth, nextHeight, 1);
            vkCmdBlitImage(command, image.handle, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                image.handle, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, blit, VK_FILTER_LINEAR);
            barrier.oldLayout(VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL).newLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)
                .srcAccessMask(VK_ACCESS_TRANSFER_READ_BIT).dstAccessMask(VK_ACCESS_SHADER_READ_BIT);
            vkCmdPipelineBarrier(command, VK_PIPELINE_STAGE_TRANSFER_BIT, VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT,
                0, null, null, barrier);
            mipWidth = nextWidth;
            mipHeight = nextHeight;
        }
        barrier.subresourceRange().baseMipLevel(image.mipLevels - 1);
        barrier.oldLayout(VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL).newLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)
            .srcAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT).dstAccessMask(VK_ACCESS_SHADER_READ_BIT);
        vkCmdPipelineBarrier(command, VK_PIPELINE_STAGE_TRANSFER_BIT, VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT,
            0, null, null, barrier);
    }

    private static void imageBarrier(VkCommandBuffer command, VulkanImage image, int oldLayout, int newLayout,
                                     int srcAccess, int dstAccess, int srcStage, int dstStage) {
        VulkanImage.barrier(command, image, oldLayout, newLayout, srcAccess, dstAccess, srcStage, dstStage);
    }
}
