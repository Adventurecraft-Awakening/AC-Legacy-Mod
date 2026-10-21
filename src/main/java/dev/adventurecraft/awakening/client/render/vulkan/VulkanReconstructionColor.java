package dev.adventurecraft.awakening.client.render.vulkan;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.*;

import java.io.IOException;
import java.nio.ByteOrder;

import static dev.adventurecraft.awakening.client.render.vulkan.VulkanDevice.check;
import static org.lwjgl.vulkan.VK10.*;

/** Reversible transfer-function adapter for FSR's linear input and classic display-color worlds. */
final class VulkanReconstructionColor implements AutoCloseable {
    private final VulkanDevice context;
    private final long[] descriptors;
    private long descriptorLayout, descriptorPool, pipelineLayout, pipeline, sampler;

    VulkanReconstructionColor(VulkanDevice context, int frameCount) {
        this.context = context;
        descriptors = new long[Math.multiplyExact(frameCount,2)];
        try { createBindings(); createPipeline(); }
        catch (RuntimeException | Error error) { close(); throw error; }
    }

    /** Source enters/leaves TRANSFER_SRC; a separate linear scratch image leaves GENERAL. */
    void toLinear(VkCommandBuffer command, int slot, VulkanImage source, VulkanImage target, boolean fresh) {
        if (source == target || source.width != target.width || source.height != target.height)
            throw new IllegalArgumentException("Color conversion requires separate equal-sized input and scratch images");
        VulkanImage.barrier(command, source,VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,VK_IMAGE_LAYOUT_GENERAL,
            VK_ACCESS_MEMORY_WRITE_BIT | VK_ACCESS_TRANSFER_READ_BIT,VK_ACCESS_SHADER_READ_BIT,
            VK_PIPELINE_STAGE_ALL_COMMANDS_BIT,VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT);
        VulkanImage.barrier(command, target,fresh ? VK_IMAGE_LAYOUT_UNDEFINED : VK_IMAGE_LAYOUT_GENERAL,VK_IMAGE_LAYOUT_GENERAL,
            fresh ? 0 : VK_ACCESS_SHADER_READ_BIT,VK_ACCESS_SHADER_WRITE_BIT,
            fresh ? VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT : VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT,VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT);
        record(command,slot*2,source,target,false);
        VulkanImage.barrier(command, source,VK_IMAGE_LAYOUT_GENERAL,VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
            VK_ACCESS_SHADER_READ_BIT,VK_ACCESS_TRANSFER_READ_BIT,VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT,VK_PIPELINE_STAGE_TRANSFER_BIT);
    }

    /** Each invocation reads and writes only its own pixel; native output remains GENERAL. */
    void toDisplay(VkCommandBuffer command, int slot, VulkanImage image) {
        record(command,slot*2+1,image,image,true);
    }

