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

/** Spatial cubic world reconstruction without exposure, color conversion or temporal history. */
final class VulkanUpscale implements AutoCloseable {
    private static final int WORKGROUP_SIZE = 8;
    private static final int COMPUTE = VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT;
    private final VulkanDevice context;
    private final long[] descriptors;
    private long descriptorLayout;
    private long descriptorPool;
    private long pipelineLayout;
    private long sampler;
    private long floatPipeline;
    private long unormPipeline;

    static boolean supported(VulkanDevice context) {
        Objects.requireNonNull(context, "context");
        try (var stack = MemoryStack.stackPush()) {
            var properties = VkPhysicalDeviceProperties.malloc(stack);
            vkGetPhysicalDeviceProperties(context.physicalDevice(), properties);
            var limits = properties.limits();
            if (!atLeast(limits.maxPerStageDescriptorSamplers(), 1) || !atLeast(limits.maxDescriptorSetSamplers(), 1)
                || !atLeast(limits.maxPerStageDescriptorSampledImages(), 1) || !atLeast(limits.maxDescriptorSetSampledImages(), 1)
                || !atLeast(limits.maxPerStageDescriptorStorageImages(), 1) || !atLeast(limits.maxDescriptorSetStorageImages(), 1)
                || !atLeast(limits.maxPerStageResources(), 2) || !atLeast(limits.maxComputeWorkGroupInvocations(), WORKGROUP_SIZE * WORKGROUP_SIZE)
                || !atLeast(limits.maxComputeWorkGroupSize(0), WORKGROUP_SIZE) || !atLeast(limits.maxComputeWorkGroupSize(1), WORKGROUP_SIZE)) return false;
            var count = stack.mallocInt(1);
            vkGetPhysicalDeviceQueueFamilyProperties(context.physicalDevice(), count, null);
            var queues = VkQueueFamilyProperties.malloc(count.get(0), stack);
            vkGetPhysicalDeviceQueueFamilyProperties(context.physicalDevice(), count, queues);
            if ((queues.get(context.queueFamily()).queueFlags() & VK_QUEUE_COMPUTE_BIT) == 0) return false;
            int required = VK_FORMAT_FEATURE_SAMPLED_IMAGE_BIT | VK_FORMAT_FEATURE_STORAGE_IMAGE_BIT;
            var format = VkFormatProperties.malloc(stack);
            for (int value : new int[] {VK_FORMAT_R16G16B16A16_SFLOAT, VK_FORMAT_R8G8B8A8_UNORM}) {
                vkGetPhysicalDeviceFormatProperties(context.physicalDevice(), value, format);
                if ((format.optimalTilingFeatures() & required) != required) return false;
            }
            return true;
        }
    }

    private static boolean atLeast(int unsignedLimit, int value) { return Integer.compareUnsigned(unsignedLimit, value) >= 0; }

    VulkanUpscale(VulkanDevice context, int frameCount) {
        this.context = Objects.requireNonNull(context, "context");
        if (frameCount < 1) throw new IllegalArgumentException("Spatial reconstruction requires a frame slot");
        if (!supported(context)) throw new IllegalStateException("Spatial reconstruction image formats or compute limits are unsupported");
        descriptors = new long[frameCount];
        try {
            createBindings();
            floatPipeline = createPipeline("spatial_upscale.comp");
            unormPipeline = createPipeline("spatial_upscale_rgba8.comp");
        } catch (RuntimeException | Error error) {
            close();
            throw error;
        }
    }

