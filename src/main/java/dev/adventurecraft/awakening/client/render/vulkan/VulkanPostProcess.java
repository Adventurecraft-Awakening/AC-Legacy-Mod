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

/** HDR post effects and classic display-color copies, with fence-owned resources per frame slot. */
final class VulkanPostProcess implements AutoCloseable {
    private static final int BLOOM_LEVELS = 5;
    private static final int SAMPLED_IMAGES = 1 + BLOOM_LEVELS;
    private static final int RESTORE_SET = BLOOM_LEVELS + 1;
    private static final int SETS_PER_FRAME = RESTORE_SET + 1;
    private static final int PUSH_BYTES = 4 * Float.BYTES;
    private static final int WORKGROUP_SIZE = 8;
    private static final float BLOOM_STRENGTH = setting("ac.vulkan.bloomStrength", 0.06f, 0, 4);
    private static final float TONEMAP_STRENGTH = setting("ac.vulkan.tonemapStrength", 1, 0, 1);

    private static final class Frame {
        final VulkanImage[] bloom = new VulkanImage[BLOOM_LEVELS];
        final long[] sets = new long[SETS_PER_FRAME];
        int width;
        int height;

        void closeImages() {
            for (int i = 0; i < bloom.length; i++) {
                if (bloom[i] != null) bloom[i].close();
                bloom[i] = null;
            }
            width = height = 0;
        }
    }

    private final VulkanDevice context;
    private final Frame[] frames;
    private long descriptorLayout;
    private long descriptorPool;
    private long pipelineLayout;
    private long sampler;
    private long downsamplePipeline;
    private long compositePipeline;
    private long restorePipeline;
    private VulkanBuffer neutralExposure;

    VulkanPostProcess(VulkanDevice context, int frameCount) {
        this.context = Objects.requireNonNull(context, "context");
        if (frameCount <= 0) throw new IllegalArgumentException("Post-processing requires a frame slot");
        this.frames = new Frame[frameCount];
        for (int i = 0; i < frameCount; i++) frames[i] = new Frame();
        try {
            neutralExposure = context.hostBuffer(16, VK_BUFFER_USAGE_STORAGE_BUFFER_BIT);
            neutralExposure.write(bytes -> bytes.putFloat(1).putFloat(1).putFloat(0).putFloat(0));
            createBindings();
            this.downsamplePipeline = createPipeline("post_downsample.comp");
            this.compositePipeline = createPipeline("post_composite.comp");
            this.restorePipeline = createPipeline("post_restore.comp");
        } catch (RuntimeException | Error error) {
            close();
            throw error;
        }
    }

