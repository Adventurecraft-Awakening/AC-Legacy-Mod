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
import static org.lwjgl.vulkan.VK12.VK_BUFFER_USAGE_SHADER_DEVICE_ADDRESS_BIT;

/** Owns terrain capture and fence-slot ReSTIR reservoirs, visibility results and denoised history. */
final class VulkanRestirDi implements AutoCloseable {
    static final int POSITION_FORMAT = VK_FORMAT_R32G32B32A32_SFLOAT;
    static final int NORMAL_FORMAT = VK_FORMAT_R16G16B16A16_SFLOAT;
    static final int MATERIAL_FORMAT = VK_FORMAT_R8G8B8A8_UNORM;
    static final int DEPTH_FORMAT = VK_FORMAT_D32_SFLOAT;
    static final int COLOR_ATTACHMENTS = 4;
    static final int RESERVOIR_BYTES = 48;
    static final int RESULT_BYTES = 16;
    private static final int PARAM_BYTES = 128;
    private static final int PUSH_BYTES = 64;
    private static final int WORKGROUP_SIZE = 8;
    private static final int SAMPLED_IMAGES = COLOR_ATTACHMENTS * 2;
    private static final int TOTAL_SAMPLED_IMAGES = SAMPLED_IMAGES + VulkanRayScene.TEXTURE_COUNT;
    private static final int CANDIDATES = setting("ac.vulkan.restirCandidates", 16, 1, 32);
    private static final int TEMPORAL_SAMPLES = setting("ac.vulkan.restirTemporalSamples", 32, 1, 64);
    private static final int SPATIAL_NEIGHBORS = setting("ac.vulkan.restirSpatialNeighbors", 4, 0, 8);
    private static final int SPATIAL_RADIUS = setting("ac.vulkan.restirSpatialRadius", 16, 1, 64);
    private static final int DEBUG_MODE = setting("ac.vulkan.restirDebug", 0, 0, 3);

    record Statistics(long frames, long resets, int width, int height, int historyFrames) { }

    private static final class Frame {
        final VulkanImage[] color = new VulkanImage[COLOR_ATTACHMENTS];
        VulkanImage depth;
        VulkanBuffer reservoir;
        VulkanBuffer result;
        VulkanBuffer parameters;
        long descriptor;
        long framebuffer;
        Matrix4 viewProjection;
        Vector4 worldOrigin;
        long materialRevision;
        long emitterFingerprint;
        long epoch;
        int historyFrames;
        boolean fresh = true;
        boolean captured;
        boolean recorded;
    }

    private final VulkanDevice context;
    private final Frame[] frames;
    private long renderPass;
    private long descriptorLayout;
    private long emptyLayout;
    private long descriptorPool;
    private long pipelineLayout;
    private long sampler;
    private long initialPipeline;
    private long spatialPipeline;
    private long denoisePipeline;
    private VulkanBuffer initialScratch;
    private VulkanBuffer rawScratch;
    private boolean scratchFresh = true;
    private int width;
    private int height;
    private int capturing = -1;
    private Frame previous;
    private long epoch;
    private long submittedFrames;
    private long historyResets;
    private boolean taaActive;
    private boolean reconstructionExport;
    private boolean allocatedReconstructionExport;

