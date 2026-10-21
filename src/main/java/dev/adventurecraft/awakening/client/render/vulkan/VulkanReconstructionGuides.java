package dev.adventurecraft.awakening.client.render.vulkan;

import dev.adventurecraft.awakening.client.render.state.Matrix4;
import dev.adventurecraft.awakening.client.render.state.Vector4;
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

/** Application-owned native-denoiser and temporal-upscaler inputs. All exported images stay borrowed. */
final class VulkanReconstructionGuides implements AutoCloseable {
    private static final int PARAMETERS = 256;
    private static final int WORKGROUP = 8;
    private static final int COMPUTE = VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT;
    private static final int OUTPUTS = 10;
    private static final int TOTAL_IMAGES = 13;
    private static final int FLOAT4 = VK_FORMAT_R16G16B16A16_SFLOAT;

    /** Previous matrices consume current camera-relative positions, including origin rebasing. */
    record Camera(Matrix4 viewProjection, Matrix4 previousViewProjection, Matrix4 worldToView,
                  Matrix4 previousWorldToView, float jitterPixelsX, float jitterPixelsY,
                  float previousJitterPixelsX, float previousJitterPixelsY, boolean reset) { }

    private static final class Frame {
        final VulkanImage[] images = new VulkanImage[TOTAL_IMAGES];
        VulkanBuffer parameters;
        long captureDescriptor;
        long motionDescriptor;
        long applyDescriptor;
        Camera camera;
        Matrix4 worldToView;
        Matrix4 viewProjection;
        Vector4 origin;
        float jitterX, jitterY;
        long epoch;
        boolean fresh = true;
        boolean recorded;
        boolean worldMotion;
    }

    private final VulkanDevice context;
    private final Frame[] frames;
    private long descriptorLayout;
    private long applyLayout;
    private long descriptorPool;
    private long pipelineLayout;
    private long applyPipelineLayout;
    private long sampler;
    private long capturePipeline;
    private long motionPipeline;
    private long applyPipeline;
    private int width, height;
    private long epoch;
    private Frame previous;

    static boolean supported(VulkanDevice context) {
        if (!context.bufferDeviceAddressSupported()) return false;
        try (var stack = MemoryStack.stackPush()) {
            var properties = VkPhysicalDeviceProperties.malloc(stack);
            vkGetPhysicalDeviceProperties(context.physicalDevice(), properties);
            var limits = properties.limits();
            if (!atLeast(limits.maxPerStageDescriptorStorageImages(), OUTPUTS)
                || !atLeast(limits.maxDescriptorSetStorageImages(), OUTPUTS)
                || !atLeast(limits.maxPerStageDescriptorSampledImages(), 5)
                || !atLeast(limits.maxDescriptorSetSampledImages(), 5)
                || !atLeast(limits.maxPerStageDescriptorSamplers(), 5)
                || !atLeast(limits.maxDescriptorSetSamplers(), 5)
                || !atLeast(limits.maxPerStageDescriptorUniformBuffers(), 1)
                || !atLeast(limits.maxDescriptorSetUniformBuffers(), 1)
                || !atLeast(limits.maxPerStageResources(), OUTPUTS + 5)
                || !atLeast(limits.maxUniformBufferRange(), PARAMETERS)
                || !atLeast(limits.maxPushConstantsSize(), 24)
                || !atLeast(limits.maxComputeWorkGroupInvocations(), WORKGROUP * WORKGROUP)
                || !atLeast(limits.maxComputeWorkGroupSize(0), WORKGROUP)
                || !atLeast(limits.maxComputeWorkGroupSize(1), WORKGROUP)) return false;
            var format = VkFormatProperties.malloc(stack);
            for (int value : new int[]{FLOAT4, VK_FORMAT_R32_SFLOAT, VK_FORMAT_R8G8B8A8_UNORM}) {
                vkGetPhysicalDeviceFormatProperties(context.physicalDevice(), value, format);
                int required = VK_FORMAT_FEATURE_STORAGE_IMAGE_BIT | VK_FORMAT_FEATURE_SAMPLED_IMAGE_BIT;
                if ((format.optimalTilingFeatures() & required) != required) return false;
            }
            vkGetPhysicalDeviceFormatProperties(context.physicalDevice(), VK_FORMAT_D32_SFLOAT, format);
            return (format.optimalTilingFeatures() & VK_FORMAT_FEATURE_SAMPLED_IMAGE_BIT) != 0;
        }
    }