    /**
     * Record once after this slot's fence, before native-resolution hands or overlays. The caller owns
     * the sampled source and storage target through submission. Both enter and leave TRANSFER_SRC_OPTIMAL;
     * either may be RGBA16F or RGBA8_UNORM. There is no retained image or size state across resize.
     */
    void record(VkCommandBuffer command, int slot, VulkanImage source, VulkanImage target) {
        if (pipelineLayout == VK_NULL_HANDLE) throw new IllegalStateException("Spatial reconstruction is closed");
        if (slot < 0 || slot >= descriptors.length) throw new IndexOutOfBoundsException(slot);
        checkImage(source);
        checkImage(target);
        if (source == target || source.handle == target.handle) throw new IllegalArgumentException("Spatial reconstruction images must be distinct");
        if (target.width < source.width || target.height < source.height)
            throw new IllegalArgumentException("Spatial reconstruction target must not be smaller than its source");
        updateDescriptors(descriptors[slot], source, target);
        VulkanImage.barrier(command, source, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
            VK_ACCESS_MEMORY_WRITE_BIT | VK_ACCESS_TRANSFER_READ_BIT, VK_ACCESS_SHADER_READ_BIT,
            VK_PIPELINE_STAGE_ALL_COMMANDS_BIT, COMPUTE);
        VulkanImage.barrier(command, target, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, VK_IMAGE_LAYOUT_GENERAL,
            VK_ACCESS_MEMORY_WRITE_BIT | VK_ACCESS_TRANSFER_READ_BIT, VK_ACCESS_SHADER_WRITE_BIT,
            VK_PIPELINE_STAGE_ALL_COMMANDS_BIT, COMPUTE);
        try (var stack = MemoryStack.stackPush()) {
            vkCmdBindPipeline(command, VK_PIPELINE_BIND_POINT_COMPUTE,
                target.format == VK_FORMAT_R16G16B16A16_SFLOAT ? floatPipeline : unormPipeline);
            vkCmdBindDescriptorSets(command, VK_PIPELINE_BIND_POINT_COMPUTE, pipelineLayout, 0, stack.longs(descriptors[slot]), null);
            vkCmdDispatch(command, (target.width + WORKGROUP_SIZE - 1) / WORKGROUP_SIZE,
                (target.height + WORKGROUP_SIZE - 1) / WORKGROUP_SIZE, 1);
        }
        VulkanImage.barrier(command, source, VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
            VK_ACCESS_SHADER_READ_BIT, VK_ACCESS_TRANSFER_READ_BIT, COMPUTE, VK_PIPELINE_STAGE_TRANSFER_BIT);
        VulkanImage.barrier(command, target, VK_IMAGE_LAYOUT_GENERAL, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
            VK_ACCESS_SHADER_WRITE_BIT, VK_ACCESS_TRANSFER_READ_BIT, COMPUTE, VK_PIPELINE_STAGE_TRANSFER_BIT);
    }

    private static void checkImage(VulkanImage image) {
        Objects.requireNonNull(image, "image");
        if (image.handle == VK_NULL_HANDLE || image.view == VK_NULL_HANDLE || image.width < 1 || image.height < 1
            || image.aspect != VK_IMAGE_ASPECT_COLOR_BIT || image.mipLevels != 1
            || image.format != VK_FORMAT_R16G16B16A16_SFLOAT && image.format != VK_FORMAT_R8G8B8A8_UNORM)
            throw new IllegalArgumentException("Spatial reconstruction requires an open, single-level RGBA16F or RGBA8 color image");
    }

    private void updateDescriptors(long descriptor, VulkanImage source, VulkanImage target) {
        try (var stack = MemoryStack.stackPush()) {
            var input = VkDescriptorImageInfo.calloc(1, stack).sampler(sampler).imageView(source.view)
                .imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
            var output = VkDescriptorImageInfo.calloc(1, stack).imageView(target.view).imageLayout(VK_IMAGE_LAYOUT_GENERAL);
            var writes = VkWriteDescriptorSet.calloc(2, stack);
            writes.get(0).sType$Default().dstSet(descriptor).dstBinding(0).descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
                .descriptorCount(1).pImageInfo(input);
            writes.get(1).sType$Default().dstSet(descriptor).dstBinding(1).descriptorType(VK_DESCRIPTOR_TYPE_STORAGE_IMAGE)
                .descriptorCount(1).pImageInfo(output);
            vkUpdateDescriptorSets(context.device(), writes, null);
        }
    }