    private void createBindings() {
        try (var stack = MemoryStack.stackPush()) {
            var handle = stack.mallocLong(1);
            // Constant-indexed sampler array keeps both pipelines on the same small descriptor layout.
            var bindings = VkDescriptorSetLayoutBinding.calloc(3, stack);
            bindings.get(0).binding(0).descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
                .descriptorCount(SAMPLED_IMAGES).stageFlags(VK_SHADER_STAGE_COMPUTE_BIT);
            bindings.get(1).binding(1).descriptorType(VK_DESCRIPTOR_TYPE_STORAGE_IMAGE)
                .descriptorCount(1).stageFlags(VK_SHADER_STAGE_COMPUTE_BIT);
            bindings.get(2).binding(2).descriptorType(VK_DESCRIPTOR_TYPE_STORAGE_BUFFER)
                .descriptorCount(1).stageFlags(VK_SHADER_STAGE_COMPUTE_BIT);
            var descriptorInfo = VkDescriptorSetLayoutCreateInfo.calloc(stack).sType$Default().pBindings(bindings);
            check(vkCreateDescriptorSetLayout(context.device(), descriptorInfo, null, handle), "create post descriptor layout");
            this.descriptorLayout = handle.get(0);

            var pushRange = VkPushConstantRange.calloc(1, stack)
                .stageFlags(VK_SHADER_STAGE_COMPUTE_BIT).offset(0).size(PUSH_BYTES);
            var layoutInfo = VkPipelineLayoutCreateInfo.calloc(stack).sType$Default()
                .pSetLayouts(stack.longs(descriptorLayout)).pPushConstantRanges(pushRange);
            check(vkCreatePipelineLayout(context.device(), layoutInfo, null, handle), "create post pipeline layout");
            this.pipelineLayout = handle.get(0);

            var samplerInfo = VkSamplerCreateInfo.calloc(stack).sType$Default()
                .magFilter(VK_FILTER_LINEAR).minFilter(VK_FILTER_LINEAR)
                .mipmapMode(VK_SAMPLER_MIPMAP_MODE_NEAREST)
                .addressModeU(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE)
                .addressModeV(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE)
                .addressModeW(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE)
                .minLod(0).maxLod(0).maxAnisotropy(1);
            check(vkCreateSampler(context.device(), samplerInfo, null, handle), "create post sampler");
            this.sampler = handle.get(0);

            int setCount = Math.multiplyExact(frames.length, SETS_PER_FRAME);
            var sizes = VkDescriptorPoolSize.calloc(3, stack);
            sizes.get(0).type(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
                .descriptorCount(Math.multiplyExact(setCount, SAMPLED_IMAGES));
            sizes.get(1).type(VK_DESCRIPTOR_TYPE_STORAGE_IMAGE).descriptorCount(setCount);
            sizes.get(2).type(VK_DESCRIPTOR_TYPE_STORAGE_BUFFER).descriptorCount(setCount);
            var poolInfo = VkDescriptorPoolCreateInfo.calloc(stack).sType$Default().maxSets(setCount).pPoolSizes(sizes);
            check(vkCreateDescriptorPool(context.device(), poolInfo, null, handle), "create post descriptor pool");
            this.descriptorPool = handle.get(0);
            var layouts = stack.mallocLong(setCount);
            for (int i = 0; i < setCount; i++) layouts.put(i, descriptorLayout);
            var allocation = VkDescriptorSetAllocateInfo.calloc(stack).sType$Default()
                .descriptorPool(descriptorPool).pSetLayouts(layouts);
            var sets = stack.mallocLong(setCount);
            check(vkAllocateDescriptorSets(context.device(), allocation, sets), "allocate post descriptor sets");
            for (int i = 0; i < frames.length; i++) {
                for (int j = 0; j < SETS_PER_FRAME; j++) frames[i].sets[j] = sets.get(i * SETS_PER_FRAME + j);
            }
        }
    }

    /**
     * The caller has waited for this frame slot's fence. Both input images enter and leave in
     * TRANSFER_SRC_OPTIMAL; source is sampled RGBA16F and target is storage-capable RGBA8_UNORM.
     * Record once per slot submission, after world rendering and before any display-space overlays.
     */
    void record(VkCommandBuffer command, int frameIndex, VulkanImage source, VulkanImage target,
                float exposure, boolean hdrEffects, VulkanBuffer meteredExposure) {
        if (pipelineLayout == VK_NULL_HANDLE) throw new IllegalStateException("Post-processing is closed");
        if (frameIndex < 0 || frameIndex >= frames.length) throw new IndexOutOfBoundsException(frameIndex);
        if (source == target || source.width != target.width || source.height != target.height) {
            throw new IllegalArgumentException("Post images must be distinct and have equal dimensions");
        }
        Frame frame = frames[frameIndex];
        resize(frame, source.width, source.height);
        updateDescriptors(frame, source, target, hdrEffects,
            hdrEffects && meteredExposure != null ? meteredExposure : neutralExposure);
        exposure = Float.isFinite(exposure) ? Math.max(0, exposure) : 1;
        // Include attachment and transfer producers: the world may have been rendered, copied or cleared.
        VulkanImage.barrier(command, source, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
            VK_ACCESS_MEMORY_WRITE_BIT | VK_ACCESS_TRANSFER_READ_BIT, VK_ACCESS_SHADER_READ_BIT,
            VK_PIPELINE_STAGE_ALL_COMMANDS_BIT, VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT);
        VulkanImage.barrier(command, target, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, VK_IMAGE_LAYOUT_GENERAL,
            VK_ACCESS_TRANSFER_READ_BIT, VK_ACCESS_SHADER_WRITE_BIT,
            VK_PIPELINE_STAGE_TRANSFER_BIT, VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT);

        try (var stack = MemoryStack.stackPush()) {
            ByteBuffer push = stack.malloc(PUSH_BYTES).order(ByteOrder.nativeOrder());
            if (hdrEffects) {
                vkCmdBindPipeline(command, VK_PIPELINE_BIND_POINT_COMPUTE, downsamplePipeline);
                for (int i = 0; i < BLOOM_LEVELS; i++) {
                    VulkanImage image = frame.bloom[i];
                    // Every texel is overwritten, so discard previous contents and avoid host-side layout history.
                    VulkanImage.barrier(command, image, VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_GENERAL,
                        0, VK_ACCESS_SHADER_WRITE_BIT, VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT, VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT);
                    push.clear().putFloat(i == 0 ? 1 : 0).putFloat(exposure)
                        .putFloat(BLOOM_STRENGTH).putFloat(TONEMAP_STRENGTH).flip();
                    dispatch(command, frame.sets[i], image, push, stack);
                    VulkanImage.barrier(command, image, VK_IMAGE_LAYOUT_GENERAL, VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
                        VK_ACCESS_SHADER_WRITE_BIT, VK_ACCESS_SHADER_READ_BIT,
                        VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT, VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT);
                }
            }
            vkCmdBindPipeline(command, VK_PIPELINE_BIND_POINT_COMPUTE, compositePipeline);
            push.clear().putFloat(hdrEffects ? 1 : 0).putFloat(exposure)
                .putFloat(BLOOM_STRENGTH).putFloat(TONEMAP_STRENGTH).flip();
            dispatch(command, frame.sets[BLOOM_LEVELS], target, push, stack);
        }

        VulkanImage.barrier(command, source, VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
            VK_ACCESS_SHADER_READ_BIT, VK_ACCESS_TRANSFER_READ_BIT,
            VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT, VK_PIPELINE_STAGE_TRANSFER_BIT);
        VulkanImage.barrier(command, target, VK_IMAGE_LAYOUT_GENERAL, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
            VK_ACCESS_SHADER_WRITE_BIT, VK_ACCESS_TRANSFER_READ_BIT,
            VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT, VK_PIPELINE_STAGE_TRANSFER_BIT);
    }

    /** Restore preserved display color when a frame switches to a different world-color domain. */
    void restore(VkCommandBuffer command, int frameIndex, VulkanImage source, VulkanImage target, boolean linear) {
        Frame frame = frames[frameIndex];
        try (var stack = MemoryStack.stackPush()) {
            var sampled = VkDescriptorImageInfo.calloc(SAMPLED_IMAGES, stack);
            for (int i = 0; i < SAMPLED_IMAGES; i++) sampled.get(i).sampler(sampler).imageView(source.view)
                .imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
            var storage = VkDescriptorImageInfo.calloc(1, stack).imageView(target.view).imageLayout(VK_IMAGE_LAYOUT_GENERAL);
            var writes = VkWriteDescriptorSet.calloc(2, stack);
            writes.get(0).sType$Default().dstSet(frame.sets[RESTORE_SET]).dstBinding(0)
                .descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER).descriptorCount(SAMPLED_IMAGES).pImageInfo(sampled);
            writes.get(1).sType$Default().dstSet(frame.sets[RESTORE_SET]).dstBinding(1)
                .descriptorType(VK_DESCRIPTOR_TYPE_STORAGE_IMAGE).descriptorCount(1).pImageInfo(storage);
            vkUpdateDescriptorSets(context.device(), writes, null);
            VulkanImage.barrier(command, source, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
                VK_ACCESS_MEMORY_WRITE_BIT | VK_ACCESS_TRANSFER_READ_BIT, VK_ACCESS_SHADER_READ_BIT,
                VK_PIPELINE_STAGE_ALL_COMMANDS_BIT, VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT);
            VulkanImage.barrier(command, target, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, VK_IMAGE_LAYOUT_GENERAL,
                VK_ACCESS_MEMORY_WRITE_BIT | VK_ACCESS_TRANSFER_READ_BIT, VK_ACCESS_SHADER_WRITE_BIT,
                VK_PIPELINE_STAGE_ALL_COMMANDS_BIT, VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT);
            ByteBuffer push = stack.malloc(PUSH_BYTES).order(ByteOrder.nativeOrder());
            push.putFloat(linear ? 1 : 0).putFloat(0).putFloat(0).putFloat(0).flip();
            vkCmdBindPipeline(command, VK_PIPELINE_BIND_POINT_COMPUTE, restorePipeline);
            dispatch(command, frame.sets[RESTORE_SET], target, push, stack);
            VulkanImage.barrier(command, source, VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                VK_ACCESS_SHADER_READ_BIT, VK_ACCESS_TRANSFER_READ_BIT,
                VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT, VK_PIPELINE_STAGE_TRANSFER_BIT);
            VulkanImage.barrier(command, target, VK_IMAGE_LAYOUT_GENERAL, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                VK_ACCESS_SHADER_WRITE_BIT, VK_ACCESS_TRANSFER_READ_BIT,
                VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT, VK_PIPELINE_STAGE_TRANSFER_BIT);
        }
    }