    VulkanReconstructionGuides(VulkanDevice context, int frameCount) {
        this.context = Objects.requireNonNull(context, "context");
        if (frameCount < 2) throw new IllegalArgumentException("Native reconstruction requires two frame slots");
        if (!supported(context)) throw new IllegalStateException("Native reconstruction guide formats or limits are unsupported");
        frames = new Frame[frameCount];
        for (int i = 0; i < frameCount; i++) frames[i] = new Frame();
        try {
            createBindings();
            capturePipeline = createPipeline("restir_reconstruction_guides.comp", pipelineLayout);
            motionPipeline = createPipeline("reconstruction_motion.comp", pipelineLayout);
            applyPipeline = createPipeline("restir_reconstruction_apply.comp", applyPipelineLayout);
            for (Frame frame : frames) frame.parameters = context.hostBuffer(PARAMETERS, VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT);
        } catch (RuntimeException | Error error) { close(); throw error; }
    }

    VulkanImage viewZ(int slot) { return image(slot, 0); }
    /** RG contains current-to-previous pixels without jitter; BA are zero. */
    VulkanImage motion(int slot) { return image(slot, 1); }
    /** Signed world normal XYZ and linear roughness W; NRD encoding4/roughness1. */
    VulkanImage normalRoughness(int slot) { return image(slot, 2); }
    VulkanImage diffuseAlbedo(int slot) { return image(slot, 3); }
    VulkanImage specularAlbedo(int slot) { return image(slot, 4); }
    VulkanImage diffuseRadiance(int slot) { return image(slot, 5); }
    VulkanImage specularRadiance(int slot) { return image(slot, 6); }
    VulkanImage indirectRadiance(int slot) { return image(slot, 7); }
    VulkanImage reactive(int slot) { return image(slot, 8); }
    VulkanImage specularHitDistance(int slot) { return image(slot, 9); }
    VulkanImage denoisedDiffuse(int slot) { return image(slot, 10); }
    VulkanImage denoisedSpecular(int slot) { return image(slot, 11); }
    VulkanImage denoisedIndirect(int slot) { return image(slot, 12); }
    Camera camera(int slot) { return Objects.requireNonNull(frame(slot).camera, "Record guides before requesting camera state"); }

    void invalidateHistory() { previous = null; epoch++; }

    /** A preceding submission may still read any older slot. */
    void resize(int width, int height) {
        ensureOpen();
        if (width < 1 || height < 1) throw new IllegalArgumentException("Guide extent must be positive");
        if (this.width == width && this.height == height) return;
        context.waitIdle();
        closeImages();
        invalidateHistory();
        int usage = VK_IMAGE_USAGE_STORAGE_BIT | VK_IMAGE_USAGE_SAMPLED_BIT | VK_IMAGE_USAGE_TRANSFER_SRC_BIT | VK_IMAGE_USAGE_TRANSFER_DST_BIT;
        try {
            for (Frame frame : frames) for (int i = 0; i < TOTAL_IMAGES; i++) {
                int format = i == 0 || i == 9 ? VK_FORMAT_R32_SFLOAT : i == 8 ? VK_FORMAT_R8G8B8A8_UNORM : FLOAT4;
                frame.images[i] = VulkanImage.create(context, width, height, format, usage, VK_IMAGE_ASPECT_COLOR_BIT, 1);
            }
            this.width = width;
            this.height = height;
        } catch (RuntimeException | Error error) { closeImages(); throw error; }
    }

