package dev.adventurecraft.awakening.client.render.vulkan;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;

import static dev.adventurecraft.awakening.client.render.vulkan.VulkanDevice.check;
import static org.lwjgl.vulkan.VK10.*;

/** Color/depth attachments, framebuffer and lazy readback storage for one frame slot. */
final class VulkanFrameTargets implements AutoCloseable {
    private final VulkanDevice context;
    private final long renderPass;
    private final long worldRenderPass;
    private final int reactiveFormat;
    private final boolean sampledDepth;
    VulkanImage color;
    VulkanImage world;
    VulkanImage reactive;
    VulkanImage depth;
    private VulkanBuffer colorReadback;
    private VulkanBuffer depthReadback;
    long framebuffer;
    long worldFramebuffer;
    int width;
    int height;

    VulkanFrameTargets(VulkanDevice context, long renderPass, long worldRenderPass, int reactiveFormat, boolean sampledDepth) {
        this.context = context;
        this.renderPass = renderPass;
        this.worldRenderPass = worldRenderPass;
        this.reactiveFormat = reactiveFormat;
        this.sampledDepth = sampledDepth;
    }

    void resize(int width, int height, int colorFormat, int depthFormat) {
        close();
        try {
            int transferUsage = VK_IMAGE_USAGE_TRANSFER_SRC_BIT | VK_IMAGE_USAGE_TRANSFER_DST_BIT;
            color = VulkanImage.create(context, width, height, colorFormat,
                VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT | transferUsage
                    | (worldRenderPass != VK_NULL_HANDLE ? VK_IMAGE_USAGE_STORAGE_BIT | VK_IMAGE_USAGE_SAMPLED_BIT : 0), VK_IMAGE_ASPECT_COLOR_BIT, 1);
            depth = VulkanImage.create(context, width, height, depthFormat,
                VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT | transferUsage | (sampledDepth ? VK_IMAGE_USAGE_SAMPLED_BIT : 0), VK_IMAGE_ASPECT_DEPTH_BIT, 1);
            try (var stack = MemoryStack.stackPush()) {
                var info = VkFramebufferCreateInfo.calloc(stack).sType$Default().renderPass(renderPass)
                    .pAttachments(stack.longs(color.view, depth.view)).width(width).height(height).layers(1);
                var handle = stack.mallocLong(1);
                check(vkCreateFramebuffer(context.device(), info, null, handle), "create frame framebuffer");
                framebuffer = handle.get(0);
                if (worldRenderPass != VK_NULL_HANDLE) {
                    world = VulkanImage.create(context, width, height, VK_FORMAT_R16G16B16A16_SFLOAT,
                        VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT | VK_IMAGE_USAGE_SAMPLED_BIT | VK_IMAGE_USAGE_STORAGE_BIT | transferUsage,
                        VK_IMAGE_ASPECT_COLOR_BIT, 1);
                    reactive = VulkanImage.create(context, width, height, reactiveFormat,
                        VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT | VK_IMAGE_USAGE_SAMPLED_BIT | transferUsage,
                        VK_IMAGE_ASPECT_COLOR_BIT, 1);
                    info.renderPass(worldRenderPass).pAttachments(stack.longs(world.view, reactive.view, depth.view));
                    check(vkCreateFramebuffer(context.device(), info, null, handle), "create HDR framebuffer");
                    worldFramebuffer = handle.get(0);
                }
            }
            this.width = width;
            this.height = height;
        } catch (RuntimeException | Error error) {
            close();
            throw error;
        }
    }

    /** The caller establishes ordering with previous rendering and presentation. */
    void preserveFrom(VkCommandBuffer command, VulkanFrameTargets previous, boolean preserveColor, boolean preserveDepth,
                      boolean preserveWorld) {
        if (previous.width != width || previous.height != height) return;
        if (preserveColor) copyAttachment(command, previous.color, color);
        if (preserveDepth) copyAttachment(command, previous.depth, depth);
        if (preserveWorld) copyAttachment(command, previous.world, world);
    }

    void resolveWorldFrom(VkCommandBuffer command, VulkanImage result) {
        copyAttachment(command, result, world);
    }