    private void dispatch(VkCommandBuffer command, long descriptor, VulkanImage target, ByteBuffer push, MemoryStack stack) {
        vkCmdBindDescriptorSets(command, VK_PIPELINE_BIND_POINT_COMPUTE, pipelineLayout, 0, stack.longs(descriptor), null);
        vkCmdPushConstants(command, pipelineLayout, VK_SHADER_STAGE_COMPUTE_BIT, 0, push);
        vkCmdDispatch(command, (target.width + WORKGROUP_SIZE - 1) / WORKGROUP_SIZE,
            (target.height + WORKGROUP_SIZE - 1) / WORKGROUP_SIZE, 1);
    }

    private void resize(Frame frame, int width, int height) {
        if (frame.width == width && frame.height == height) return;
        frame.closeImages();
        try {
            int levelWidth = width;
            int levelHeight = height;
            for (int i = 0; i < BLOOM_LEVELS; i++) {
                levelWidth = Math.max(1, (levelWidth + 1) / 2);
                levelHeight = Math.max(1, (levelHeight + 1) / 2);
                frame.bloom[i] = VulkanImage.create(context, levelWidth, levelHeight, VK_FORMAT_R16G16B16A16_SFLOAT,
                    VK_IMAGE_USAGE_STORAGE_BIT | VK_IMAGE_USAGE_SAMPLED_BIT, VK_IMAGE_ASPECT_COLOR_BIT, 1);
            }
            frame.width = width;
            frame.height = height;
        } catch (RuntimeException | Error error) {
            frame.closeImages();
            throw error;
        }
    }

