package dev.adventurecraft.awakening.client.render.vulkan;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

import static dev.adventurecraft.awakening.client.render.vulkan.VulkanDevice.check;
import static org.lwjgl.vulkan.VK10.*;

/** World-only GPU luminance metering and time-based exposure, with fence-owned buffers per slot. */
final class VulkanExposure implements AutoCloseable {
    private static final int HISTOGRAM_BYTES = 256 * Integer.BYTES;
    private static final int EXPOSURE_BYTES = 4 * Float.BYTES;
    private static final int PUSH_BYTES = 32;
    private static final int COMPUTE = VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT;
    static final Settings DEFAULT_SETTINGS = new Settings(.09f, 1, 1.6f, 1.5f, 3);

    /** Speeds are exponential rates per second; brightening raises exposure and darkening lowers it. */
    record Settings(float key, float minExposure, float maxExposure, float brightenSpeed, float darkenSpeed) {
        Settings {
            if (!Float.isFinite(key) || key <= 0 || !Float.isFinite(minExposure) || minExposure <= 0
                || !Float.isFinite(maxExposure) || maxExposure < minExposure
                || !Float.isFinite(brightenSpeed) || brightenSpeed <= 0
                || !Float.isFinite(darkenSpeed) || darkenSpeed <= 0)
                throw new IllegalArgumentException("Exposure key, bounds and adaptation speeds must be finite and positive");
        }
    }

    record Statistics(long frames, long resets, int width, int height) { }

    private static final class Frame {
        VulkanBuffer histogram;
        VulkanBuffer exposure;
        long descriptor;
        long epoch;
        int width;
        int height;
        boolean recorded;
        boolean reusedHistory;
    }

    private final VulkanDevice context;
    private final Frame[] frames;
    private VulkanBuffer neutral;
    private Frame previous;
    private long epoch;
    private long submittedFrames;
    private long resets;
    private int width;
    private int height;
    private long descriptorLayout;
    private long descriptorPool;
    private long pipelineLayout;
    private long sampler;
    private long histogramPipeline;
    private long reducePipeline;

    static boolean supported(VulkanDevice context) {
        Objects.requireNonNull(context, "context");
        try (var stack = MemoryStack.stackPush()) {
            var properties = VkPhysicalDeviceProperties.malloc(stack);
            vkGetPhysicalDeviceProperties(context.physicalDevice(), properties);
            var limits = properties.limits();
            if (!atLeast(limits.maxPerStageDescriptorSamplers(), 1) || !atLeast(limits.maxDescriptorSetSamplers(), 1)
                || !atLeast(limits.maxPerStageDescriptorSampledImages(), 1) || !atLeast(limits.maxDescriptorSetSampledImages(), 1)
                || !atLeast(limits.maxPerStageDescriptorStorageBuffers(), 3) || !atLeast(limits.maxDescriptorSetStorageBuffers(), 3)
                || !atLeast(limits.maxPerStageResources(), 4) || !atLeast(limits.maxStorageBufferRange(), HISTOGRAM_BYTES)
                || !atLeast(limits.maxComputeSharedMemorySize(), HISTOGRAM_BYTES) || !atLeast(limits.maxPushConstantsSize(), PUSH_BYTES)
                || !atLeast(limits.maxComputeWorkGroupInvocations(), 128)
                || !atLeast(limits.maxComputeWorkGroupSize(0), 16) || !atLeast(limits.maxComputeWorkGroupSize(1), 8)) return false;
            var count = stack.mallocInt(1);
            vkGetPhysicalDeviceQueueFamilyProperties(context.physicalDevice(), count, null);
            var queues = VkQueueFamilyProperties.malloc(count.get(0), stack);
            vkGetPhysicalDeviceQueueFamilyProperties(context.physicalDevice(), count, queues);
            if ((queues.get(context.queueFamily()).queueFlags() & VK_QUEUE_COMPUTE_BIT) == 0) return false;
            var format = VkFormatProperties.malloc(stack);
            vkGetPhysicalDeviceFormatProperties(context.physicalDevice(), VK_FORMAT_R16G16B16A16_SFLOAT, format);
            return (format.optimalTilingFeatures() & VK_FORMAT_FEATURE_SAMPLED_IMAGE_BIT) != 0;
        }
    }