    /** Depth remains native-sized for later draws, retained attachments and screenshot readback. */
    void upscaleDepthFrom(VkCommandBuffer command, VulkanFrameTargets source) {
        try (var stack = MemoryStack.stackPush()) {
            VulkanImage.barrier(command, depth, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                VK_ACCESS_TRANSFER_READ_BIT, VK_ACCESS_TRANSFER_WRITE_BIT, VK_PIPELINE_STAGE_TRANSFER_BIT, VK_PIPELINE_STAGE_TRANSFER_BIT);
            var blit = VkImageBlit.calloc(1, stack);
            blit.srcSubresource().aspectMask(VK_IMAGE_ASPECT_DEPTH_BIT).layerCount(1);
            blit.srcOffsets(1).set(source.width, source.height, 1);
            blit.dstSubresource().aspectMask(VK_IMAGE_ASPECT_DEPTH_BIT).layerCount(1);
            blit.dstOffsets(1).set(width, height, 1);
            vkCmdBlitImage(command, source.depth.handle, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                depth.handle, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, blit, VK_FILTER_NEAREST);
            VulkanImage.barrier(command, depth, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                VK_ACCESS_TRANSFER_WRITE_BIT, VK_ACCESS_TRANSFER_READ_BIT, VK_PIPELINE_STAGE_TRANSFER_BIT, VK_PIPELINE_STAGE_TRANSFER_BIT);
        }
    }

    private void copyAttachment(VkCommandBuffer command, VulkanImage source, VulkanImage target) {
        try (var stack = MemoryStack.stackPush()) {
            VulkanImage.barrier(command, target, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                VK_ACCESS_TRANSFER_READ_BIT, VK_ACCESS_TRANSFER_WRITE_BIT, VK_PIPELINE_STAGE_TRANSFER_BIT, VK_PIPELINE_STAGE_TRANSFER_BIT);
            var copy = VkImageCopy.calloc(1, stack);
            copy.srcSubresource().aspectMask(source.aspect).layerCount(1);
            copy.dstSubresource().aspectMask(target.aspect).layerCount(1);
            copy.extent().set(width, height, 1);
            vkCmdCopyImage(command, source.handle, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                target.handle, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, copy);
            VulkanImage.barrier(command, target, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                VK_ACCESS_TRANSFER_WRITE_BIT, VK_ACCESS_TRANSFER_READ_BIT, VK_PIPELINE_STAGE_TRANSFER_BIT, VK_PIPELINE_STAGE_TRANSFER_BIT);
        }
    }

    /** The owning command slot must have completed before this blocking screenshot path. */
    void readback() {
        ensureReadbackBuffers();
        context.immediate(command -> {
            try (var stack = MemoryStack.stackPush()) {
                vkCmdCopyImageToBuffer(command, color.handle, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                    colorReadback.handle(), imageCopy(stack, VK_IMAGE_ASPECT_COLOR_BIT));
                vkCmdCopyImageToBuffer(command, depth.handle, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                    depthReadback.handle(), imageCopy(stack, VK_IMAGE_ASPECT_DEPTH_BIT));
                var barrier = VkMemoryBarrier.calloc(1, stack).sType$Default()
                    .srcAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT).dstAccessMask(VK_ACCESS_HOST_READ_BIT);
                vkCmdPipelineBarrier(command, VK_PIPELINE_STAGE_TRANSFER_BIT, VK_PIPELINE_STAGE_HOST_BIT, 0, barrier, null, null);
            }
        });
    }

    private void ensureReadbackBuffers() {
        if (colorReadback != null) return;
        int bytes = Math.multiplyExact(Math.multiplyExact(width, height), 4);
        VulkanBuffer colorBuffer = context.hostBuffer(bytes, VK_BUFFER_USAGE_TRANSFER_DST_BIT);
        try {
            depthReadback = context.hostBuffer(bytes, VK_BUFFER_USAGE_TRANSFER_DST_BIT);
            colorReadback = colorBuffer;
        } catch (RuntimeException | Error error) {
            colorBuffer.close();
            throw error;
        }
    }

    private VkBufferImageCopy.Buffer imageCopy(MemoryStack stack, int aspect) {
        var copy = VkBufferImageCopy.calloc(1, stack);
        copy.imageSubresource().aspectMask(aspect).layerCount(1);
        copy.imageExtent().set(width, height, 1);
        return copy;
    }

    ByteBuffer colorPixels() { return colorReadback.mapped(); }
    ByteBuffer depthPixels() { return depthReadback.mapped(); }

    @Override public void close() {
        if (framebuffer != VK_NULL_HANDLE) vkDestroyFramebuffer(context.device(), framebuffer, null);
        if (worldFramebuffer != VK_NULL_HANDLE) vkDestroyFramebuffer(context.device(), worldFramebuffer, null);
        framebuffer = VK_NULL_HANDLE;
        worldFramebuffer = VK_NULL_HANDLE;
        if (colorReadback != null) colorReadback.close();
        if (depthReadback != null) depthReadback.close();
        if (color != null) color.close();
        if (world != null) world.close();
        if (reactive != null) reactive.close();
        if (depth != null) depth.close();
        colorReadback = depthReadback = null;
        color = depth = null;
        world = reactive = null;
        width = height = 0;
    }
}