    static boolean supported(VulkanDevice context) {
        if (!context.rayTracingSupported() || !context.bufferDeviceAddressSupported()) return false;
        try (var stack = MemoryStack.stackPush()) {
            var properties = VkPhysicalDeviceProperties.malloc(stack);
            vkGetPhysicalDeviceProperties(context.physicalDevice(), properties);
            var limits = properties.limits();
            if (!atLeast(limits.maxColorAttachments(), COLOR_ATTACHMENTS)
                || !atLeast(limits.maxPerStageDescriptorSamplers(), TOTAL_SAMPLED_IMAGES)
                || !atLeast(limits.maxDescriptorSetSamplers(), TOTAL_SAMPLED_IMAGES)
                || !atLeast(limits.maxPerStageDescriptorSampledImages(), TOTAL_SAMPLED_IMAGES)
                || !atLeast(limits.maxDescriptorSetSampledImages(), TOTAL_SAMPLED_IMAGES)
                || !atLeast(limits.maxPerStageResources(), TOTAL_SAMPLED_IMAGES + 4)
                || !atLeast(limits.maxPushConstantsSize(), PUSH_BYTES)
                || !atLeast(limits.maxComputeWorkGroupInvocations(), WORKGROUP_SIZE * WORKGROUP_SIZE)
                || !atLeast(limits.maxComputeWorkGroupSize(0), WORKGROUP_SIZE)
                || !atLeast(limits.maxComputeWorkGroupSize(1), WORKGROUP_SIZE)) return false;
            var count = stack.mallocInt(1);
            vkGetPhysicalDeviceQueueFamilyProperties(context.physicalDevice(), count, null);
            var queues = VkQueueFamilyProperties.malloc(count.get(0), stack);
            vkGetPhysicalDeviceQueueFamilyProperties(context.physicalDevice(), count, queues);
            if ((queues.get(context.queueFamily()).queueFlags() & VK_QUEUE_COMPUTE_BIT) == 0) return false;
            int colorFeatures = VK_FORMAT_FEATURE_COLOR_ATTACHMENT_BIT | VK_FORMAT_FEATURE_SAMPLED_IMAGE_BIT;
            var format = VkFormatProperties.malloc(stack);
            for (int value : new int[]{POSITION_FORMAT, NORMAL_FORMAT, MATERIAL_FORMAT}) {
                vkGetPhysicalDeviceFormatProperties(context.physicalDevice(), value, format);
                if ((format.optimalTilingFeatures() & colorFeatures) != colorFeatures) return false;
            }
            vkGetPhysicalDeviceFormatProperties(context.physicalDevice(), DEPTH_FORMAT, format);
            return (format.optimalTilingFeatures() & VK_FORMAT_FEATURE_DEPTH_STENCIL_ATTACHMENT_BIT) != 0;
        }
    }

    VulkanRestirDi(VulkanDevice context, int frameCount, long raySceneLayout) {
        this.context = Objects.requireNonNull(context, "context");
        if (frameCount < 2) throw new IllegalArgumentException("ReSTIR history requires at least two frame slots");
        if (!supported(context)) throw new IllegalStateException("Vulkan ReSTIR capture and compute are unsupported");
        if (raySceneLayout == VK_NULL_HANDLE) throw new IllegalArgumentException("ReSTIR requires a ray scene descriptor layout");
        this.frames = new Frame[frameCount];
        for (int i = 0; i < frameCount; i++) frames[i] = new Frame();
        try {
            createRenderPass();
            createBindings(raySceneLayout);
            initialPipeline = createPipeline("restir_initial.comp");
            spatialPipeline = createPipeline("restir_spatial.comp");
            denoisePipeline = createPipeline("restir_denoise.comp");
        } catch (RuntimeException | Error error) {
            close();
            throw error;
        }
    }

    long renderPass() { return renderPass; }

    long framebuffer(int frameIndex) { return frame(frameIndex).framebuffer; }

    long[] colorViews(int frameIndex) {
        Frame frame = frame(frameIndex);
        long[] views = new long[COLOR_ATTACHMENTS];
        for (int i = 0; i < views.length; i++) views[i] = frame.color[i].view;
        return views;
    }

    long resultAddress(int frameIndex) {
        Frame frame = frame(frameIndex);
        if (frame.result == null) throw new IllegalStateException("Resize ReSTIR targets before requesting their address");
        return frame.result.deviceAddress();
    }

    Statistics statistics() {
        return new Statistics(submittedFrames, historyResets, width, height, previous == null ? 0 : previous.historyFrames);
    }

    /** Reprojection history is meaningful only across consecutive compatible world frames. */
    void invalidateHistory() {
        previous = null;
        epoch++;
        historyResets++;
    }

    /** Record final-color TAA state independently of the lighting denoiser's history. */
    void setTaaActive(boolean active) { taaActive = active; }

    /** Extra unfiltered, material-demodulated signals are allocated only for native denoising. */
    void setReconstructionExport(boolean active) { reconstructionExport = active; }

    long rawDiffuseAddress() { return rawLobeAddress(1); }
    long rawSpecularAddress() { return rawLobeAddress(2); }
    long rawResultAddress() { return rawScratch == null ? 0 : rawScratch.deviceAddress(); }