    private void updateDescriptors(Frame frame, VulkanImage source, VulkanImage target, boolean hdrEffects,
                                   VulkanBuffer exposure) {
        try (var stack = MemoryStack.stackPush()) {
            var sampled = VkDescriptorImageInfo.calloc(SAMPLED_IMAGES, stack);
            var storage = VkDescriptorImageInfo.calloc(1, stack).imageLayout(VK_IMAGE_LAYOUT_GENERAL);
            var writes = VkWriteDescriptorSet.calloc(3, stack);
            writes.get(0).sType$Default().dstBinding(0).descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
                .descriptorCount(SAMPLED_IMAGES).pImageInfo(sampled);
            writes.get(1).sType$Default().dstBinding(1).descriptorType(VK_DESCRIPTOR_TYPE_STORAGE_IMAGE)
                .descriptorCount(1).pImageInfo(storage);
            var exposureInfo = VkDescriptorBufferInfo.calloc(1, stack).buffer(exposure.handle()).offset(0).range(16);
            writes.get(2).sType$Default().dstBinding(2).descriptorType(VK_DESCRIPTOR_TYPE_STORAGE_BUFFER)
                .descriptorCount(1).pBufferInfo(exposureInfo);
            for (int pass = 0; pass <= BLOOM_LEVELS; pass++) {
                boolean composite = pass == BLOOM_LEVELS;
                for (int i = 0; i < SAMPLED_IMAGES; i++) {
                    VulkanImage input = composite ? (i == 0 || !hdrEffects ? source : frame.bloom[i - 1])
                        : (pass == 0 ? source : frame.bloom[pass - 1]);
                    sampled.get(i).sampler(sampler).imageView(input.view).imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
                }
                storage.imageView(composite ? target.view : frame.bloom[pass].view);
                writes.get(0).dstSet(frame.sets[pass]);
                writes.get(1).dstSet(frame.sets[pass]);
                writes.get(2).dstSet(frame.sets[pass]);
                vkUpdateDescriptorSets(context.device(), writes, null);
            }
        }
    }