    private static boolean atLeast(int unsignedLimit, int value) { return Integer.compareUnsigned(unsignedLimit, value) >= 0; }

    VulkanExposure(VulkanDevice context, int frameCount) {
        this.context = Objects.requireNonNull(context, "context");
        if (frameCount < 2) throw new IllegalArgumentException("Eye adaptation requires at least two frame slots");
        if (!supported(context)) throw new IllegalStateException("GPU eye adaptation compute limits or sampled floating-point images are unsupported");
        frames = new Frame[frameCount];
        for (int i = 0; i < frames.length; i++) frames[i] = new Frame();
        try {
            for (Frame frame : frames) {
                frame.histogram = gpuBuffer(HISTOGRAM_BYTES);
                frame.exposure = gpuBuffer(EXPOSURE_BYTES);
            }
            neutral = context.hostBuffer(EXPOSURE_BYTES, VK_BUFFER_USAGE_STORAGE_BUFFER_BIT);
            neutral.write(bytes -> bytes.putFloat(1).putFloat(1).putFloat(0).putFloat(0));
            createBindings();
            histogramPipeline = createPipeline("exposure_histogram.comp");
            reducePipeline = createPipeline("exposure_reduce.comp");
        } catch (RuntimeException | Error error) {
            close();
            throw error;
        }
    }