    /**
     * The caller has waited for this slot's fence and completed opaque capture/lighting.
     * Capture views enter/leave SHADER_READ_ONLY_OPTIMAL; guides and native outputs leave GENERAL.
     * Radiance addresses refer to this submission's raw, demodulated vec4 RGB/hit-distance arrays.
     */
    void recordCapture(VkCommandBuffer command, int slot, long[] captureViews, Matrix4 jitteredViewProjection,
                       Matrix4 worldToView, Vector4 origin, float jitterNdcX, float jitterNdcY, boolean reset,
                       long directDiffuseAddress, long directSpecularAddress, long indirectDiffuseAddress) {
        ensureOpen();
        Frame frame = frame(slot);
        if (frame.images[0] == null || frame.recorded) throw new IllegalStateException("Guides need resized, unsubmitted targets");
        if (captureViews != null) {
            if (captureViews.length != 4) throw new IllegalArgumentException("Guides require four opaque capture views");
            for (long view : captureViews) if (view == VK_NULL_HANDLE) throw new IllegalArgumentException("Capture view is null");
        }
        Objects.requireNonNull(origin, "origin");
        if (!Float.isFinite(origin.x()) || !Float.isFinite(origin.y()) || !Float.isFinite(origin.z())
            || !Float.isFinite(jitterNdcX) || !Float.isFinite(jitterNdcY)) throw new IllegalArgumentException("Guide camera must be finite");
        Matrix4 inverseCurrent = VulkanTemporalAa.inverse(jitteredViewProjection);
        Matrix4 viewProjection = VulkanTemporalAa.jittered(jitteredViewProjection, -jitterNdcX, -jitterNdcY);
        Matrix4 inverseView = VulkanTemporalAa.inverse(worldToView);
        boolean history = !reset && previous != null && previous != frame && previous.epoch == epoch;
        if (history) {
            Matrix4 previousInverseView = VulkanTemporalAa.inverse(previous.worldToView);
            history = !VulkanTemporalAa.cameraJump(previous.viewProjection, previous.origin,
                new Vector4(previousInverseView.get(12),previousInverseView.get(13),previousInverseView.get(14),1),
                viewProjection,origin,new Vector4(inverseView.get(12),inverseView.get(13),inverseView.get(14),1));
        }
        Frame source = history ? previous : frame;
        Matrix4 previousViewProjection = history ? source.viewProjection : viewProjection;
        Matrix4 previousWorldToView = history ? source.worldToView : worldToView;
        Vector4 previousOrigin = history ? source.origin : origin;
        float dx = origin.x() - previousOrigin.x(), dy = origin.y() - previousOrigin.y(), dz = origin.z() - previousOrigin.z();
        boolean historyValid = history;
        frame.parameters.write(bytes -> {
            matrix(bytes, inverseCurrent);
            matrix(bytes, previousViewProjection);
            matrix(bytes, worldToView);
            vector(bytes, dx, dy, dz, 0);
            bytes.putInt(width).putInt(height).putInt(historyValid ? 1 : 0).putInt(0);
            vector(bytes, jitterNdcX, jitterNdcY, 0, 0);
            vector(bytes, inverseView.get(12), inverseView.get(13), inverseView.get(14), 0);
        });
        frame.camera = new Camera(viewProjection, previousViewProjection.translated(dx, dy, dz), worldToView,
            previousWorldToView.translated(dx, dy, dz), jitterNdcX * width * .5f, jitterNdcY * height * .5f,
            (history ? source.jitterX : jitterNdcX) * width * .5f, (history ? source.jitterY : jitterNdcY) * height * .5f, !history);
        if (captureViews != null) updateGuideDescriptor(frame.captureDescriptor, frame, captureViews, null);
        for (VulkanImage image : frame.images) VulkanImage.barrier(command, image,
            frame.fresh ? VK_IMAGE_LAYOUT_UNDEFINED : VK_IMAGE_LAYOUT_GENERAL, VK_IMAGE_LAYOUT_GENERAL,
            frame.fresh ? 0 : VK_ACCESS_MEMORY_READ_BIT | VK_ACCESS_MEMORY_WRITE_BIT, VK_ACCESS_SHADER_WRITE_BIT | VK_ACCESS_SHADER_READ_BIT,
            frame.fresh ? VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT : VK_PIPELINE_STAGE_ALL_COMMANDS_BIT, COMPUTE);
        memoryBarrier(command, VK_ACCESS_SHADER_WRITE_BIT | VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT,
            VK_ACCESS_SHADER_READ_BIT, VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT | VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT, COMPUTE);
        try (var stack = MemoryStack.stackPush()) {
            if (captureViews != null) {
                bind(command, capturePipeline, pipelineLayout, frame.captureDescriptor, stack);
                vkCmdPushConstants(command, pipelineLayout, VK_SHADER_STAGE_COMPUTE_BIT, 0,
                    stack.longs(directDiffuseAddress, directSpecularAddress, indirectDiffuseAddress));
                dispatch(command);
            } else {
                var clear = VkClearColorValue.calloc(stack);
                var range = VkImageSubresourceRange.calloc(1, stack).aspectMask(VK_IMAGE_ASPECT_COLOR_BIT).levelCount(1).layerCount(1);
                memoryBarrier(command,VK_ACCESS_MEMORY_WRITE_BIT,VK_ACCESS_TRANSFER_WRITE_BIT,
                    VK_PIPELINE_STAGE_ALL_COMMANDS_BIT,VK_PIPELINE_STAGE_TRANSFER_BIT);
                for (int i = 0; i < OUTPUTS; i++) {
                    clear.float32(0,i == 0 ? 1000000 : 0);
                    vkCmdClearColorImage(command, frame.images[i].handle, VK_IMAGE_LAYOUT_GENERAL, clear, range);
                }
                memoryBarrier(command,VK_ACCESS_TRANSFER_WRITE_BIT,VK_ACCESS_SHADER_READ_BIT | VK_ACCESS_SHADER_WRITE_BIT,
                    VK_PIPELINE_STAGE_TRANSFER_BIT,COMPUTE);
            }
        }
        nativeReadBarrier(command);
        frame.viewProjection = viewProjection;
        frame.worldToView = worldToView;
        frame.origin = origin;
        frame.jitterX = jitterNdcX;
        frame.jitterY = jitterNdcY;
        frame.epoch = epoch;
        frame.recorded = true;
        frame.worldMotion = false;
    }