    private long createPipeline(String name) {
        String path = "/assets/adventurecraft/shaders/vulkan/" + name + ".spv";
        try (InputStream stream = VulkanPostProcess.class.getResourceAsStream(path)) {
            if (stream == null) throw new IllegalStateException("Missing compiled Vulkan shader " + path);
            byte[] bytes = stream.readAllBytes();
            if (bytes.length == 0 || bytes.length % 4 != 0) throw new IllegalStateException("Invalid SPIR-V shader " + path);
            ByteBuffer code = MemoryUtil.memAlloc(bytes.length).order(ByteOrder.nativeOrder());
            try (var stack = MemoryStack.stackPush()) {
                code.put(bytes).flip();
                var moduleInfo = VkShaderModuleCreateInfo.calloc(stack).sType$Default().pCode(code);
                var handle = stack.mallocLong(1);
                check(vkCreateShaderModule(context.device(), moduleInfo, null, handle), "create post shader " + name);
                long module = handle.get(0);
                try {
                    var pipelineInfo = VkComputePipelineCreateInfo.calloc(1, stack).sType$Default().layout(pipelineLayout);
                    pipelineInfo.stage().sType$Default().stage(VK_SHADER_STAGE_COMPUTE_BIT).module(module).pName(stack.UTF8("main"));
                    check(vkCreateComputePipelines(context.device(), VK_NULL_HANDLE, pipelineInfo, null, handle), "create post pipeline " + name);
                    return handle.get(0);
                } finally {
                    vkDestroyShaderModule(context.device(), module, null);
                }
            } finally {
                MemoryUtil.memFree(code);
            }
        } catch (IOException error) {
            throw new IllegalStateException("Cannot read post shader " + path, error);
        }
    }

    private static float setting(String name, float fallback, float minimum, float maximum) {
        try {
            float value = Float.parseFloat(System.getProperty(name, Float.toString(fallback)));
            return Float.isFinite(value) ? Math.clamp(value, minimum, maximum) : fallback;
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    /** The renderer must finish its submissions before destroying post resources. */
    @Override public void close() {
        if (downsamplePipeline != VK_NULL_HANDLE) vkDestroyPipeline(context.device(), downsamplePipeline, null);
        if (compositePipeline != VK_NULL_HANDLE) vkDestroyPipeline(context.device(), compositePipeline, null);
        if (restorePipeline != VK_NULL_HANDLE) vkDestroyPipeline(context.device(), restorePipeline, null);
        if (pipelineLayout != VK_NULL_HANDLE) vkDestroyPipelineLayout(context.device(), pipelineLayout, null);
        if (descriptorPool != VK_NULL_HANDLE) vkDestroyDescriptorPool(context.device(), descriptorPool, null);
        if (sampler != VK_NULL_HANDLE) vkDestroySampler(context.device(), sampler, null);
        if (descriptorLayout != VK_NULL_HANDLE) vkDestroyDescriptorSetLayout(context.device(), descriptorLayout, null);
        if (neutralExposure != null) neutralExposure.close();
        neutralExposure = null;
        downsamplePipeline = compositePipeline = restorePipeline = pipelineLayout = descriptorPool = sampler = descriptorLayout = VK_NULL_HANDLE;
        for (Frame frame : frames) frame.closeImages();
    }
}