    private VulkanBuffer gpuBuffer(int bytes) {
        return context.buffer(bytes, VK_BUFFER_USAGE_STORAGE_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
            VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
    }

    Statistics statistics() { return new Statistics(submittedFrames, resets, width, height); }

    /** Four GPU floats: adapted exposure, target exposure, metered luminance and valid-meter flag. Record before use. */
    VulkanBuffer exposureBuffer(int slot) { return frame(slot).exposure; }

    void invalidateHistory() { previous = null; epoch++; }

    void record(VkCommandBuffer command, int slot, VulkanImage source, float deltaSeconds, boolean reset) {
        record(command, slot, source, deltaSeconds, reset, DEFAULT_SETTINGS);
    }

    /**
     * Call once after this slot's fence, with linear RGBA16F world color before hands or overlays.
     * Source enters and leaves TRANSFER_SRC_OPTIMAL. Buffers are visible to later compute consumers;
     * the caller keeps the image alive through submission. No image or extent is retained across resize.
     */
    void record(VkCommandBuffer command, int slot, VulkanImage source, float deltaSeconds, boolean reset, Settings settings) {
        ensureOpen();
        Frame frame = frame(slot);
        if (frame.recorded) throw new IllegalStateException("Exposure work is already recorded for this slot");
        Objects.requireNonNull(settings, "settings");
        if (source == null || source.handle == VK_NULL_HANDLE || source.view == VK_NULL_HANDLE || source.width < 1 || source.height < 1
            || source.aspect != VK_IMAGE_ASPECT_COLOR_BIT || source.mipLevels != 1 || source.format != VK_FORMAT_R16G16B16A16_SFLOAT)
            throw new IllegalArgumentException("Eye adaptation requires an open, single-level RGBA16F world image");
        if (!Float.isFinite(deltaSeconds) || deltaSeconds < 0) throw new IllegalArgumentException("Exposure elapsed time must be finite and nonnegative");
        // Suspension must not turn the first frame after a pause into an exposure jump.
        deltaSeconds = Math.min(deltaSeconds, 1);
        boolean history = !reset && previous != null && previous != frame && previous.epoch == epoch;
        VulkanBuffer preceding = history ? previous.exposure : neutral;
        updateDescriptors(frame, source, preceding);
        bufferBarrier(command, frame.histogram, VK_ACCESS_SHADER_READ_BIT | VK_ACCESS_SHADER_WRITE_BIT,
            VK_ACCESS_TRANSFER_WRITE_BIT, COMPUTE, VK_PIPELINE_STAGE_TRANSFER_BIT);
        vkCmdFillBuffer(command, frame.histogram.handle(), 0, HISTOGRAM_BYTES, 0);
        bufferBarrier(command, frame.histogram, VK_ACCESS_TRANSFER_WRITE_BIT,
            VK_ACCESS_SHADER_READ_BIT | VK_ACCESS_SHADER_WRITE_BIT, VK_PIPELINE_STAGE_TRANSFER_BIT, COMPUTE);
        VulkanImage.barrier(command, source, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
            VK_ACCESS_MEMORY_WRITE_BIT | VK_ACCESS_TRANSFER_READ_BIT, VK_ACCESS_SHADER_READ_BIT,
            VK_PIPELINE_STAGE_ALL_COMMANDS_BIT, COMPUTE);
        if (history) bufferBarrier(command, preceding, VK_ACCESS_SHADER_WRITE_BIT | VK_ACCESS_SHADER_READ_BIT, VK_ACCESS_SHADER_READ_BIT, COMPUTE, COMPUTE);
        // Another queued frame may still read this slot's old exposure as its immutable predecessor.
        bufferBarrier(command, frame.exposure, VK_ACCESS_SHADER_READ_BIT | VK_ACCESS_SHADER_WRITE_BIT, VK_ACCESS_SHADER_WRITE_BIT, COMPUTE, COMPUTE);
        try (var stack = MemoryStack.stackPush()) {
            vkCmdBindDescriptorSets(command, VK_PIPELINE_BIND_POINT_COMPUTE, pipelineLayout, 0, stack.longs(frame.descriptor), null);
            vkCmdBindPipeline(command, VK_PIPELINE_BIND_POINT_COMPUTE, histogramPipeline);
            vkCmdDispatch(command, (source.width + 15) / 16, (source.height + 7) / 8, 1);
            bufferBarrier(command, frame.histogram, VK_ACCESS_SHADER_WRITE_BIT, VK_ACCESS_SHADER_READ_BIT, COMPUTE, COMPUTE);
            vkCmdBindPipeline(command, VK_PIPELINE_BIND_POINT_COMPUTE, reducePipeline);
            ByteBuffer push = stack.malloc(PUSH_BYTES).order(ByteOrder.nativeOrder());
            push.putFloat(deltaSeconds).putFloat(settings.key).putFloat(settings.minExposure).putFloat(settings.maxExposure)
                .putFloat(settings.brightenSpeed).putFloat(settings.darkenSpeed).putInt(history ? 1 : 0).putInt(0).flip();
            vkCmdPushConstants(command, pipelineLayout, VK_SHADER_STAGE_COMPUTE_BIT, 0, push);
            vkCmdDispatch(command, 1, 1, 1);
        }
        bufferBarrier(command, frame.exposure, VK_ACCESS_SHADER_WRITE_BIT, VK_ACCESS_SHADER_READ_BIT, COMPUTE, COMPUTE);
        VulkanImage.barrier(command, source, VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
            VK_ACCESS_SHADER_READ_BIT, VK_ACCESS_TRANSFER_READ_BIT, COMPUTE, VK_PIPELINE_STAGE_TRANSFER_BIT);
        frame.epoch = epoch;
        frame.width = source.width;
        frame.height = source.height;
        frame.reusedHistory = history;
        frame.recorded = true;
    }

    void submitted(int slot) {
        Frame frame = frame(slot);
        if (!frame.recorded) throw new IllegalStateException("No exposure work was recorded for this slot");
        frame.recorded = false;
        previous = frame.epoch == epoch ? frame : null;
        if (!frame.reusedHistory) resets++;
        width = frame.width;
        height = frame.height;
        submittedFrames++;
    }

    void abort(int slot) { frame(slot).recorded = false; invalidateHistory(); }

    private void updateDescriptors(Frame frame, VulkanImage source, VulkanBuffer preceding) {
        try (var stack = MemoryStack.stackPush()) {
            var input = VkDescriptorImageInfo.calloc(1, stack).sampler(sampler).imageView(source.view)
                .imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
            VulkanBuffer[] buffers = {frame.histogram, preceding, frame.exposure};
            var writes = VkWriteDescriptorSet.calloc(4, stack);
            writes.get(0).sType$Default().dstSet(frame.descriptor).dstBinding(0).descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
                .descriptorCount(1).pImageInfo(input);
            for (int i = 0; i < buffers.length; i++) {
                var buffer = VkDescriptorBufferInfo.calloc(1, stack).buffer(buffers[i].handle()).range(buffers[i].size());
                writes.get(i + 1).sType$Default().dstSet(frame.descriptor).dstBinding(i + 1).descriptorType(VK_DESCRIPTOR_TYPE_STORAGE_BUFFER)
                    .descriptorCount(1).pBufferInfo(buffer);
            }
            vkUpdateDescriptorSets(context.device(), writes, null);
        }
    }

    private void createBindings() {
        try (var stack = MemoryStack.stackPush()) {
            var handle = stack.mallocLong(1);
            var bindings = VkDescriptorSetLayoutBinding.calloc(4, stack);
            bindings.get(0).binding(0).descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER).descriptorCount(1).stageFlags(VK_SHADER_STAGE_COMPUTE_BIT);
            for (int i = 1; i < 4; i++) bindings.get(i).binding(i).descriptorType(VK_DESCRIPTOR_TYPE_STORAGE_BUFFER)
                .descriptorCount(1).stageFlags(VK_SHADER_STAGE_COMPUTE_BIT);
            var descriptorInfo = VkDescriptorSetLayoutCreateInfo.calloc(stack).sType$Default().pBindings(bindings);
            check(vkCreateDescriptorSetLayout(context.device(), descriptorInfo, null, handle), "create exposure descriptor layout");
            descriptorLayout = handle.get(0);
            var pushRange = VkPushConstantRange.calloc(1, stack).stageFlags(VK_SHADER_STAGE_COMPUTE_BIT).offset(0).size(PUSH_BYTES);
            var layoutInfo = VkPipelineLayoutCreateInfo.calloc(stack).sType$Default().pSetLayouts(stack.longs(descriptorLayout)).pPushConstantRanges(pushRange);
            check(vkCreatePipelineLayout(context.device(), layoutInfo, null, handle), "create exposure pipeline layout");
            pipelineLayout = handle.get(0);
            var samplerInfo = VkSamplerCreateInfo.calloc(stack).sType$Default().magFilter(VK_FILTER_NEAREST).minFilter(VK_FILTER_NEAREST)
                .mipmapMode(VK_SAMPLER_MIPMAP_MODE_NEAREST).addressModeU(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE)
                .addressModeV(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE).addressModeW(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE)
                .minLod(0).maxLod(0).maxAnisotropy(1);
            check(vkCreateSampler(context.device(), samplerInfo, null, handle), "create exposure sampler");
            sampler = handle.get(0);
            var sizes = VkDescriptorPoolSize.calloc(2, stack);
            sizes.get(0).type(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER).descriptorCount(frames.length);
            sizes.get(1).type(VK_DESCRIPTOR_TYPE_STORAGE_BUFFER).descriptorCount(frames.length * 3);
            var poolInfo = VkDescriptorPoolCreateInfo.calloc(stack).sType$Default().maxSets(frames.length).pPoolSizes(sizes);
            check(vkCreateDescriptorPool(context.device(), poolInfo, null, handle), "create exposure descriptor pool");
            descriptorPool = handle.get(0);
            var layouts = stack.mallocLong(frames.length);
            for (int i = 0; i < frames.length; i++) layouts.put(i, descriptorLayout);
            var allocation = VkDescriptorSetAllocateInfo.calloc(stack).sType$Default().descriptorPool(descriptorPool).pSetLayouts(layouts);
            var sets = stack.mallocLong(frames.length);
            check(vkAllocateDescriptorSets(context.device(), allocation, sets), "allocate exposure frame descriptors");
            for (int i = 0; i < frames.length; i++) frames[i].descriptor = sets.get(i);
        }
    }