    /** Refreshes depth/motion for sky, translucent surfaces and entities after the full world pass. */
    void recordWorldMotion(VkCommandBuffer command, int slot, VulkanImage deviceDepth, VulkanImage engineReactive) {
        Frame frame = frame(slot);
        if (!frame.recorded || frame.worldMotion) throw new IllegalStateException("Record capture once before full-world motion");
        checkInput(deviceDepth, VK_IMAGE_ASPECT_DEPTH_BIT);
        checkInput(engineReactive, VK_IMAGE_ASPECT_COLOR_BIT);
        if (deviceDepth.format != VK_FORMAT_D32_SFLOAT) throw new IllegalArgumentException("World depth must be D32");
        long[] views = {deviceDepth.view, engineReactive.view, engineReactive.view, engineReactive.view};
        updateGuideDescriptor(frame.motionDescriptor, frame, views, deviceDepth);
        VulkanImage.barrier(command, deviceDepth, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, VK_IMAGE_LAYOUT_DEPTH_STENCIL_READ_ONLY_OPTIMAL,
            VK_ACCESS_MEMORY_WRITE_BIT | VK_ACCESS_TRANSFER_READ_BIT, VK_ACCESS_SHADER_READ_BIT, VK_PIPELINE_STAGE_ALL_COMMANDS_BIT, COMPUTE);
        VulkanImage.barrier(command, engineReactive, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
            VK_ACCESS_MEMORY_WRITE_BIT | VK_ACCESS_TRANSFER_READ_BIT, VK_ACCESS_SHADER_READ_BIT, VK_PIPELINE_STAGE_ALL_COMMANDS_BIT, COMPUTE);
        memoryBarrier(command, VK_ACCESS_MEMORY_READ_BIT | VK_ACCESS_MEMORY_WRITE_BIT, VK_ACCESS_SHADER_WRITE_BIT,
            VK_PIPELINE_STAGE_ALL_COMMANDS_BIT, COMPUTE);
        try (var stack = MemoryStack.stackPush()) {
            bind(command, motionPipeline, pipelineLayout, frame.motionDescriptor, stack);
            dispatch(command);
        }
        nativeReadBarrier(command);
        VulkanImage.barrier(command, deviceDepth, VK_IMAGE_LAYOUT_DEPTH_STENCIL_READ_ONLY_OPTIMAL, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
            VK_ACCESS_SHADER_READ_BIT, VK_ACCESS_TRANSFER_READ_BIT, COMPUTE, VK_PIPELINE_STAGE_TRANSFER_BIT);
        VulkanImage.barrier(command, engineReactive, VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
            VK_ACCESS_SHADER_READ_BIT, VK_ACCESS_TRANSFER_READ_BIT, COMPUTE, VK_PIPELINE_STAGE_TRANSFER_BIT);
        frame.worldMotion = true;
    }

