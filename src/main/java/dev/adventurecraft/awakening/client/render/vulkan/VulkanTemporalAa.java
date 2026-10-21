package dev.adventurecraft.awakening.client.render.vulkan;

import dev.adventurecraft.awakening.client.render.RenderMath;
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

/** Floating-point temporal resolve and pristine world-only history in the world's current color domain. */
final class VulkanTemporalAa implements AutoCloseable {
    private static final int PARAM_BYTES = 272;
    private static final int SAMPLED_IMAGES = 5;
    private static final int WORKGROUP_SIZE = 8;
    private static final int COMPUTE = VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT;
    private static final int COLOR_FORMAT = VK_FORMAT_R16G16B16A16_SFLOAT;
    private static final int HISTORY_DEPTH_FORMAT = VK_FORMAT_R32_SFLOAT;
    private static final int DEPTH_FORMAT = VK_FORMAT_D32_SFLOAT;

    record Statistics(long frames, long resets, int width, int height, int historyFrames) { }

    private static final class Frame {
        VulkanImage color;
        VulkanImage depth;
        VulkanBuffer parameters;
        long descriptor;
        Matrix4 viewProjection;
        Matrix4 inverseViewProjection;
        Vector4 worldOrigin;
        Vector4 eyeOrigin;
        float jitterX;
        float jitterY;
        long epoch;
        int historyFrames;
        boolean fresh = true;
        boolean recorded;
        boolean reusedHistory;
        boolean coverageAccumulation;
        boolean linearHdr;
    }

    private final VulkanDevice context;
    private final Frame[] frames;
    private final float historyWeight = scalar("ac.vulkan.taaHistoryWeight", .9f, 0, .97f);
    private final float varianceGamma = scalar("ac.vulkan.taaVarianceGamma", 1.25f, .5f, 3);
    private final float depthTolerance = scalar("ac.vulkan.taaDepthTolerance", .025f, .001f, .25f);
    private long descriptorLayout;
    private long descriptorPool;
    private long pipelineLayout;
    private long pipeline;
    private long linearSampler;
    private long nearestSampler;
    private Frame previous;
    private int width;
    private int height;
    private long epoch;
    private long submittedFrames;
    private long historyResets;

    static boolean supported(VulkanDevice context) {
        Objects.requireNonNull(context, "context");
        if (!context.independentBlendSupported()) return false;
        try (var stack = MemoryStack.stackPush()) {
            var properties = VkPhysicalDeviceProperties.malloc(stack);
            vkGetPhysicalDeviceProperties(context.physicalDevice(), properties);
            var limits = properties.limits();
            if (!atLeast(limits.maxColorAttachments(), 2)
                || !atLeast(limits.maxPerStageDescriptorSamplers(), SAMPLED_IMAGES)
                || !atLeast(limits.maxDescriptorSetSamplers(), SAMPLED_IMAGES)
                || !atLeast(limits.maxPerStageDescriptorSampledImages(), SAMPLED_IMAGES)
                || !atLeast(limits.maxDescriptorSetSampledImages(), SAMPLED_IMAGES)
                || !atLeast(limits.maxPerStageDescriptorStorageImages(), 2)
                || !atLeast(limits.maxDescriptorSetStorageImages(), 2)
                || !atLeast(limits.maxPerStageDescriptorUniformBuffers(), 1)
                || !atLeast(limits.maxDescriptorSetUniformBuffers(), 1)
                || !atLeast(limits.maxUniformBufferRange(), PARAM_BYTES)
                || !atLeast(limits.maxPerStageResources(), SAMPLED_IMAGES + 3)
                || !atLeast(limits.maxComputeWorkGroupInvocations(), WORKGROUP_SIZE * WORKGROUP_SIZE)
                || !atLeast(limits.maxComputeWorkGroupSize(0), WORKGROUP_SIZE)
                || !atLeast(limits.maxComputeWorkGroupSize(1), WORKGROUP_SIZE)) return false;
            var count = stack.mallocInt(1);
            vkGetPhysicalDeviceQueueFamilyProperties(context.physicalDevice(), count, null);
            var queues = VkQueueFamilyProperties.malloc(count.get(0), stack);
            vkGetPhysicalDeviceQueueFamilyProperties(context.physicalDevice(), count, queues);
            if ((queues.get(context.queueFamily()).queueFlags() & VK_QUEUE_COMPUTE_BIT) == 0) return false;
            var format = VkFormatProperties.malloc(stack);
            int color = VK_FORMAT_FEATURE_COLOR_ATTACHMENT_BIT | VK_FORMAT_FEATURE_SAMPLED_IMAGE_BIT | VK_FORMAT_FEATURE_STORAGE_IMAGE_BIT
                | VK_FORMAT_FEATURE_SAMPLED_IMAGE_FILTER_LINEAR_BIT;
            if (!format(context, COLOR_FORMAT, color, format)
                || !format(context, HISTORY_DEPTH_FORMAT, VK_FORMAT_FEATURE_SAMPLED_IMAGE_BIT | VK_FORMAT_FEATURE_STORAGE_IMAGE_BIT, format)
                || !format(context, DEPTH_FORMAT, VK_FORMAT_FEATURE_SAMPLED_IMAGE_BIT | VK_FORMAT_FEATURE_DEPTH_STENCIL_ATTACHMENT_BIT, format)) return false;
            return reactiveFormat(context, format) != VK_FORMAT_UNDEFINED;
        }
    }