    private void createBindings() {
        try (var stack = MemoryStack.stackPush()) {
            var handle = stack.mallocLong(1);
            var bindings = VkDescriptorSetLayoutBinding.calloc(2, stack);
            bindings.get(0).binding(0).descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER).descriptorCount(1).stageFlags(VK_SHADER_STAGE_COMPUTE_BIT);
            bindings.get(1).binding(1).descriptorType(VK_DESCRIPTOR_TYPE_STORAGE_IMAGE).descriptorCount(1).stageFlags(VK_SHADER_STAGE_COMPUTE_BIT);
            var descriptorInfo = VkDescriptorSetLayoutCreateInfo.calloc(stack).sType$Default().pBindings(bindings);
            check(vkCreateDescriptorSetLayout(context.device(), descriptorInfo, null, handle), "create spatial reconstruction descriptor layout");
            descriptorLayout = handle.get(0);
            var layoutInfo = VkPipelineLayoutCreateInfo.calloc(stack).sType$Default().pSetLayouts(stack.longs(descriptorLayout));
            check(vkCreatePipelineLayout(context.device(), layoutInfo, null, handle), "create spatial reconstruction pipeline layout");
            pipelineLayout = handle.get(0);
            var samplerInfo = VkSamplerCreateInfo.calloc(stack).sType$Default().magFilter(VK_FILTER_NEAREST).minFilter(VK_FILTER_NEAREST)
                .mipmapMode(VK_SAMPLER_MIPMAP_MODE_NEAREST).addressModeU(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE)
                .addressModeV(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE).addressModeW(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE)
                .minLod(0).maxLod(0).maxAnisotropy(1);
            check(vkCreateSampler(context.device(), samplerInfo, null, handle), "create spatial reconstruction sampler");
            sampler = handle.get(0);
            var sizes = VkDescriptorPoolSize.calloc(2, stack);
            sizes.get(0).type(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER).descriptorCount(descriptors.length);
            sizes.get(1).type(VK_DESCRIPTOR_TYPE_STORAGE_IMAGE).descriptorCount(descriptors.length);
            var poolInfo = VkDescriptorPoolCreateInfo.calloc(stack).sType$Default().maxSets(descriptors.length).pPoolSizes(sizes);
            check(vkCreateDescriptorPool(context.device(), poolInfo, null, handle), "create spatial reconstruction descriptor pool");
            descriptorPool = handle.get(0);
            var layouts = stack.mallocLong(descriptors.length);
            for (int i = 0; i < descriptors.length; i++) layouts.put(i, descriptorLayout);
            var allocation = VkDescriptorSetAllocateInfo.calloc(stack).sType$Default().descriptorPool(descriptorPool).pSetLayouts(layouts);
            var sets = stack.mallocLong(descriptors.length);
            check(vkAllocateDescriptorSets(context.device(), allocation, sets), "allocate spatial reconstruction frame descriptors");
            for (int i = 0; i < descriptors.length; i++) descriptors[i] = sets.get(i);
        }
    }

    private long createPipeline(String shader) {
        String path = "/assets/adventurecraft/shaders/vulkan/" + shader + ".spv";
        try (InputStream stream = VulkanUpscale.class.getResourceAsStream(path)) {
            if (stream == null) throw new IllegalStateException("Missing spatial reconstruction shader " + path);
            byte[] bytes = stream.readAllBytes();
            if (bytes.length == 0 || bytes.length % Integer.BYTES != 0) throw new IllegalStateException("Invalid spatial reconstruction shader " + path);
            ByteBuffer code = MemoryUtil.memAlloc(bytes.length).order(ByteOrder.nativeOrder());
            try (var stack = MemoryStack.stackPush()) {
                code.put(bytes).flip();
                var moduleInfo = VkShaderModuleCreateInfo.calloc(stack).sType$Default().pCode(code);
                var handle = stack.mallocLong(1);
                check(vkCreateShaderModule(context.device(), moduleInfo, null, handle), "create spatial reconstruction shader");
                long module = handle.get(0);
                try {
                    var info = VkComputePipelineCreateInfo.calloc(1, stack).sType$Default().layout(pipelineLayout);
                    info.stage().sType$Default().stage(VK_SHADER_STAGE_COMPUTE_BIT).module(module).pName(stack.UTF8("main"));
                    check(vkCreateComputePipelines(context.device(), VK_NULL_HANDLE, info, null, handle), "create spatial reconstruction pipeline");
                    return handle.get(0);
                } finally { vkDestroyShaderModule(context.device(), module, null); }
            } finally { MemoryUtil.memFree(code); }
        } catch (IOException error) { throw new IllegalStateException("Cannot read spatial reconstruction shader " + path, error); }
    }

    /** The caller finishes its queue before destroying frame descriptors and pipelines. */
    @Override public void close() {
        if (floatPipeline != VK_NULL_HANDLE) vkDestroyPipeline(context.device(), floatPipeline, null);
        if (unormPipeline != VK_NULL_HANDLE) vkDestroyPipeline(context.device(), unormPipeline, null);
        if (descriptorPool != VK_NULL_HANDLE) vkDestroyDescriptorPool(context.device(), descriptorPool, null);
        if (sampler != VK_NULL_HANDLE) vkDestroySampler(context.device(), sampler, null);
        if (pipelineLayout != VK_NULL_HANDLE) vkDestroyPipelineLayout(context.device(), pipelineLayout, null);
        if (descriptorLayout != VK_NULL_HANDLE) vkDestroyDescriptorSetLayout(context.device(), descriptorLayout, null);
        floatPipeline = unormPipeline = descriptorPool = sampler = pipelineLayout = descriptorLayout = VK_NULL_HANDLE;
    }
}