    /** Native denoising must have written the requested output images and restored them to GENERAL. */
    void applyDenoised(VkCommandBuffer command, int slot, long directResultAddress, long indirectResultAddress,
                       boolean direct, boolean indirect) {
        Frame frame = frame(slot);
        if (!frame.recorded) throw new IllegalStateException("Record guides before applying native denoising");
        if (direct && directResultAddress == 0 || indirect && indirectResultAddress == 0)
            throw new IllegalArgumentException("A requested denoised result buffer is null");
        if (!direct && !indirect) return;
        updateApplyDescriptor(frame);
        nativeReadBarrier(command);
        try (var stack = MemoryStack.stackPush()) {
            bind(command, applyPipeline, applyPipelineLayout, frame.applyDescriptor, stack);
            vkCmdPushConstants(command, applyPipelineLayout, VK_SHADER_STAGE_COMPUTE_BIT, 0,
                stack.longs(direct ? directResultAddress : 0, indirect ? indirectResultAddress : 0));
            dispatch(command);
        }
        memoryBarrier(command, VK_ACCESS_SHADER_WRITE_BIT, VK_ACCESS_SHADER_READ_BIT,
            COMPUTE, COMPUTE | VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT);
    }

    void submitted(int slot) {
        Frame frame = frame(slot);
        if (!frame.recorded) throw new IllegalStateException("No reconstruction guides were recorded");
        frame.recorded = false;
        frame.fresh = false;
        previous = frame.epoch == epoch ? frame : null;
    }

    void abort(int slot) { frame(slot).recorded = false; invalidateHistory(); }