    private void record(VkCommandBuffer command, int descriptorIndex, VulkanImage source, VulkanImage target, boolean encode) {
        if (descriptorIndex < 0 || descriptorIndex >= descriptors.length || pipeline == VK_NULL_HANDLE
            || source.format != VK_FORMAT_R16G16B16A16_SFLOAT || target.format != VK_FORMAT_R16G16B16A16_SFLOAT)
            throw new IllegalArgumentException("Invalid floating color conversion input");
        try (var stack = MemoryStack.stackPush()) {
            long descriptor = descriptors[descriptorIndex];
            var input = VkDescriptorImageInfo.calloc(1,stack).sampler(sampler).imageView(source.view).imageLayout(VK_IMAGE_LAYOUT_GENERAL);
            var output = VkDescriptorImageInfo.calloc(1,stack).imageView(target.view).imageLayout(VK_IMAGE_LAYOUT_GENERAL);
            var writes = VkWriteDescriptorSet.calloc(2,stack);
            writes.get(0).sType$Default().dstSet(descriptor).dstBinding(0).descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER).descriptorCount(1).pImageInfo(input);
            writes.get(1).sType$Default().dstSet(descriptor).dstBinding(1).descriptorType(VK_DESCRIPTOR_TYPE_STORAGE_IMAGE).descriptorCount(1).pImageInfo(output);
            vkUpdateDescriptorSets(context.device(),writes,null);
            var barrier = VkMemoryBarrier.calloc(1,stack).sType$Default().srcAccessMask(VK_ACCESS_SHADER_WRITE_BIT)
                .dstAccessMask(VK_ACCESS_SHADER_READ_BIT | VK_ACCESS_SHADER_WRITE_BIT);
            vkCmdPipelineBarrier(command,VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT,VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT,0,barrier,null,null);
            vkCmdBindPipeline(command,VK_PIPELINE_BIND_POINT_COMPUTE,pipeline);
            vkCmdBindDescriptorSets(command,VK_PIPELINE_BIND_POINT_COMPUTE,pipelineLayout,0,stack.longs(descriptor),null);
            vkCmdPushConstants(command,pipelineLayout,VK_SHADER_STAGE_COMPUTE_BIT,0,stack.ints(encode ? 1 : 0));
            vkCmdDispatch(command,(target.width+7)/8,(target.height+7)/8,1);
            vkCmdPipelineBarrier(command,VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT,VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT,0,barrier,null,null);
        }
    }

    private void createBindings() {
        try (var stack = MemoryStack.stackPush()) {
            var handle = stack.mallocLong(1);
            var bindings = VkDescriptorSetLayoutBinding.calloc(2,stack);
            bindings.get(0).binding(0).descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER).descriptorCount(1).stageFlags(VK_SHADER_STAGE_COMPUTE_BIT);
            bindings.get(1).binding(1).descriptorType(VK_DESCRIPTOR_TYPE_STORAGE_IMAGE).descriptorCount(1).stageFlags(VK_SHADER_STAGE_COMPUTE_BIT);
            var descriptor = VkDescriptorSetLayoutCreateInfo.calloc(stack).sType$Default().pBindings(bindings);
            check(vkCreateDescriptorSetLayout(context.device(),descriptor,null,handle),"create reconstruction color descriptors");
            descriptorLayout = handle.get(0);
            var push = VkPushConstantRange.calloc(1,stack).stageFlags(VK_SHADER_STAGE_COMPUTE_BIT).size(4);
            var layout = VkPipelineLayoutCreateInfo.calloc(stack).sType$Default().pSetLayouts(stack.longs(descriptorLayout)).pPushConstantRanges(push);
            check(vkCreatePipelineLayout(context.device(),layout,null,handle),"create reconstruction color layout");
            pipelineLayout = handle.get(0);
            var samplerInfo = VkSamplerCreateInfo.calloc(stack).sType$Default().magFilter(VK_FILTER_NEAREST).minFilter(VK_FILTER_NEAREST)
                .mipmapMode(VK_SAMPLER_MIPMAP_MODE_NEAREST).addressModeU(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE)
                .addressModeV(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE).addressModeW(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE).maxAnisotropy(1);
            check(vkCreateSampler(context.device(),samplerInfo,null,handle),"create reconstruction color sampler");
            sampler = handle.get(0);
            var sizes = VkDescriptorPoolSize.calloc(2,stack);
            sizes.get(0).type(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER).descriptorCount(descriptors.length);
            sizes.get(1).type(VK_DESCRIPTOR_TYPE_STORAGE_IMAGE).descriptorCount(descriptors.length);
            var pool = VkDescriptorPoolCreateInfo.calloc(stack).sType$Default().maxSets(descriptors.length).pPoolSizes(sizes);
            check(vkCreateDescriptorPool(context.device(),pool,null,handle),"create reconstruction color pool");
            descriptorPool = handle.get(0);
            var layouts = stack.mallocLong(descriptors.length);
            for (int i=0;i<descriptors.length;i++) layouts.put(i,descriptorLayout);
            var allocation = VkDescriptorSetAllocateInfo.calloc(stack).sType$Default().descriptorPool(descriptorPool).pSetLayouts(layouts);
            var result = stack.mallocLong(descriptors.length);
            check(vkAllocateDescriptorSets(context.device(),allocation,result),"allocate reconstruction color sets");
            for (int i=0;i<descriptors.length;i++) descriptors[i]=result.get(i);
        }
    }

    private void createPipeline() {
        String path = "/assets/adventurecraft/shaders/vulkan/reconstruction_color.comp.spv";
        try (var stream = VulkanReconstructionColor.class.getResourceAsStream(path)) {
            if (stream == null) throw new IllegalStateException("Missing reconstruction color shader");
            byte[] data = stream.readAllBytes();
            if (data.length == 0 || data.length%4 != 0) throw new IllegalStateException("Invalid reconstruction color shader");
            var code = MemoryUtil.memAlloc(data.length).order(ByteOrder.nativeOrder());
            try (var stack=MemoryStack.stackPush()) {
                code.put(data).flip();
                var moduleInfo=VkShaderModuleCreateInfo.calloc(stack).sType$Default().pCode(code);
                var handle=stack.mallocLong(1);
                check(vkCreateShaderModule(context.device(),moduleInfo,null,handle),"create reconstruction color shader");
                long module=handle.get(0);
                try {
                    var info=VkComputePipelineCreateInfo.calloc(1,stack).sType$Default().layout(pipelineLayout);
                    info.stage().sType$Default().stage(VK_SHADER_STAGE_COMPUTE_BIT).module(module).pName(stack.UTF8("main"));
                    check(vkCreateComputePipelines(context.device(),VK_NULL_HANDLE,info,null,handle),"create reconstruction color pipeline");
                    pipeline=handle.get(0);
                } finally { vkDestroyShaderModule(context.device(),module,null); }
            } finally { MemoryUtil.memFree(code); }
        } catch(IOException error) { throw new IllegalStateException("Cannot load reconstruction color shader",error); }
    }

    @Override public void close() {
        if(pipeline!=VK_NULL_HANDLE)vkDestroyPipeline(context.device(),pipeline,null);
        if(descriptorPool!=VK_NULL_HANDLE)vkDestroyDescriptorPool(context.device(),descriptorPool,null);
        if(sampler!=VK_NULL_HANDLE)vkDestroySampler(context.device(),sampler,null);
        if(pipelineLayout!=VK_NULL_HANDLE)vkDestroyPipelineLayout(context.device(),pipelineLayout,null);
        if(descriptorLayout!=VK_NULL_HANDLE)vkDestroyDescriptorSetLayout(context.device(),descriptorLayout,null);
        pipeline=descriptorPool=sampler=pipelineLayout=descriptorLayout=VK_NULL_HANDLE;
    }
}