    private long rawLobeAddress(int lobe) {
        if (!allocatedReconstructionExport || rawScratch == null)
            throw new IllegalStateException("Resize ReSTIR with reconstruction export enabled first");
        return rawScratch.deviceAddress() + Math.multiplyExact(Math.multiplyExact((long) width, height), RESULT_BYTES * lobe);
    }

    /** Extent changes wait for all readers, including a newer slot still sampling an older slot's history. */
    void resize(int width, int height) {
        ensureOpen();
        if (width < 1 || height < 1) throw new IllegalArgumentException("ReSTIR dimensions must be positive");
        if (this.width == width && this.height == height && allocatedReconstructionExport == reconstructionExport) return;
        if (capturing >= 0) throw new IllegalStateException("Cannot resize an open ReSTIR capture");
        context.waitIdle();
        for (Frame frame : frames) closeTargets(frame);
        closeScratch();
        this.width = this.height = 0;
        invalidateHistory();
        try {
            long pixels = Math.multiplyExact((long) width, height);
            for (Frame frame : frames) createTargets(frame, width, height, pixels);
            int usage = VK_BUFFER_USAGE_STORAGE_BUFFER_BIT | VK_BUFFER_USAGE_SHADER_DEVICE_ADDRESS_BIT
                | VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_TRANSFER_SRC_BIT;
            initialScratch = context.buffer(Math.multiplyExact(pixels, RESERVOIR_BYTES), usage, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
            rawScratch = context.buffer(Math.multiplyExact(pixels, RESULT_BYTES * (reconstructionExport ? 3 : 1)), usage, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
            allocatedReconstructionExport = reconstructionExport;
            this.width = width;
            this.height = height;
        } catch (RuntimeException | Error error) {
            for (Frame frame : frames) closeTargets(frame);
            closeScratch();
            throw error;
        }
    }

    private void createTargets(Frame frame, int width, int height, long pixels) {
        for (int i = 0; i < COLOR_ATTACHMENTS; i++) {
            int format = i == 0 ? POSITION_FORMAT : i == 1 ? NORMAL_FORMAT : MATERIAL_FORMAT;
            frame.color[i] = VulkanImage.create(context, width, height, format,
                VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT | VK_IMAGE_USAGE_SAMPLED_BIT, VK_IMAGE_ASPECT_COLOR_BIT, 1);
        }
        frame.depth = VulkanImage.create(context, width, height, DEPTH_FORMAT,
            VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT, VK_IMAGE_ASPECT_DEPTH_BIT, 1);
        int bufferUsage = VK_BUFFER_USAGE_STORAGE_BUFFER_BIT | VK_BUFFER_USAGE_SHADER_DEVICE_ADDRESS_BIT
            | VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_TRANSFER_SRC_BIT;
        frame.reservoir = context.buffer(Math.multiplyExact(pixels, RESERVOIR_BYTES), bufferUsage, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
        frame.result = context.buffer(Math.multiplyExact(pixels, RESULT_BYTES), bufferUsage, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
        frame.parameters = context.hostBuffer(PARAM_BYTES, VK_BUFFER_USAGE_STORAGE_BUFFER_BIT | VK_BUFFER_USAGE_SHADER_DEVICE_ADDRESS_BIT);
        try (var stack = MemoryStack.stackPush()) {
            var info = VkFramebufferCreateInfo.calloc(stack).sType$Default().renderPass(renderPass)
                .pAttachments(stack.longs(frame.color[0].view, frame.color[1].view, frame.color[2].view, frame.color[3].view, frame.depth.view))
                .width(width).height(height).layers(1);
            var handle = stack.mallocLong(1);
            check(vkCreateFramebuffer(context.device(), info, null, handle), "create ReSTIR capture framebuffer");
            frame.framebuffer = handle.get(0);
        }
    }

    /** The caller has waited for this slot's submission fence and records compatible terrain MRT pipelines. */
    void beginCapture(VkCommandBuffer command, int frameIndex) {
        ensureOpen();
        Frame frame = frame(frameIndex);
        if (frame.framebuffer == VK_NULL_HANDLE) throw new IllegalStateException("Resize ReSTIR targets before capture");
        if (capturing >= 0 || frame.recorded) throw new IllegalStateException("Previous ReSTIR recording was not completed");
        try (var stack = MemoryStack.stackPush()) {
            var clears = VkClearValue.calloc(COLOR_ATTACHMENTS + 1, stack);
            clears.get(COLOR_ATTACHMENTS).depthStencil().set(1, 0);
            var begin = VkRenderPassBeginInfo.calloc(stack).sType$Default()
                .renderPass(renderPass).framebuffer(frame.framebuffer).pClearValues(clears);
            begin.renderArea().extent().set(width, height);
            vkCmdBeginRenderPass(command, begin, VK_SUBPASS_CONTENTS_INLINE);
        }
        frame.captured = false;
        capturing = frameIndex;
    }

    void endCapture(VkCommandBuffer command) {
        if (capturing < 0) throw new IllegalStateException("No ReSTIR capture is open");
        vkCmdEndRenderPass(command);
        frames[capturing].captured = true;
        capturing = -1;
    }

    void recordCompute(VkCommandBuffer command, int frameIndex, long raySceneSet, Matrix4 currentViewProjection,
                       Vector4 worldCameraOrigin, Vector4 eyeOrigin, long materialRevision, long emitterFingerprint,
                       long emitterAddress, boolean cameraCut) {
        ensureOpen();
        Frame frame = frame(frameIndex);
        if (!frame.captured || frame.recorded || capturing >= 0) throw new IllegalStateException("ReSTIR needs a completed fresh capture");
        Objects.requireNonNull(currentViewProjection, "currentViewProjection");
        Objects.requireNonNull(worldCameraOrigin, "worldCameraOrigin");
        Objects.requireNonNull(eyeOrigin, "eyeOrigin");
        if (raySceneSet == VK_NULL_HANDLE || emitterAddress == 0) throw new IllegalArgumentException("ReSTIR scene and emitter bindings are required");
        boolean history = !cameraCut && previous != null && previous != frame && previous.epoch == epoch
            && previous.materialRevision == materialRevision
            && !cameraJump(previous, currentViewProjection, worldCameraOrigin);
        // A camera-culled source list can change while the receiver and its lighting stay stable.
        // Keep geometrically validated color history, but restart proposals when their domain changes.
        boolean reservoirHistory = history && previous.emitterFingerprint == emitterFingerprint;
        if (!history && previous != null) {
            invalidateHistory();
        }
        Frame source = history ? previous : frame;
        updateDescriptors(frame, source);
        writeParameters(frame, source, currentViewProjection, worldCameraOrigin, eyeOrigin, history, reservoirHistory);
        try (var stack = MemoryStack.stackPush()) {
            // A reused slot may still have been sampled by a previous submission on this same queue.
            barrier(command, VK_ACCESS_SHADER_READ_BIT | VK_ACCESS_SHADER_WRITE_BIT,
                VK_ACCESS_SHADER_READ_BIT | VK_ACCESS_SHADER_WRITE_BIT,
                VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT | VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT, VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT);
            if (frame.fresh || scratchFresh) {
                if (frame.fresh) {
                    vkCmdFillBuffer(command, frame.reservoir.handle(), 0, VK_WHOLE_SIZE, 0);
                    vkCmdFillBuffer(command, frame.result.handle(), 0, VK_WHOLE_SIZE, 0);
                }
                if (scratchFresh) {
                    vkCmdFillBuffer(command, initialScratch.handle(), 0, VK_WHOLE_SIZE, 0);
                    vkCmdFillBuffer(command, rawScratch.handle(), 0, VK_WHOLE_SIZE, 0);
                }
                barrier(command, VK_ACCESS_TRANSFER_WRITE_BIT, VK_ACCESS_SHADER_READ_BIT | VK_ACCESS_SHADER_WRITE_BIT,
                    VK_PIPELINE_STAGE_TRANSFER_BIT, VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT);
            }
            vkCmdBindDescriptorSets(command, VK_PIPELINE_BIND_POINT_COMPUTE, pipelineLayout, 0, stack.longs(frame.descriptor), null);
            vkCmdBindDescriptorSets(command, VK_PIPELINE_BIND_POINT_COMPUTE, pipelineLayout, VulkanBindings.RAY_SCENE,
                stack.longs(raySceneSet), null);
            ByteBuffer push = stack.malloc(PUSH_BYTES).order(ByteOrder.nativeOrder());
            push.putLong(emitterAddress).putLong(initialScratch.deviceAddress()).putLong(source.reservoir.deviceAddress())
                .putLong(frame.reservoir.deviceAddress()).putLong(rawScratch.deviceAddress()).putLong(frame.result.deviceAddress())
                .putLong(source.result.deviceAddress()).putLong(frame.parameters.deviceAddress()).flip();
            vkCmdPushConstants(command, pipelineLayout, VK_SHADER_STAGE_COMPUTE_BIT, 0, push);
            dispatch(command, initialPipeline);
            computeBarrier(command);
            dispatch(command, spatialPipeline);
            computeBarrier(command);
            dispatch(command, denoisePipeline);
            barrier(command, VK_ACCESS_SHADER_WRITE_BIT, VK_ACCESS_SHADER_READ_BIT | VK_ACCESS_TRANSFER_READ_BIT,
                VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT,
                VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT | VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT | VK_PIPELINE_STAGE_TRANSFER_BIT);
        }
        frame.viewProjection = currentViewProjection;
        frame.worldOrigin = worldCameraOrigin;
        frame.materialRevision = materialRevision;
        frame.emitterFingerprint = emitterFingerprint;
        frame.epoch = epoch;
        frame.historyFrames = history ? Math.min(source.historyFrames + 1, 65535) : 1;
        frame.recorded = true;
    }

    /** Commit only after the renderer successfully queues the recorded command buffer. */
    void submitted(int frameIndex) {
        Frame frame = frame(frameIndex);
        if (!frame.recorded) throw new IllegalStateException("No recorded ReSTIR work to submit");
        frame.fresh = false;
        scratchFresh = false;
        frame.recorded = frame.captured = false;
        previous = frame.epoch == epoch ? frame : null;
        submittedFrames++;
    }

    /** Surface capture can also feed SHARC when no local direct-light pass is needed. */
    void submittedCapture(int frameIndex) {
        Frame frame = frame(frameIndex);
        if (!frame.captured || frame.recorded) throw new IllegalStateException("No capture-only frame to submit");
        frame.captured = false;
        invalidateHistory();
    }

    /** Abandoned command buffers did not create a usable history frame. */
    void abort(int frameIndex) {
        Frame frame = frame(frameIndex);
        frame.recorded = frame.captured = false;
        if (capturing == frameIndex) capturing = -1;
        invalidateHistory();
    }

    private void writeParameters(Frame frame, Frame source, Matrix4 currentViewProjection,
                                 Vector4 worldOrigin, Vector4 eyeOrigin, boolean history, boolean reservoirHistory) {
        frame.parameters.write(bytes -> {
            Matrix4 previousView = history ? source.viewProjection : currentViewProjection;
            for (float value : previousView.copyArray()) bytes.putFloat(value);
            Vector4 previousOrigin = history ? source.worldOrigin : worldOrigin;
            bytes.putFloat(worldOrigin.x() - previousOrigin.x()).putFloat(worldOrigin.y() - previousOrigin.y())
                .putFloat(worldOrigin.z() - previousOrigin.z()).putFloat(0);
            bytes.putFloat(eyeOrigin.x()).putFloat(eyeOrigin.y()).putFloat(eyeOrigin.z()).putFloat(0);
            bytes.putInt(width).putInt(height).putInt((reservoirHistory ? 1 : 0) | (taaActive ? 2 : 0)
                    | (history ? 4 : 0) | (allocatedReconstructionExport ? 8 : 0) | (DEBUG_MODE << 8))
                .putInt((int) (submittedFrames & 0x7fffffff));
            bytes.putFloat(CANDIDATES).putFloat(TEMPORAL_SAMPLES).putFloat(SPATIAL_NEIGHBORS).putFloat(SPATIAL_RADIUS);
        });
    }

    private void dispatch(VkCommandBuffer command, long pipeline) {
        vkCmdBindPipeline(command, VK_PIPELINE_BIND_POINT_COMPUTE, pipeline);
        vkCmdDispatch(command, (width + WORKGROUP_SIZE - 1) / WORKGROUP_SIZE, (height + WORKGROUP_SIZE - 1) / WORKGROUP_SIZE, 1);
    }

    private void updateDescriptors(Frame frame, Frame source) {
        try (var stack = MemoryStack.stackPush()) {
            var images = VkDescriptorImageInfo.calloc(SAMPLED_IMAGES, stack);
            for (int i = 0; i < SAMPLED_IMAGES; i++) images.get(i).sampler(sampler)
                .imageView((i < COLOR_ATTACHMENTS ? frame : source).color[i % COLOR_ATTACHMENTS].view)
                .imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
            var write = VkWriteDescriptorSet.calloc(1, stack).sType$Default().dstSet(frame.descriptor).dstBinding(0)
                .descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER).descriptorCount(SAMPLED_IMAGES).pImageInfo(images);
            vkUpdateDescriptorSets(context.device(), write, null);
        }
    }

    private void createBindings(long raySceneLayout) {
        try (var stack = MemoryStack.stackPush()) {
            var handle = stack.mallocLong(1);
            var binding = VkDescriptorSetLayoutBinding.calloc(1, stack).binding(0)
                .descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER).descriptorCount(SAMPLED_IMAGES).stageFlags(VK_SHADER_STAGE_COMPUTE_BIT);
            var layout = VkDescriptorSetLayoutCreateInfo.calloc(stack).sType$Default().pBindings(binding);
            check(vkCreateDescriptorSetLayout(context.device(), layout, null, handle), "create ReSTIR image descriptor layout");
            descriptorLayout = handle.get(0);
            layout.pBindings(null);
            check(vkCreateDescriptorSetLayout(context.device(), layout, null, handle), "create ReSTIR empty descriptor layout");
            emptyLayout = handle.get(0);
            var range = VkPushConstantRange.calloc(1, stack).stageFlags(VK_SHADER_STAGE_COMPUTE_BIT).size(PUSH_BYTES);
            var pipelineInfo = VkPipelineLayoutCreateInfo.calloc(stack).sType$Default()
                .pSetLayouts(stack.longs(descriptorLayout, emptyLayout, emptyLayout, emptyLayout, raySceneLayout)).pPushConstantRanges(range);
            check(vkCreatePipelineLayout(context.device(), pipelineInfo, null, handle), "create ReSTIR compute layout");
            pipelineLayout = handle.get(0);
            var samplerInfo = VkSamplerCreateInfo.calloc(stack).sType$Default().magFilter(VK_FILTER_NEAREST).minFilter(VK_FILTER_NEAREST)
                .mipmapMode(VK_SAMPLER_MIPMAP_MODE_NEAREST).addressModeU(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE)
                .addressModeV(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE).addressModeW(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE).maxAnisotropy(1);
            check(vkCreateSampler(context.device(), samplerInfo, null, handle), "create ReSTIR G-buffer sampler");
            sampler = handle.get(0);
            var poolSize = VkDescriptorPoolSize.calloc(1, stack).type(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
                .descriptorCount(Math.multiplyExact(frames.length, SAMPLED_IMAGES));
            var pool = VkDescriptorPoolCreateInfo.calloc(stack).sType$Default().maxSets(frames.length).pPoolSizes(poolSize);
            check(vkCreateDescriptorPool(context.device(), pool, null, handle), "create ReSTIR descriptor pool");
            descriptorPool = handle.get(0);
            var layouts = stack.mallocLong(frames.length);
            for (int i = 0; i < frames.length; i++) layouts.put(i, descriptorLayout);
            var allocation = VkDescriptorSetAllocateInfo.calloc(stack).sType$Default().descriptorPool(descriptorPool).pSetLayouts(layouts);
            var sets = stack.mallocLong(frames.length);
            check(vkAllocateDescriptorSets(context.device(), allocation, sets), "allocate ReSTIR frame descriptors");
            for (int i = 0; i < frames.length; i++) frames[i].descriptor = sets.get(i);
        }
    }

    private void createRenderPass() {
        try (var stack = MemoryStack.stackPush()) {
            var attachments = VkAttachmentDescription.calloc(COLOR_ATTACHMENTS + 1, stack);
            var colors = VkAttachmentReference.calloc(COLOR_ATTACHMENTS, stack);
            for (int i = 0; i < COLOR_ATTACHMENTS; i++) {
                attachments.get(i).format(i == 0 ? POSITION_FORMAT : i == 1 ? NORMAL_FORMAT : MATERIAL_FORMAT)
                    .samples(VK_SAMPLE_COUNT_1_BIT).loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR).storeOp(VK_ATTACHMENT_STORE_OP_STORE)
                    .stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE).stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE)
                    .initialLayout(VK_IMAGE_LAYOUT_UNDEFINED).finalLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
                colors.get(i).attachment(i).layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
            }
            attachments.get(COLOR_ATTACHMENTS).format(DEPTH_FORMAT).samples(VK_SAMPLE_COUNT_1_BIT)
                .loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR).storeOp(VK_ATTACHMENT_STORE_OP_DONT_CARE)
                .stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE).stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE)
                .initialLayout(VK_IMAGE_LAYOUT_UNDEFINED).finalLayout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
            var depth = VkAttachmentReference.calloc(stack).attachment(COLOR_ATTACHMENTS).layout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
            var subpass = VkSubpassDescription.calloc(1, stack).pipelineBindPoint(VK_PIPELINE_BIND_POINT_GRAPHICS)
                .colorAttachmentCount(COLOR_ATTACHMENTS).pColorAttachments(colors).pDepthStencilAttachment(depth);
            var dependencies = VkSubpassDependency.calloc(2, stack);
            dependencies.get(0).srcSubpass(VK_SUBPASS_EXTERNAL).dstSubpass(0)
                .srcStageMask(VK_PIPELINE_STAGE_ALL_COMMANDS_BIT).dstStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT | VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT)
                .srcAccessMask(VK_ACCESS_MEMORY_READ_BIT | VK_ACCESS_MEMORY_WRITE_BIT)
                .dstAccessMask(VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT | VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT);
            dependencies.get(1).srcSubpass(0).dstSubpass(VK_SUBPASS_EXTERNAL)
                .srcStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT).dstStageMask(VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT)
                .srcAccessMask(VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT).dstAccessMask(VK_ACCESS_SHADER_READ_BIT);
            var info = VkRenderPassCreateInfo.calloc(stack).sType$Default().pAttachments(attachments).pSubpasses(subpass).pDependencies(dependencies);
            var handle = stack.mallocLong(1);
            check(vkCreateRenderPass(context.device(), info, null, handle), "create ReSTIR capture render pass");
            renderPass = handle.get(0);
        }
    }

    private long createPipeline(String name) {
        String path = "/assets/adventurecraft/shaders/vulkan/" + name + ".spv";
        try (InputStream stream = VulkanRestirDi.class.getResourceAsStream(path)) {
            if (stream == null) throw new IllegalStateException("Missing compiled Vulkan shader " + path);
            byte[] bytes = stream.readAllBytes();
            if (bytes.length == 0 || bytes.length % 4 != 0) throw new IllegalStateException("Invalid SPIR-V shader " + path);
            ByteBuffer code = MemoryUtil.memAlloc(bytes.length).order(ByteOrder.nativeOrder());
            try (var stack = MemoryStack.stackPush()) {
                code.put(bytes).flip();
                var moduleInfo = VkShaderModuleCreateInfo.calloc(stack).sType$Default().pCode(code);
                var handle = stack.mallocLong(1);
                check(vkCreateShaderModule(context.device(), moduleInfo, null, handle), "create ReSTIR shader " + name);
                long module = handle.get(0);
                try {
                    var info = VkComputePipelineCreateInfo.calloc(1, stack).sType$Default().layout(pipelineLayout);
                    info.stage().sType$Default().stage(VK_SHADER_STAGE_COMPUTE_BIT).module(module).pName(stack.UTF8("main"));
                    check(vkCreateComputePipelines(context.device(), VK_NULL_HANDLE, info, null, handle), "create ReSTIR pipeline " + name);
                    return handle.get(0);
                } finally {
                    vkDestroyShaderModule(context.device(), module, null);
                }
            } finally {
                MemoryUtil.memFree(code);
            }
        } catch (IOException error) {
            throw new IllegalStateException("Cannot read ReSTIR shader " + path, error);
        }
    }

    private static void computeBarrier(VkCommandBuffer command) {
        barrier(command, VK_ACCESS_SHADER_WRITE_BIT, VK_ACCESS_SHADER_READ_BIT | VK_ACCESS_SHADER_WRITE_BIT,
            VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT, VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT);
    }

    private static void barrier(VkCommandBuffer command, int sourceAccess, int targetAccess, int sourceStage, int targetStage) {
        try (var stack = MemoryStack.stackPush()) {
            var memory = VkMemoryBarrier.calloc(1, stack).sType$Default().srcAccessMask(sourceAccess).dstAccessMask(targetAccess);
            vkCmdPipelineBarrier(command, sourceStage, targetStage, 0, memory, null, null);
        }
    }

    private Frame frame(int index) {
        if (index < 0 || index >= frames.length) throw new IndexOutOfBoundsException(index);
        return frames[index];
    }

    private void ensureOpen() {
        if (renderPass == VK_NULL_HANDLE) throw new IllegalStateException("ReSTIR resources are closed");
    }

    private void closeTargets(Frame frame) {
        if (frame.framebuffer != VK_NULL_HANDLE) vkDestroyFramebuffer(context.device(), frame.framebuffer, null);
        frame.framebuffer = VK_NULL_HANDLE;
        for (int i = 0; i < frame.color.length; i++) {
            if (frame.color[i] != null) frame.color[i].close();
            frame.color[i] = null;
        }
        if (frame.depth != null) frame.depth.close();
        if (frame.reservoir != null) frame.reservoir.close();
        if (frame.result != null) frame.result.close();
        if (frame.parameters != null) frame.parameters.close();
        frame.depth = null;
        frame.reservoir = frame.result = frame.parameters = null;
        frame.viewProjection = null;
        frame.worldOrigin = null;
        frame.recorded = frame.captured = false;
        frame.fresh = true;
        frame.historyFrames = 0;
    }

    private void closeScratch() {
        if (initialScratch != null) initialScratch.close();
        if (rawScratch != null) rawScratch.close();
        initialScratch = rawScratch = null;
        scratchFresh = true;
    }

    private static int setting(String name, int fallback, int min, int max) {
        try { return Math.clamp(Integer.parseInt(System.getProperty(name, Integer.toString(fallback))), min, max); }
        catch (NumberFormatException ignored) { return fallback; }
    }

    private static boolean atLeast(int unsignedLimit, int required) {
        return Integer.compareUnsigned(unsignedLimit, required) >= 0;
    }

    private static boolean cameraJump(Frame previous, Matrix4 viewProjection, Vector4 origin) {
        double x = (double) origin.x() - previous.worldOrigin.x();
        double y = (double) origin.y() - previous.worldOrigin.y();
        double z = (double) origin.z() - previous.worldOrigin.z();
        if (!Double.isFinite(x + y + z) || x * x + y * y + z * z > 64) return true;
        // Smooth motion reprojects normally; a large instantaneous rotation, zoom or eye transform starts fresh.
        for (int i = 0; i < 16; i++) {
            float current = viewProjection.get(i);
            if (!Float.isFinite(current) || Math.abs(current - previous.viewProjection.get(i)) > .75f) return true;
        }
        return false;
    }

    /** All renderer submissions must finish before closing these shared history resources. */
    @Override public void close() {
        for (Frame frame : frames) closeTargets(frame);
        closeScratch();
        if (initialPipeline != VK_NULL_HANDLE) vkDestroyPipeline(context.device(), initialPipeline, null);
        if (spatialPipeline != VK_NULL_HANDLE) vkDestroyPipeline(context.device(), spatialPipeline, null);
        if (denoisePipeline != VK_NULL_HANDLE) vkDestroyPipeline(context.device(), denoisePipeline, null);
        if (descriptorPool != VK_NULL_HANDLE) vkDestroyDescriptorPool(context.device(), descriptorPool, null);
        if (sampler != VK_NULL_HANDLE) vkDestroySampler(context.device(), sampler, null);
        if (pipelineLayout != VK_NULL_HANDLE) vkDestroyPipelineLayout(context.device(), pipelineLayout, null);
        if (descriptorLayout != VK_NULL_HANDLE) vkDestroyDescriptorSetLayout(context.device(), descriptorLayout, null);
        if (emptyLayout != VK_NULL_HANDLE) vkDestroyDescriptorSetLayout(context.device(), emptyLayout, null);
        if (renderPass != VK_NULL_HANDLE) vkDestroyRenderPass(context.device(), renderPass, null);
        initialPipeline = spatialPipeline = denoisePipeline = descriptorPool = sampler = pipelineLayout = descriptorLayout = emptyLayout = renderPass = VK_NULL_HANDLE;
        previous = null;
        width = height = 0;
        capturing = -1;
    }
}