    private long createPipeline(String shader) {
        String path = "/assets/adventurecraft/shaders/vulkan/" + shader + ".spv";
        try (InputStream stream = VulkanExposure.class.getResourceAsStream(path)) {
            if (stream == null) throw new IllegalStateException("Missing exposure shader " + path);
            byte[] bytes = stream.readAllBytes();
            if (bytes.length == 0 || bytes.length % Integer.BYTES != 0) throw new IllegalStateException("Invalid exposure shader " + path);
            ByteBuffer code = MemoryUtil.memAlloc(bytes.length).order(ByteOrder.nativeOrder());
            try (var stack = MemoryStack.stackPush()) {
                code.put(bytes).flip();
                var moduleInfo = VkShaderModuleCreateInfo.calloc(stack).sType$Default().pCode(code);
                var handle = stack.mallocLong(1);
                check(vkCreateShaderModule(context.device(), moduleInfo, null, handle), "create exposure shader");
                long module = handle.get(0);
                try {
                    var info = VkComputePipelineCreateInfo.calloc(1, stack).sType$Default().layout(pipelineLayout);
                    info.stage().sType$Default().stage(VK_SHADER_STAGE_COMPUTE_BIT).module(module).pName(stack.UTF8("main"));
                    check(vkCreateComputePipelines(context.device(), VK_NULL_HANDLE, info, null, handle), "create exposure pipeline");
                    return handle.get(0);
                } finally { vkDestroyShaderModule(context.device(), module, null); }
            } finally { MemoryUtil.memFree(code); }
        } catch (IOException error) { throw new IllegalStateException("Cannot read exposure shader " + path, error); }
    }