    private void updateGuideDescriptor(long descriptor, Frame frame, long[] views, VulkanImage depth) {
        try (var stack = MemoryStack.stackPush()) {
            var inputs = VkDescriptorImageInfo.calloc(4, stack);
            for (int i = 0; i < 4; i++) inputs.get(i).sampler(sampler).imageView(views[i])
                .imageLayout(i == 0 && depth != null ? VK_IMAGE_LAYOUT_DEPTH_STENCIL_READ_ONLY_OPTIMAL : VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
            var outputs = VkDescriptorImageInfo.calloc(OUTPUTS, stack);
            for (int i = 0; i < OUTPUTS; i++) outputs.get(i).imageView(frame.images[i].view).imageLayout(VK_IMAGE_LAYOUT_GENERAL);
            var uniform = VkDescriptorBufferInfo.calloc(1, stack).buffer(frame.parameters.handle()).range(PARAMETERS);
            var writes = VkWriteDescriptorSet.calloc(OUTPUTS + 2, stack);
            writes.get(0).sType$Default().dstSet(descriptor).dstBinding(0).descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
                .descriptorCount(4).pImageInfo(inputs);
            for (int i = 0; i < OUTPUTS; i++) writes.get(i + 1).sType$Default().dstSet(descriptor).dstBinding(i < 9 ? i + 1 : 11)
                .descriptorType(VK_DESCRIPTOR_TYPE_STORAGE_IMAGE).descriptorCount(1)
                .pImageInfo(VkDescriptorImageInfo.create(outputs.get(i).address(), 1));
            writes.get(OUTPUTS + 1).sType$Default().dstSet(descriptor).dstBinding(10).descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
                .descriptorCount(1).pBufferInfo(uniform);
            vkUpdateDescriptorSets(context.device(), writes, null);
        }
    }

    private void updateApplyDescriptor(Frame frame) {
        try (var stack = MemoryStack.stackPush()) {
            int[] indices = {10, 11, 12, 3, 4};
            var images = VkDescriptorImageInfo.calloc(indices.length, stack);
            for (int i = 0; i < indices.length; i++) images.get(i).sampler(sampler).imageView(frame.images[indices[i]].view)
                .imageLayout(VK_IMAGE_LAYOUT_GENERAL);
            var write = VkWriteDescriptorSet.calloc(1, stack).sType$Default().dstSet(frame.applyDescriptor).dstBinding(0)
                .descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER).descriptorCount(indices.length).pImageInfo(images);
            vkUpdateDescriptorSets(context.device(), write, null);
        }
    }

