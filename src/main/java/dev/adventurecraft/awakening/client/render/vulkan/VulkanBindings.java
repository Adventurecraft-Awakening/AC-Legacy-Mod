package dev.adventurecraft.awakening.client.render.vulkan;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import static dev.adventurecraft.awakening.client.render.vulkan.VulkanDevice.check;
import static org.lwjgl.vulkan.VK10.*;

/** Descriptor set schema, pipeline layout and pools shared by the renderer's resource owners. */
final class VulkanBindings implements AutoCloseable {
    static final int VIEW = 0;
    static final int OBJECT = 1;
    static final int MATERIAL = 2;
    static final int TEXTURE = 3;
    static final int RAY_SCENE = 4;

    private final VulkanDevice context;
    private final long[] layouts;
    private VulkanDescriptorPools pools;
    private long pipelineLayout;
    private long uniformAlignment;

    VulkanBindings(VulkanDevice context, int frameCount) {
        this.context = context;
        this.layouts = new long[context.rayTracingSupported() ? 5 : 4];
        try (var stack = MemoryStack.stackPush()) {
            var properties = VkPhysicalDeviceProperties.calloc(stack);
            vkGetPhysicalDeviceProperties(context.physicalDevice(), properties);
            uniformAlignment = Math.max(16, properties.limits().minUniformBufferOffsetAlignment());

            var binding = VkDescriptorSetLayoutBinding.calloc(1, stack).binding(0).descriptorCount(1)
                .descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC)
                .stageFlags(VK_SHADER_STAGE_VERTEX_BIT | VK_SHADER_STAGE_FRAGMENT_BIT);
            var info = VkDescriptorSetLayoutCreateInfo.calloc(stack).sType$Default().pBindings(binding);
            var handle = stack.mallocLong(1);
            for (int set = VIEW; set <= MATERIAL; set++) {
                check(vkCreateDescriptorSetLayout(context.device(), info, null, handle), "create uniform descriptor layout");
                layouts[set] = handle.get(0);
            }
            binding.descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER).stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);
            check(vkCreateDescriptorSetLayout(context.device(), info, null, handle), "create texture descriptor layout");
            layouts[TEXTURE] = handle.get(0);
            if (context.rayTracingSupported()) layouts[RAY_SCENE] = VulkanRayScene.createLayout(context);

            pools = new VulkanDescriptorPools(context, frameCount * 3);
            var pipeline = VkPipelineLayoutCreateInfo.calloc(stack).sType$Default().pSetLayouts(stack.longs(layouts));
            var constants = VkPushConstantRange.calloc(context.rayTracingSupported() ? 2 : 1, stack);
            // The Vulkan baseline guarantees 128 bytes: draw/ray data, jitter,
            // then the changed-texture cell mask and its sampler addressing.
            constants.get(0).offset(0).size(128).stageFlags(VK_SHADER_STAGE_VERTEX_BIT | VK_SHADER_STAGE_FRAGMENT_BIT);
            if (context.rayTracingSupported()) {
                constants.get(1).offset(0).size(32).stageFlags(VK_SHADER_STAGE_COMPUTE_BIT);
            }
            pipeline.pPushConstantRanges(constants);
            check(vkCreatePipelineLayout(context.device(), pipeline, null, handle), "create pipeline layout");
            pipelineLayout = handle.get(0);
        } catch (RuntimeException | Error error) {
            close();
            throw error;
        }
    }

    long pipelineLayout() { return pipelineLayout; }
    long uniformAlignment() { return uniformAlignment; }
    long rayLayout() { return layouts[RAY_SCENE]; }

    long allocateUniform(int set) {
        if (set < VIEW || set > MATERIAL) throw new IllegalArgumentException("Not a uniform descriptor set");
        return pools.uniform(layouts[set]);
    }

    long allocateTexture() { return pools.texture(layouts[TEXTURE]); }
    void recycleTexture(long descriptor) { pools.recycleTexture(descriptor); }

    @Override public void close() {
        if (pipelineLayout != VK_NULL_HANDLE) vkDestroyPipelineLayout(context.device(), pipelineLayout, null);
        pipelineLayout = VK_NULL_HANDLE;
        if (pools != null) pools.close();
        pools = null;
        for (int set = 0; set < layouts.length; set++) {
            if (layouts[set] != VK_NULL_HANDLE) vkDestroyDescriptorSetLayout(context.device(), layouts[set], null);
            layouts[set] = VK_NULL_HANDLE;
        }
    }
}