    private static void bufferBarrier(VkCommandBuffer command, VulkanBuffer buffer, int sourceAccess, int targetAccess, int sourceStage, int targetStage) {
        try (var stack = MemoryStack.stackPush()) {
            var barrier = VkBufferMemoryBarrier.calloc(1, stack).sType$Default().buffer(buffer.handle()).offset(0).size(VK_WHOLE_SIZE)
                .srcAccessMask(sourceAccess).dstAccessMask(targetAccess).srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED).dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED);
            vkCmdPipelineBarrier(command, sourceStage, targetStage, 0, null, barrier, null);
        }
    }

    private Frame frame(int slot) {
        if (slot < 0 || slot >= frames.length) throw new IndexOutOfBoundsException(slot);
        return frames[slot];
    }

    private void ensureOpen() { if (pipelineLayout == VK_NULL_HANDLE) throw new IllegalStateException("Eye adaptation is closed"); }

    /** The caller completes the queue before releasing exposure buffers and descriptors. */
    @Override public void close() {
        if (histogramPipeline != VK_NULL_HANDLE) vkDestroyPipeline(context.device(), histogramPipeline, null);
        if (reducePipeline != VK_NULL_HANDLE) vkDestroyPipeline(context.device(), reducePipeline, null);
        if (descriptorPool != VK_NULL_HANDLE) vkDestroyDescriptorPool(context.device(), descriptorPool, null);
        if (sampler != VK_NULL_HANDLE) vkDestroySampler(context.device(), sampler, null);
        if (pipelineLayout != VK_NULL_HANDLE) vkDestroyPipelineLayout(context.device(), pipelineLayout, null);
        if (descriptorLayout != VK_NULL_HANDLE) vkDestroyDescriptorSetLayout(context.device(), descriptorLayout, null);
        for (Frame frame : frames) {
            if (frame.histogram != null) frame.histogram.close();
            if (frame.exposure != null) frame.exposure.close();
            frame.histogram = frame.exposure = null;
            frame.recorded = false;
        }
        if (neutral != null) neutral.close();
        neutral = null;
        histogramPipeline = reducePipeline = descriptorPool = sampler = pipelineLayout = descriptorLayout = VK_NULL_HANDLE;
        previous = null;
    }
}