    private void createBindings() {
        try (var stack = MemoryStack.stackPush()) {
            var handle = stack.mallocLong(1);
            var bindings = VkDescriptorSetLayoutBinding.calloc(OUTPUTS + 2, stack);
            bindings.get(0).binding(0).descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER).descriptorCount(4).stageFlags(VK_SHADER_STAGE_COMPUTE_BIT);
            for (int i = 1; i <= OUTPUTS; i++) bindings.get(i).binding(i <= 9 ? i : 11).descriptorType(VK_DESCRIPTOR_TYPE_STORAGE_IMAGE)
                .descriptorCount(1).stageFlags(VK_SHADER_STAGE_COMPUTE_BIT);
            bindings.get(OUTPUTS + 1).binding(10).descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER).descriptorCount(1).stageFlags(VK_SHADER_STAGE_COMPUTE_BIT);
            var layout = VkDescriptorSetLayoutCreateInfo.calloc(stack).sType$Default().pBindings(bindings);
            check(vkCreateDescriptorSetLayout(context.device(), layout, null, handle), "create native guide descriptor layout");
            descriptorLayout = handle.get(0);
            var applyBindings = VkDescriptorSetLayoutBinding.calloc(1, stack).binding(0)
                .descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER).descriptorCount(5).stageFlags(VK_SHADER_STAGE_COMPUTE_BIT);
            layout.pBindings(applyBindings);
            check(vkCreateDescriptorSetLayout(context.device(), layout, null, handle), "create native lighting apply descriptor layout");
            applyLayout = handle.get(0);
            var ranges = VkPushConstantRange.calloc(1, stack).stageFlags(VK_SHADER_STAGE_COMPUTE_BIT).size(24);
            var pipeline = VkPipelineLayoutCreateInfo.calloc(stack).sType$Default().pSetLayouts(stack.longs(descriptorLayout)).pPushConstantRanges(ranges);
            check(vkCreatePipelineLayout(context.device(), pipeline, null, handle), "create native guide pipeline layout");
            pipelineLayout = handle.get(0);
            ranges.size(16);
            pipeline.pSetLayouts(stack.longs(applyLayout));
            check(vkCreatePipelineLayout(context.device(), pipeline, null, handle), "create native lighting apply pipeline layout");
            applyPipelineLayout = handle.get(0);
            var samplerInfo = VkSamplerCreateInfo.calloc(stack).sType$Default().minFilter(VK_FILTER_NEAREST).magFilter(VK_FILTER_NEAREST)
                .mipmapMode(VK_SAMPLER_MIPMAP_MODE_NEAREST).addressModeU(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE)
                .addressModeV(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE).addressModeW(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE).maxAnisotropy(1);
            check(vkCreateSampler(context.device(), samplerInfo, null, handle), "create native guide sampler");
            sampler = handle.get(0);
            var sizes = VkDescriptorPoolSize.calloc(3, stack);
            sizes.get(0).type(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER).descriptorCount(frames.length * 13);
            sizes.get(1).type(VK_DESCRIPTOR_TYPE_STORAGE_IMAGE).descriptorCount(frames.length * OUTPUTS * 2);
            sizes.get(2).type(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER).descriptorCount(frames.length * 2);
            var pool = VkDescriptorPoolCreateInfo.calloc(stack).sType$Default().maxSets(frames.length * 3).pPoolSizes(sizes);
            check(vkCreateDescriptorPool(context.device(), pool, null, handle), "create native guide descriptor pool");
            descriptorPool = handle.get(0);
            var layouts = stack.mallocLong(frames.length * 3);
            for (int i = 0; i < frames.length; i++) layouts.put(i * 3, descriptorLayout).put(i * 3 + 1, descriptorLayout).put(i * 3 + 2, applyLayout);
            var allocation = VkDescriptorSetAllocateInfo.calloc(stack).sType$Default().descriptorPool(descriptorPool).pSetLayouts(layouts);
            var sets = stack.mallocLong(layouts.remaining());
            check(vkAllocateDescriptorSets(context.device(), allocation, sets), "allocate native guide frame descriptors");
            for (int i = 0; i < frames.length; i++) {
                frames[i].captureDescriptor = sets.get(i * 3);
                frames[i].motionDescriptor = sets.get(i * 3 + 1);
                frames[i].applyDescriptor = sets.get(i * 3 + 2);
            }
        }
    }

    private long createPipeline(String name, long layout) {
        String path = "/assets/adventurecraft/shaders/vulkan/" + name + ".spv";
        try (InputStream stream = VulkanReconstructionGuides.class.getResourceAsStream(path)) {
            if (stream == null) throw new IllegalStateException("Missing reconstruction guide shader " + path);
            byte[] bytes = stream.readAllBytes();
            if (bytes.length == 0 || bytes.length % 4 != 0) throw new IllegalStateException("Invalid reconstruction guide shader " + path);
            ByteBuffer code = MemoryUtil.memAlloc(bytes.length).order(ByteOrder.nativeOrder());
            try (var stack = MemoryStack.stackPush()) {
                code.put(bytes).flip();
                var moduleInfo = VkShaderModuleCreateInfo.calloc(stack).sType$Default().pCode(code);
                var handle = stack.mallocLong(1);
                check(vkCreateShaderModule(context.device(), moduleInfo, null, handle), "create reconstruction guide shader");
                long module = handle.get(0);
                try {
                    var info = VkComputePipelineCreateInfo.calloc(1, stack).sType$Default().layout(layout);
                    info.stage().sType$Default().stage(VK_SHADER_STAGE_COMPUTE_BIT).module(module).pName(stack.UTF8("main"));
                    check(vkCreateComputePipelines(context.device(), VK_NULL_HANDLE, info, null, handle), "create reconstruction guide pipeline");
                    return handle.get(0);
                } finally { vkDestroyShaderModule(context.device(), module, null); }
            } finally { MemoryUtil.memFree(code); }
        } catch (IOException error) { throw new IllegalStateException("Cannot load reconstruction guide shader " + path, error); }
    }

    private void dispatch(VkCommandBuffer command) { vkCmdDispatch(command, (width + WORKGROUP - 1) / WORKGROUP, (height + WORKGROUP - 1) / WORKGROUP, 1); }
    private static void bind(VkCommandBuffer command, long pipeline, long layout, long descriptor, MemoryStack stack) {
        vkCmdBindPipeline(command, VK_PIPELINE_BIND_POINT_COMPUTE, pipeline);
        vkCmdBindDescriptorSets(command, VK_PIPELINE_BIND_POINT_COMPUTE, layout, 0, stack.longs(descriptor), null);
    }
    private static void nativeReadBarrier(VkCommandBuffer command) {
        memoryBarrier(command, VK_ACCESS_SHADER_WRITE_BIT, VK_ACCESS_SHADER_READ_BIT | VK_ACCESS_SHADER_WRITE_BIT, COMPUTE, COMPUTE);
    }
    private static void memoryBarrier(VkCommandBuffer command, int sourceAccess, int targetAccess, int sourceStage, int targetStage) {
        try (var stack = MemoryStack.stackPush()) {
            var memory = VkMemoryBarrier.calloc(1, stack).sType$Default().srcAccessMask(sourceAccess).dstAccessMask(targetAccess);
            vkCmdPipelineBarrier(command, sourceStage, targetStage, 0, memory, null, null);
        }
    }
    private void checkInput(VulkanImage image, int aspect) {
        if (image == null || image.handle == VK_NULL_HANDLE || image.width != width || image.height != height || image.aspect != aspect)
            throw new IllegalArgumentException("Reconstruction input extent/aspect is incompatible");
    }
    private static boolean atLeast(int value, int minimum) { return Integer.compareUnsigned(value, minimum) >= 0; }
    private static void matrix(ByteBuffer target, Matrix4 matrix) { for (float value : matrix.copyArray()) target.putFloat(value); }
    private static void vector(ByteBuffer target, float x, float y, float z, float w) { target.putFloat(x).putFloat(y).putFloat(z).putFloat(w); }
    private Frame frame(int slot) { if (slot < 0 || slot >= frames.length) throw new IndexOutOfBoundsException(slot); return frames[slot]; }
    private VulkanImage image(int slot, int index) { return Objects.requireNonNull(frame(slot).images[index], "Resize reconstruction guides first"); }
    private void ensureOpen() { if (pipelineLayout == VK_NULL_HANDLE) throw new IllegalStateException("Reconstruction guides are closed"); }
    private void closeImages() {
        for (Frame frame : frames) {
            for (int i = 0; i < frame.images.length; i++) { if (frame.images[i] != null) frame.images[i].close(); frame.images[i] = null; }
            frame.fresh = true;
            frame.recorded = frame.worldMotion = false;
            frame.camera = null;
        }
        width = height = 0;
    }
    /** The renderer finishes its queue before disposing native contexts or these borrowed images. */
    @Override public void close() {
        closeImages();
        for (Frame frame : frames) { if (frame.parameters != null) frame.parameters.close(); frame.parameters = null; }
        for (long pipeline : new long[]{capturePipeline, motionPipeline, applyPipeline}) if (pipeline != VK_NULL_HANDLE) vkDestroyPipeline(context.device(), pipeline, null);
        if (descriptorPool != VK_NULL_HANDLE) vkDestroyDescriptorPool(context.device(), descriptorPool, null);
        if (sampler != VK_NULL_HANDLE) vkDestroySampler(context.device(), sampler, null);
        for (long layout : new long[]{pipelineLayout, applyPipelineLayout}) if (layout != VK_NULL_HANDLE) vkDestroyPipelineLayout(context.device(), layout, null);
        for (long layout : new long[]{descriptorLayout, applyLayout}) if (layout != VK_NULL_HANDLE) vkDestroyDescriptorSetLayout(context.device(), layout, null);
        capturePipeline = motionPipeline = applyPipeline = descriptorPool = sampler = pipelineLayout = applyPipelineLayout = descriptorLayout = applyLayout = VK_NULL_HANDLE;
        previous = null;
    }
}