    /** The renderer and the support probe must agree on the reactive attachment format. */
    static int reactiveFormat(VulkanDevice context) {
        try (var stack = MemoryStack.stackPush()) { return reactiveFormat(context, VkFormatProperties.malloc(stack)); }
    }

    private static int reactiveFormat(VulkanDevice context, VkFormatProperties properties) {
        int required = VK_FORMAT_FEATURE_SAMPLED_IMAGE_BIT | VK_FORMAT_FEATURE_COLOR_ATTACHMENT_BIT | VK_FORMAT_FEATURE_COLOR_ATTACHMENT_BLEND_BIT;
        if (format(context, VK_FORMAT_R8_UNORM, required, properties)) return VK_FORMAT_R8_UNORM;
        return format(context, VK_FORMAT_R8G8B8A8_UNORM, required, properties) ? VK_FORMAT_R8G8B8A8_UNORM : VK_FORMAT_UNDEFINED;
    }

    private static boolean format(VulkanDevice context, int value, int required, VkFormatProperties properties) {
        vkGetPhysicalDeviceFormatProperties(context.physicalDevice(), value, properties);
        return (properties.optimalTilingFeatures() & required) == required;
    }

    private static boolean atLeast(int unsignedLimit, int value) { return Integer.compareUnsigned(unsignedLimit, value) >= 0; }

    VulkanTemporalAa(VulkanDevice context, int frameCount) {
        this.context = Objects.requireNonNull(context, "context");
        if (frameCount < 2) throw new IllegalArgumentException("Temporal AA requires at least two frame slots");
        if (!supported(context)) throw new IllegalStateException("HDR temporal AA formats or compute limits are unsupported");
        frames = new Frame[frameCount];
        for (int i = 0; i < frames.length; i++) frames[i] = new Frame();
        try {
            createBindings();
            pipeline = createPipeline();
            for (Frame frame : frames) frame.parameters = context.hostBuffer(PARAM_BYTES, VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT);
        } catch (RuntimeException | Error error) {
            close();
            throw error;
        }
    }

    Statistics statistics() {
        return new Statistics(submittedFrames, historyResets, width, height, previous == null ? 0 : previous.historyFrames);
    }

    VulkanImage result(int slot) {
        Frame frame = frame(slot);
        if (frame.color == null) throw new IllegalStateException("Resize temporal AA targets before requesting a result");
        return frame.color;
    }

    void invalidateHistory() { previous = null; epoch++; }

    /** A later submission can still sample an older slot, so replacing history waits for the complete queue. */
    void resize(int width, int height) {
        ensureOpen();
        if (width < 1 || height < 1) throw new IllegalArgumentException("Temporal AA dimensions must be positive");
        if (this.width == width && this.height == height) return;
        context.waitIdle();
        closeImages();
        invalidateHistory();
        try {
            int usage = VK_IMAGE_USAGE_STORAGE_BIT | VK_IMAGE_USAGE_SAMPLED_BIT | VK_IMAGE_USAGE_TRANSFER_SRC_BIT | VK_IMAGE_USAGE_TRANSFER_DST_BIT;
            for (Frame frame : frames) {
                frame.color = VulkanImage.create(context, width, height, COLOR_FORMAT, usage, VK_IMAGE_ASPECT_COLOR_BIT, 1);
                frame.depth = VulkanImage.create(context, width, height, HISTORY_DEPTH_FORMAT, usage, VK_IMAGE_ASPECT_COLOR_BIT, 1);
            }
            this.width = width;
            this.height = height;
        } catch (RuntimeException | Error error) {
            closeImages();
            throw error;
        }
    }

    /**
     * Call after this slot's fence and world rendering, before hands or overlays. All three inputs enter
     * and leave TRANSFER_SRC_OPTIMAL. The resolved color leaves in that same layout for copying or post processing.
     */
    void record(VkCommandBuffer command, int slot, VulkanImage source, VulkanImage depth, VulkanImage reactive,
                Matrix4 currentJitteredViewProjection, Vector4 worldOrigin, Vector4 eyeOrigin,
                float jitterNdcX, float jitterNdcY, boolean cameraCut, boolean linearHdr) {
        ensureOpen();
        Frame frame = frame(slot);
        if (frame.color == null || frame.recorded) throw new IllegalStateException("Temporal AA needs resized, unsubmitted targets");
        checkInput(source, VK_IMAGE_ASPECT_COLOR_BIT);
        checkInput(depth, VK_IMAGE_ASPECT_DEPTH_BIT);
        checkInput(reactive, VK_IMAGE_ASPECT_COLOR_BIT);
        if (source == frame.color || depth == frame.depth || source == reactive) throw new IllegalArgumentException("Temporal AA inputs must be distinct from their outputs");
        Objects.requireNonNull(currentJitteredViewProjection, "currentJitteredViewProjection");
        requireFinite(worldOrigin, "worldOrigin");
        requireFinite(eyeOrigin, "eyeOrigin");
        if (!Float.isFinite(jitterNdcX) || !Float.isFinite(jitterNdcY)) throw new IllegalArgumentException("Temporal AA jitter must be finite");
        Matrix4 inverseCurrent = inverse(currentJitteredViewProjection);
        boolean history = !cameraCut && previous != null && previous != frame && previous.epoch == epoch
            && previous.linearHdr == linearHdr
            && !cameraJump(previous.viewProjection, previous.worldOrigin, previous.eyeOrigin,
                currentJitteredViewProjection, worldOrigin, eyeOrigin);
        Frame preceding = history ? previous : null;
        boolean coverageAccumulation = history && cameraUnchanged(preceding, currentJitteredViewProjection,
            worldOrigin, eyeOrigin, jitterNdcX, jitterNdcY);
        updateDescriptors(frame, preceding, source, depth, reactive);
        writeParameters(frame, preceding, currentJitteredViewProjection, inverseCurrent, worldOrigin, eyeOrigin,
            jitterNdcX, jitterNdcY, coverageAccumulation, linearHdr);

        sampleInput(command, source, VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
        sampleInput(command, depth, VK_IMAGE_LAYOUT_DEPTH_STENCIL_READ_ONLY_OPTIMAL);
        sampleInput(command, reactive, VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
        if (history) VulkanImage.barrier(command, preceding.color, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
            VK_ACCESS_SHADER_READ_BIT | VK_ACCESS_TRANSFER_READ_BIT, VK_ACCESS_SHADER_READ_BIT,
            COMPUTE | VK_PIPELINE_STAGE_TRANSFER_BIT, COMPUTE);
        prepareOutput(command, frame.color, frame.fresh ? VK_IMAGE_LAYOUT_UNDEFINED : VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, frame.fresh);
        prepareOutput(command, frame.depth, frame.fresh ? VK_IMAGE_LAYOUT_UNDEFINED : VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL, frame.fresh);
        try (var stack = MemoryStack.stackPush()) {
            vkCmdBindPipeline(command, VK_PIPELINE_BIND_POINT_COMPUTE, pipeline);
            vkCmdBindDescriptorSets(command, VK_PIPELINE_BIND_POINT_COMPUTE, pipelineLayout, 0, stack.longs(frame.descriptor), null);
            vkCmdDispatch(command, (width + WORKGROUP_SIZE - 1) / WORKGROUP_SIZE, (height + WORKGROUP_SIZE - 1) / WORKGROUP_SIZE, 1);
        }
        VulkanImage.barrier(command, frame.color, VK_IMAGE_LAYOUT_GENERAL, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
            VK_ACCESS_SHADER_WRITE_BIT, VK_ACCESS_TRANSFER_READ_BIT, COMPUTE, VK_PIPELINE_STAGE_TRANSFER_BIT);
        VulkanImage.barrier(command, frame.depth, VK_IMAGE_LAYOUT_GENERAL, VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
            VK_ACCESS_SHADER_WRITE_BIT, VK_ACCESS_SHADER_READ_BIT, COMPUTE, COMPUTE);
        if (history) restoreInput(command, preceding.color, VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
        restoreInput(command, source, VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
        restoreInput(command, depth, VK_IMAGE_LAYOUT_DEPTH_STENCIL_READ_ONLY_OPTIMAL);
        restoreInput(command, reactive, VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
        frame.viewProjection = currentJitteredViewProjection;
        frame.inverseViewProjection = inverseCurrent;
        frame.worldOrigin = worldOrigin;
        frame.eyeOrigin = eyeOrigin;
        frame.jitterX = jitterNdcX;
        frame.jitterY = jitterNdcY;
        frame.historyFrames = history ? Math.min(preceding.historyFrames + 1, 65535) : 1;
        frame.epoch = epoch;
        frame.reusedHistory = history;
        frame.coverageAccumulation = coverageAccumulation;
        frame.linearHdr = linearHdr;
        frame.recorded = true;
    }

    void submitted(int slot) {
        Frame frame = frame(slot);
        if (!frame.recorded) throw new IllegalStateException("No temporal AA work was recorded for this slot");
        frame.recorded = false;
        frame.fresh = false;
        previous = frame.epoch == epoch ? frame : null;
        if (!frame.reusedHistory) historyResets++;
        submittedFrames++;
    }

    void abort(int slot) { frame(slot).recorded = false; invalidateHistory(); }

    private void checkInput(VulkanImage image, int aspect) {
        if (image == null || image.width != width || image.height != height || image.aspect != aspect)
            throw new IllegalArgumentException("Temporal AA input extent or image aspect is incompatible");
    }

    private static void sampleInput(VkCommandBuffer command, VulkanImage image, int sampledLayout) {
        VulkanImage.barrier(command, image, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, sampledLayout,
            VK_ACCESS_MEMORY_WRITE_BIT | VK_ACCESS_TRANSFER_READ_BIT, VK_ACCESS_SHADER_READ_BIT,
            VK_PIPELINE_STAGE_ALL_COMMANDS_BIT, COMPUTE);
    }

    private static void restoreInput(VkCommandBuffer command, VulkanImage image, int sampledLayout) {
        VulkanImage.barrier(command, image, sampledLayout, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
            VK_ACCESS_SHADER_READ_BIT, VK_ACCESS_TRANSFER_READ_BIT, COMPUTE, VK_PIPELINE_STAGE_TRANSFER_BIT);
    }

    private static void prepareOutput(VkCommandBuffer command, VulkanImage image, int previousLayout, boolean fresh) {
        VulkanImage.barrier(command, image, previousLayout, VK_IMAGE_LAYOUT_GENERAL,
            fresh ? 0 : VK_ACCESS_SHADER_READ_BIT | VK_ACCESS_TRANSFER_READ_BIT, VK_ACCESS_SHADER_WRITE_BIT,
            fresh ? VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT : COMPUTE | VK_PIPELINE_STAGE_TRANSFER_BIT, COMPUTE);
    }

    private void writeParameters(Frame frame, Frame preceding, Matrix4 current, Matrix4 inverseCurrent,
                                 Vector4 worldOrigin, Vector4 eyeOrigin, float jitterX, float jitterY,
                                 boolean coverageAccumulation, boolean linearHdr) {
        frame.parameters.write(bytes -> {
            matrix(bytes, inverseCurrent);
            matrix(bytes, preceding == null ? current : preceding.viewProjection);
            matrix(bytes, preceding == null ? inverseCurrent : preceding.inverseViewProjection);
            Vector4 previousOrigin = preceding == null ? worldOrigin : preceding.worldOrigin;
            bytes.putFloat(worldOrigin.x() - previousOrigin.x()).putFloat(worldOrigin.y() - previousOrigin.y())
                .putFloat(worldOrigin.z() - previousOrigin.z()).putFloat(0);
            bytes.putInt(width).putInt(height).putInt(preceding == null ? 0 : 1).putInt((int) (submittedFrames & 0x7fffffff));
            int coverageFlags = (coverageAccumulation ? 1 : 0) | (preceding != null && preceding.coverageAccumulation ? 2 : 0)
                | (linearHdr ? 4 : 0);
            bytes.putFloat(historyWeight).putFloat(varianceGamma).putFloat(depthTolerance).putFloat(coverageFlags);
            bytes.putFloat(eyeOrigin.x()).putFloat(eyeOrigin.y()).putFloat(eyeOrigin.z()).putFloat(0);
            bytes.putFloat(jitterX).putFloat(jitterY).putFloat(preceding == null ? jitterX : preceding.jitterX)
                .putFloat(preceding == null ? jitterY : preceding.jitterY);
        });
    }

    private static void matrix(ByteBuffer bytes, Matrix4 matrix) { for (float value : matrix.copyArray()) bytes.putFloat(value); }

    /** Selects exact pixel correspondence for coverage while the unjittered camera stays fixed. */
    private static boolean cameraUnchanged(Frame previous, Matrix4 current, Vector4 origin, Vector4 eye, float jitterX, float jitterY) {
        if (previous.worldOrigin.x() != origin.x() || previous.worldOrigin.y() != origin.y() || previous.worldOrigin.z() != origin.z()
            || previous.eyeOrigin.x() != eye.x() || previous.eyeOrigin.y() != eye.y() || previous.eyeOrigin.z() != eye.z()) return false;
        for (int column = 0; column < 4; column++) {
            for (int row = 0; row < 4; row++) {
                int index = column * 4 + row;
                float a = current.get(index), b = previous.viewProjection.get(index);
                if (row < 2) {
                    a -= (row == 0 ? jitterX : jitterY) * current.get(column * 4 + 3);
                    b -= (row == 0 ? previous.jitterX : previous.jitterY) * previous.viewProjection.get(column * 4 + 3);
                }
                if (Math.abs(a - b) > 1e-6f * Math.max(1, Math.max(Math.abs(a), Math.abs(b)))) return false;
            }
        }
        return true;
    }

    private void updateDescriptors(Frame frame, Frame preceding, VulkanImage source, VulkanImage depth, VulkanImage reactive) {
        try (var stack = MemoryStack.stackPush()) {
            VulkanImage[] inputs = {source, depth, reactive, preceding == null ? source : preceding.color, preceding == null ? depth : preceding.depth};
            var sampled = VkDescriptorImageInfo.calloc(SAMPLED_IMAGES, stack);
            for (int i = 0; i < inputs.length; i++) sampled.get(i).sampler(i == 0 || i == 3 ? linearSampler : nearestSampler)
                .imageView(inputs[i].view).imageLayout(inputs[i].aspect == VK_IMAGE_ASPECT_DEPTH_BIT
                    ? VK_IMAGE_LAYOUT_DEPTH_STENCIL_READ_ONLY_OPTIMAL : VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
            var output = VkDescriptorImageInfo.calloc(2, stack);
            output.get(0).imageView(frame.color.view).imageLayout(VK_IMAGE_LAYOUT_GENERAL);
            output.get(1).imageView(frame.depth.view).imageLayout(VK_IMAGE_LAYOUT_GENERAL);
            var uniform = VkDescriptorBufferInfo.calloc(1, stack).buffer(frame.parameters.handle()).range(PARAM_BYTES);
            var writes = VkWriteDescriptorSet.calloc(4, stack);
            writes.get(0).sType$Default().dstSet(frame.descriptor).dstBinding(0).descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
                .descriptorCount(SAMPLED_IMAGES).pImageInfo(sampled);
            for (int i = 0; i < 2; i++) writes.get(i + 1).sType$Default().dstSet(frame.descriptor).dstBinding(i + 1)
                .descriptorType(VK_DESCRIPTOR_TYPE_STORAGE_IMAGE).descriptorCount(1).pImageInfo(VkDescriptorImageInfo.create(output.get(i).address(), 1));
            writes.get(3).sType$Default().dstSet(frame.descriptor).dstBinding(3).descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
                .descriptorCount(1).pBufferInfo(uniform);
            vkUpdateDescriptorSets(context.device(), writes, null);
        }
    }

    private void createBindings() {
        try (var stack = MemoryStack.stackPush()) {
            var handle = stack.mallocLong(1);
            var bindings = VkDescriptorSetLayoutBinding.calloc(4, stack);
            bindings.get(0).binding(0).descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER).descriptorCount(SAMPLED_IMAGES).stageFlags(VK_SHADER_STAGE_COMPUTE_BIT);
            for (int i = 1; i <= 2; i++) bindings.get(i).binding(i).descriptorType(VK_DESCRIPTOR_TYPE_STORAGE_IMAGE).descriptorCount(1).stageFlags(VK_SHADER_STAGE_COMPUTE_BIT);
            bindings.get(3).binding(3).descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER).descriptorCount(1).stageFlags(VK_SHADER_STAGE_COMPUTE_BIT);
            var descriptorInfo = VkDescriptorSetLayoutCreateInfo.calloc(stack).sType$Default().pBindings(bindings);
            check(vkCreateDescriptorSetLayout(context.device(), descriptorInfo, null, handle), "create temporal AA descriptor layout");
            descriptorLayout = handle.get(0);
            var layoutInfo = VkPipelineLayoutCreateInfo.calloc(stack).sType$Default().pSetLayouts(stack.longs(descriptorLayout));
            check(vkCreatePipelineLayout(context.device(), layoutInfo, null, handle), "create temporal AA pipeline layout");
            pipelineLayout = handle.get(0);
            var sampler = VkSamplerCreateInfo.calloc(stack).sType$Default().magFilter(VK_FILTER_LINEAR).minFilter(VK_FILTER_LINEAR)
                .mipmapMode(VK_SAMPLER_MIPMAP_MODE_NEAREST).addressModeU(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE)
                .addressModeV(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE).addressModeW(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE).maxAnisotropy(1);
            check(vkCreateSampler(context.device(), sampler, null, handle), "create temporal AA color sampler");
            linearSampler = handle.get(0);
            sampler.magFilter(VK_FILTER_NEAREST).minFilter(VK_FILTER_NEAREST);
            check(vkCreateSampler(context.device(), sampler, null, handle), "create temporal AA data sampler");
            nearestSampler = handle.get(0);
            var sizes = VkDescriptorPoolSize.calloc(3, stack);
            sizes.get(0).type(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER).descriptorCount(frames.length * SAMPLED_IMAGES);
            sizes.get(1).type(VK_DESCRIPTOR_TYPE_STORAGE_IMAGE).descriptorCount(frames.length * 2);
            sizes.get(2).type(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER).descriptorCount(frames.length);
            var pool = VkDescriptorPoolCreateInfo.calloc(stack).sType$Default().maxSets(frames.length).pPoolSizes(sizes);
            check(vkCreateDescriptorPool(context.device(), pool, null, handle), "create temporal AA descriptor pool");
            descriptorPool = handle.get(0);
            var layouts = stack.mallocLong(frames.length);
            for (int i = 0; i < frames.length; i++) layouts.put(i, descriptorLayout);
            var allocation = VkDescriptorSetAllocateInfo.calloc(stack).sType$Default().descriptorPool(descriptorPool).pSetLayouts(layouts);
            var sets = stack.mallocLong(frames.length);
            check(vkAllocateDescriptorSets(context.device(), allocation, sets), "allocate temporal AA frame descriptors");
            for (int i = 0; i < frames.length; i++) frames[i].descriptor = sets.get(i);
        }
    }

    private long createPipeline() {
        String path = "/assets/adventurecraft/shaders/vulkan/taa_resolve.comp.spv";
        try (InputStream stream = VulkanTemporalAa.class.getResourceAsStream(path)) {
            if (stream == null) throw new IllegalStateException("Missing temporal AA shader " + path);
            byte[] bytes = stream.readAllBytes();
            if (bytes.length == 0 || bytes.length % 4 != 0) throw new IllegalStateException("Invalid temporal AA shader " + path);
            ByteBuffer code = MemoryUtil.memAlloc(bytes.length).order(ByteOrder.nativeOrder());
            try (var stack = MemoryStack.stackPush()) {
                code.put(bytes).flip();
                var moduleInfo = VkShaderModuleCreateInfo.calloc(stack).sType$Default().pCode(code);
                var handle = stack.mallocLong(1);
                check(vkCreateShaderModule(context.device(), moduleInfo, null, handle), "create temporal AA shader");
                long module = handle.get(0);
                try {
                    var info = VkComputePipelineCreateInfo.calloc(1, stack).sType$Default().layout(pipelineLayout);
                    info.stage().sType$Default().stage(VK_SHADER_STAGE_COMPUTE_BIT).module(module).pName(stack.UTF8("main"));
                    check(vkCreateComputePipelines(context.device(), VK_NULL_HANDLE, info, null, handle), "create temporal AA compute pipeline");
                    return handle.get(0);
                } finally { vkDestroyShaderModule(context.device(), module, null); }
            } finally { MemoryUtil.memFree(code); }
        } catch (IOException error) { throw new IllegalStateException("Cannot read temporal AA shader " + path, error); }
    }

    /** Centered subpixel offsets; multiplying by (2/width, 2/height) converts these to clip-space offsets. */
    static Vector4 jitter(long sequence) {
        int index = (int) Math.floorMod(sequence, 16) + 1;
        return new Vector4(halton(index, 2) - .5f, halton(index, 3) - .5f, 0, 0);
    }

    private static float halton(int index, int base) {
        float sum = 0, fraction = 1;
        for (; index > 0; index /= base) { fraction /= base; sum += fraction * (index % base); }
        return sum;
    }

    /** Applies the same clip-space x/y += jitter*w operation as the world vertex shaders. */
    static Matrix4 jittered(Matrix4 viewProjection, float ndcX, float ndcY) {
        if (!Float.isFinite(ndcX) || !Float.isFinite(ndcY)) throw new IllegalArgumentException("Temporal AA jitter must be finite");
        float[] values = viewProjection.copyArray();
        for (int column = 0; column < 4; column++) {
            values[column * 4] += ndcX * values[column * 4 + 3];
            values[column * 4 + 1] += ndcY * values[column * 4 + 3];
        }
        return Matrix4.of(values);
    }

    static boolean cameraJump(Matrix4 previousView, Vector4 previousOrigin, Vector4 previousEye,
                              Matrix4 currentView, Vector4 currentOrigin, Vector4 currentEye) {
        double x = (double) currentOrigin.x() - previousOrigin.x() + currentEye.x() - previousEye.x();
        double y = (double) currentOrigin.y() - previousOrigin.y() + currentEye.y() - previousEye.y();
        double z = (double) currentOrigin.z() - previousOrigin.z() + currentEye.z() - previousEye.z();
        if (!Double.isFinite(x + y + z) || x * x + y * y + z * z > 64) return true;
        for (int i = 0; i < 16; i++) if (!Float.isFinite(currentView.get(i)) || !Float.isFinite(previousView.get(i))
            || Math.abs(currentView.get(i) - previousView.get(i)) > .75f) return true;
        return false;
    }

    static Matrix4 inverse(Matrix4 matrix) {
        for (int i = 0; i < 16; i++) if (!Float.isFinite(matrix.get(i))) throw new IllegalArgumentException("Temporal AA camera matrix must be finite");
        double[] values = RenderMath.inverse(matrix);
        float[] result = new float[16];
        for (int i = 0; i < values.length; i++) {
            result[i] = (float) values[i];
            if (!Float.isFinite(result[i])) throw new IllegalArgumentException("Temporal AA inverse matrix must be finite");
        }
        return Matrix4.of(result);
    }

    private static void requireFinite(Vector4 value, String name) {
        Objects.requireNonNull(value, name);
        if (!Float.isFinite(value.x()) || !Float.isFinite(value.y()) || !Float.isFinite(value.z()))
            throw new IllegalArgumentException("Temporal AA " + name + " must be finite");
    }

    private static float scalar(String name, float fallback, float min, float max) {
        try {
            float value = Float.parseFloat(System.getProperty(name, Float.toString(fallback)));
            return Float.isFinite(value) ? Math.clamp(value, min, max) : fallback;
        } catch (NumberFormatException ignored) { return fallback; }
    }

    private Frame frame(int slot) {
        if (slot < 0 || slot >= frames.length) throw new IndexOutOfBoundsException(slot);
        return frames[slot];
    }

    private void ensureOpen() { if (pipelineLayout == VK_NULL_HANDLE) throw new IllegalStateException("Temporal AA resources are closed"); }

    private void closeImages() {
        for (Frame frame : frames) {
            if (frame.color != null) frame.color.close();
            if (frame.depth != null) frame.depth.close();
            frame.color = frame.depth = null;
            frame.fresh = true;
            frame.recorded = false;
            frame.historyFrames = 0;
            frame.coverageAccumulation = false;
        }
        width = height = 0;
    }

    /** The renderer finishes its queue before closing temporal history and descriptors. */
    @Override public void close() {
        closeImages();
        for (Frame frame : frames) {
            if (frame.parameters != null) frame.parameters.close();
            frame.parameters = null;
        }
        if (pipeline != VK_NULL_HANDLE) vkDestroyPipeline(context.device(), pipeline, null);
        if (descriptorPool != VK_NULL_HANDLE) vkDestroyDescriptorPool(context.device(), descriptorPool, null);
        if (linearSampler != VK_NULL_HANDLE) vkDestroySampler(context.device(), linearSampler, null);
        if (nearestSampler != VK_NULL_HANDLE) vkDestroySampler(context.device(), nearestSampler, null);
        if (pipelineLayout != VK_NULL_HANDLE) vkDestroyPipelineLayout(context.device(), pipelineLayout, null);
        if (descriptorLayout != VK_NULL_HANDLE) vkDestroyDescriptorSetLayout(context.device(), descriptorLayout, null);
        pipeline = descriptorPool = linearSampler = nearestSampler = pipelineLayout = descriptorLayout = VK_NULL_HANDLE;
        previous = null;
    }
}
