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

/** GPU resources and queue ordering for the optional, separately built NVIDIA SHARC SDK kernels. */
final class VulkanSharc implements AutoCloseable {
    private static final String SHADER_ROOT = "/assets/adventurecraft/shaders/vulkan/";
    private static final String[] SHADERS = {"sharc_update.comp", "sharc_resolve.comp", "sharc_query.comp", "sharc_filter.comp"};
    private static final int PARAM_BYTES = 304;
    private static final int RESULT_BYTES = 16;
    private static final int SAMPLED_IMAGES = 8;
    private static final int COMPUTE = VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT;

    record Statistics(long frames, long resets, int width, int height, int capacity, long cacheBytes,
                      int historyFrames, long queryHits, long queryMisses, long updatePaths, long occupied) { }

    private static final class Frame {
        final long[] views = new long[4];
        VulkanBuffer parameters;
        VulkanBuffer result;
        VulkanBuffer statistics;
        long descriptor;
        Matrix4 viewProjection;
        Vector4 worldOrigin;
        Vector4 eyeOrigin;
        Vector4 anchor;
        long sceneRevision;
        long lightingRevision;
        long epoch;
        int historyFrames;
        boolean recorded;
        boolean clearCache;
        boolean fresh = true;
        boolean statisticsQueued;
    }

    private final VulkanDevice context;
    private final Frame[] frames;
    private final int capacity;
    private final int updateDownscale = integer("ac.vulkan.sharcUpdateDownscale", 5, 2, 16);
    private final int bounces = integer("ac.vulkan.sharcBounces", 3, 2, 4);
    private final int accumulationFrames = integer("ac.vulkan.sharcAccumulationFrames", 16, 1, 128);
    private final int staleFrames = integer("ac.vulkan.sharcStaleFrames", 16, 8, 1024);
    private final int querySamples = integer("ac.vulkan.sharcQuerySamples", 4, 2, 8);
    private final int filterRadius = integer("ac.vulkan.sharcFilterRadius", 2, 1, 2);
    private final int debug = integer("ac.vulkan.sharcDebug", 0, 0, 3);
    private final boolean collectCounters = debug != 0 || Boolean.getBoolean("ac.vulkan.sharcStats");
    private final float sceneScale = scalar("ac.vulkan.sharcSceneScale", 8, .25f, 64);
    private final float levelBias = scalar("ac.vulkan.sharcLevelBias", 0, -8, 8);
    private final float radianceScale = scalar("ac.vulkan.sharcRadianceScale", 1000, 1, 1_000_000);
    private final long[] pipelines = new long[SHADERS.length];
    private VulkanBuffer hash;
    private VulkanBuffer accumulation;
    private VulkanBuffer resolved;
    private VulkanBuffer queryHash;
    private VulkanBuffer queryResolved;
    private VulkanBuffer counters;
    private VulkanBuffer raw;
    private long descriptorLayout;
    private long emptyLayout;
    private long descriptorPool;
    private long pipelineLayout;
    private long sampler;
    private int width;
    private int height;
    private Frame previous;
    private Vector4 anchor;
    private Vector4 lastWorldOrigin;
    private Vector4 lastEyeOrigin;
    private long sceneRevision;
    private long lightingRevision;
    private long epoch;
    private long submittedFrames;
    private long cacheResets;
    private long hits;
    private long misses;
    private long paths;
    private long occupied;
    private boolean resetCache = true;
    private boolean rawFresh = true;
    private boolean cacheReady;
    private boolean taaActive;
    private boolean reconstructionExport;
    private boolean allocatedReconstructionExport;

    static boolean shadersAvailable() {
        for (String name : SHADERS) if (VulkanSharc.class.getResource(SHADER_ROOT + name + ".spv") == null) return false;
        return true;
    }

    static boolean supported(VulkanDevice context) {
        return context.sharcAvailable() && VulkanRestirDi.supported(context) && shadersAvailable();
    }

    static String unavailableReason(VulkanDevice context) {
        if (!context.sharcAvailable()) return context.sharcUnavailableReason();
        if (!VulkanRestirDi.supported(context)) return "sampled terrain capture and compute limits are unsupported";
        return shadersAvailable() ? null : "optional SHARC SDK shaders are absent; build with -PsharcSdkDir=<licensed SDK directory>";
    }

    VulkanSharc(VulkanDevice context, int frameCount, long raySceneLayout, int capacityExponent) {
        this.context = Objects.requireNonNull(context, "context");
        if (frameCount < 2) throw new IllegalArgumentException("SHARC screen history requires two frame slots");
        if (capacityExponent < 20 || capacityExponent > 22) throw new IllegalArgumentException("SHARC capacity exponent must be 20, 21 or 22");
        if (raySceneLayout == VK_NULL_HANDLE) throw new IllegalArgumentException("SHARC requires a ray scene layout");
        if (!supported(context)) throw new IllegalStateException(unavailableReason(context));
        capacity = 1 << capacityExponent;
        frames = new Frame[frameCount];
        for (int i = 0; i < frames.length; i++) frames[i] = new Frame();
        try {
            createBindings(raySceneLayout);
            for (int i = 0; i < pipelines.length; i++) pipelines[i] = createPipeline(SHADERS[i]);
            hash = gpuBuffer((long) capacity * 8);
            accumulation = gpuBuffer((long) capacity * 16);
            resolved = gpuBuffer((long) capacity * 16);
            // Update sees an immutable preceding cache; query sees the post-resolve copy of this same snapshot.
            queryHash = gpuBuffer((long) capacity * 8);
            queryResolved = gpuBuffer((long) capacity * 16);
            if (collectCounters) counters = gpuBuffer(16);
            for (Frame frame : frames) {
                frame.parameters = context.hostBuffer(PARAM_BYTES, VK_BUFFER_USAGE_STORAGE_BUFFER_BIT | VK_BUFFER_USAGE_SHADER_DEVICE_ADDRESS_BIT);
                if (collectCounters) frame.statistics = context.hostBuffer(16, VK_BUFFER_USAGE_TRANSFER_DST_BIT);
            }
        } catch (RuntimeException | Error error) {
            close();
            throw error;
        }
    }

    long resultAddress(int slot) {
        Frame frame = frame(slot);
        if (frame.result == null) throw new IllegalStateException("Resize SHARC screen targets before requesting an address");
        return frame.result.deviceAddress();
    }

    Statistics statistics() {
        return new Statistics(submittedFrames, cacheResets, width, height, capacity, (long) capacity * 64,
            previous == null ? 0 : previous.historyFrames, hits, misses, paths, occupied);
    }

    void invalidateHistory() { previous = null; epoch++; }

    /** Record final-color TAA state independently of the irradiance denoiser's history. */
    void setTaaActive(boolean active) { taaActive = active; }

    void setReconstructionExport(boolean active) { reconstructionExport = active; }

    /** Raw incoming diffuse lighting and the actual first-secondary path distance, before screen filtering. */
    long rawDiffuseAddress() {
        if (!allocatedReconstructionExport || raw == null)
            throw new IllegalStateException("Resize SHARC with reconstruction export enabled first");
        return raw.deviceAddress() + Math.multiplyExact(Math.multiplyExact((long) width, height), RESULT_BYTES);
    }
    long rawResultAddress() { return raw == null ? 0 : raw.deviceAddress(); }

    void invalidateCache() { resetCache = true; invalidateHistory(); }

    /** The last result can be a newer slot's history, so wait for all readers before replacing it. */
    void resize(int width, int height) {
        ensureOpen();
        if (width < 1 || height < 1) throw new IllegalArgumentException("SHARC dimensions must be positive");
        if (this.width == width && this.height == height && allocatedReconstructionExport == reconstructionExport) return;
        context.waitIdle();
        closeScreenTargets();
        invalidateHistory();
        try {
            long bytes = Math.multiplyExact(Math.multiplyExact((long) width, height), RESULT_BYTES);
            raw = gpuBuffer(Math.multiplyExact(bytes, reconstructionExport ? 2 : 1));
            allocatedReconstructionExport = reconstructionExport;
            for (Frame frame : frames) frame.result = gpuBuffer(bytes);
            this.width = width;
            this.height = height;
        } catch (RuntimeException | Error error) {
            closeScreenTargets();
            throw error;
        }
    }

    /** The caller waited for slot reuse and completed its terrain MRT capture before this call. */
    void record(VkCommandBuffer command, int slot, long raySceneSet, long[] currentViews,
                Matrix4 currentViewProjection, Vector4 worldOrigin, Vector4 eyeOrigin,
                long sceneRevision, long lightingRevision, long emitterAddress, boolean cameraCut) {
        ensureOpen();
        Frame frame = frame(slot);
        if (frame.result == null || frame.recorded) throw new IllegalStateException("SHARC needs resized, unsubmitted screen targets");
        if (currentViews == null || currentViews.length != 4) throw new IllegalArgumentException("SHARC requires four current G-buffer views");
        for (long view : currentViews) if (view == VK_NULL_HANDLE) throw new IllegalArgumentException("SHARC G-buffer view is null");
        Objects.requireNonNull(currentViewProjection, "currentViewProjection");
        Objects.requireNonNull(worldOrigin, "worldOrigin");
        Objects.requireNonNull(eyeOrigin, "eyeOrigin");
        if (raySceneSet == VK_NULL_HANDLE || emitterAddress == 0) throw new IllegalArgumentException("SHARC scene and emitter bindings are required");
        collectStatistics(frame);
        boolean reanchor = anchor == null || farFromAnchor(worldOrigin, anchor);
        boolean clear = resetCache || !cacheReady || reanchor
            || this.sceneRevision != sceneRevision || this.lightingRevision != lightingRevision;
        Vector4 targetAnchor = reanchor ? gridAnchor(worldOrigin) : anchor;
        boolean history = !clear && !cameraCut && previous != null && previous != frame && previous.epoch == epoch
            && !cameraJump(previous.viewProjection, previous.worldOrigin, currentViewProjection, worldOrigin);
        Frame source = history ? previous : frame;
        System.arraycopy(currentViews, 0, frame.views, 0, frame.views.length);
        updateDescriptors(frame, source);
        writeParameters(frame, source, targetAnchor, currentViewProjection, worldOrigin, eyeOrigin, emitterAddress, clear, history);
        try (var stack = MemoryStack.stackPush()) {
            // Shared cache/snapshot/raw buffers are serialized on the renderer's single graphics/compute queue.
            barrier(command, VK_ACCESS_SHADER_READ_BIT | VK_ACCESS_SHADER_WRITE_BIT | VK_ACCESS_TRANSFER_READ_BIT | VK_ACCESS_TRANSFER_WRITE_BIT,
                VK_ACCESS_SHADER_READ_BIT | VK_ACCESS_SHADER_WRITE_BIT | VK_ACCESS_TRANSFER_WRITE_BIT,
                COMPUTE | VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT | VK_PIPELINE_STAGE_TRANSFER_BIT,
                COMPUTE | VK_PIPELINE_STAGE_TRANSFER_BIT);
            if (clear) for (VulkanBuffer buffer : new VulkanBuffer[]{hash, accumulation, resolved, queryHash, queryResolved}) {
                vkCmdFillBuffer(command, buffer.handle(), 0, VK_WHOLE_SIZE, 0);
            }
            if (frame.fresh) vkCmdFillBuffer(command, frame.result.handle(), 0, VK_WHOLE_SIZE, 0);
            if (rawFresh) vkCmdFillBuffer(command, raw.handle(), 0, VK_WHOLE_SIZE, 0);
            if (collectCounters) vkCmdFillBuffer(command, counters.handle(), 0, VK_WHOLE_SIZE, 0);
            barrier(command, VK_ACCESS_TRANSFER_WRITE_BIT, VK_ACCESS_SHADER_READ_BIT | VK_ACCESS_SHADER_WRITE_BIT,
                VK_PIPELINE_STAGE_TRANSFER_BIT, COMPUTE);
            vkCmdBindDescriptorSets(command, VK_PIPELINE_BIND_POINT_COMPUTE, pipelineLayout, 0, stack.longs(frame.descriptor), null);
            vkCmdBindDescriptorSets(command, VK_PIPELINE_BIND_POINT_COMPUTE, pipelineLayout, VulkanBindings.RAY_SCENE, stack.longs(raySceneSet), null);
            vkCmdPushConstants(command, pipelineLayout, VK_SHADER_STAGE_COMPUTE_BIT, 0, stack.longs(frame.parameters.deviceAddress()));
            dispatch(command, pipelines[0], divide(width, updateDownscale), divide(height, updateDownscale));
            computeBarrier(command);
            vkCmdBindPipeline(command, VK_PIPELINE_BIND_POINT_COMPUTE, pipelines[1]);
            vkCmdDispatch(command, divide(capacity, 128), 1, 1);
            // Both the update's old snapshot reads and resolve writes must finish before refreshing snapshots.
            barrier(command, VK_ACCESS_SHADER_READ_BIT | VK_ACCESS_SHADER_WRITE_BIT,
                VK_ACCESS_TRANSFER_READ_BIT | VK_ACCESS_TRANSFER_WRITE_BIT, COMPUTE, VK_PIPELINE_STAGE_TRANSFER_BIT);
            copy(command, hash, queryHash, stack);
            copy(command, resolved, queryResolved, stack);
            barrier(command, VK_ACCESS_TRANSFER_WRITE_BIT, VK_ACCESS_SHADER_READ_BIT, VK_PIPELINE_STAGE_TRANSFER_BIT, COMPUTE);
            dispatch(command, pipelines[2], width, height);
            computeBarrier(command);
            dispatch(command, pipelines[3], width, height);
            barrier(command, VK_ACCESS_SHADER_WRITE_BIT,
                VK_ACCESS_SHADER_READ_BIT | VK_ACCESS_TRANSFER_READ_BIT, COMPUTE,
                COMPUTE | VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT | VK_PIPELINE_STAGE_TRANSFER_BIT);
            if (collectCounters) {
                copy(command, counters, frame.statistics, stack);
                barrier(command, VK_ACCESS_TRANSFER_WRITE_BIT, VK_ACCESS_HOST_READ_BIT,
                    VK_PIPELINE_STAGE_TRANSFER_BIT, VK_PIPELINE_STAGE_HOST_BIT);
            }
        }
        frame.viewProjection = currentViewProjection;
        frame.worldOrigin = worldOrigin;
        frame.eyeOrigin = eyeOrigin;
        frame.anchor = targetAnchor;
        frame.sceneRevision = sceneRevision;
        frame.lightingRevision = lightingRevision;
        frame.clearCache = clear;
        frame.epoch = epoch;
        frame.historyFrames = history ? Math.min(source.historyFrames + 1, 65535) : 1;
        frame.recorded = true;
    }

    void submitted(int slot) {
        Frame frame = frame(slot);
        if (!frame.recorded) throw new IllegalStateException("No SHARC work was recorded for this slot");
        frame.recorded = false;
        frame.fresh = false;
        frame.statisticsQueued = collectCounters;
        rawFresh = false;
        cacheReady = true;
        anchor = frame.anchor;
        lastWorldOrigin = frame.worldOrigin;
        lastEyeOrigin = frame.eyeOrigin;
        sceneRevision = frame.sceneRevision;
        lightingRevision = frame.lightingRevision;
        if (frame.clearCache) cacheResets++;
        if (frame.epoch == epoch) { previous = frame; resetCache = false; }
        submittedFrames++;
    }

    void abort(int slot) { frame(slot).recorded = false; invalidateCache(); }

    private void writeParameters(Frame frame, Frame source, Vector4 targetAnchor, Matrix4 viewProjection,
                                 Vector4 worldOrigin, Vector4 eyeOrigin, long emitterAddress, boolean clear, boolean history) {
        frame.parameters.write(bytes -> {
            bytes.putLong(hash.deviceAddress()).putLong(accumulation.deviceAddress()).putLong(resolved.deviceAddress())
                .putLong(queryHash.deviceAddress()).putLong(queryResolved.deviceAddress()).putLong(emitterAddress)
                .putLong(raw.deviceAddress()).putLong(frame.result.deviceAddress())
                .putLong(source.result.deviceAddress()).putLong(collectCounters ? counters.deviceAddress() : 0);
            vector(bytes, worldOrigin.x() - targetAnchor.x(), worldOrigin.y() - targetAnchor.y(), worldOrigin.z() - targetAnchor.z());
            vector(bytes, worldOrigin.x() - targetAnchor.x() + eyeOrigin.x(), worldOrigin.y() - targetAnchor.y() + eyeOrigin.y(),
                worldOrigin.z() - targetAnchor.z() + eyeOrigin.z());
            Vector4 previousWorld = !clear && lastWorldOrigin != null ? lastWorldOrigin : worldOrigin;
            Vector4 previousEye = !clear && lastEyeOrigin != null ? lastEyeOrigin : eyeOrigin;
            vector(bytes, previousWorld.x() - targetAnchor.x() + previousEye.x(), previousWorld.y() - targetAnchor.y() + previousEye.y(),
                previousWorld.z() - targetAnchor.z() + previousEye.z());
            for (float value : (history ? source.viewProjection : viewProjection).copyArray()) bytes.putFloat(value);
            Vector4 origin = history ? source.worldOrigin : worldOrigin;
            vector(bytes, worldOrigin.x() - origin.x(), worldOrigin.y() - origin.y(), worldOrigin.z() - origin.z());
            vector(bytes, eyeOrigin.x(), eyeOrigin.y(), eyeOrigin.z());
            bytes.putInt(width).putInt(height).putInt(capacity).putInt((int) (submittedFrames & 0x7fffffff));
            bytes.putFloat(sceneScale).putFloat(levelBias).putFloat(radianceScale).putFloat(1);
            bytes.putInt(updateDownscale).putInt(bounces).putInt(accumulationFrames).putInt(staleFrames);
            bytes.putInt((history ? 1 : 0) | 2 | (taaActive ? 4 : 0)).putInt(0).putInt(capacity).putInt(debug);
            bytes.putInt(querySamples).putInt(filterRadius).putInt(allocatedReconstructionExport ? 1 : 0).putInt(0);
        });
    }

    private void collectStatistics(Frame frame) {
        if (!frame.statisticsQueued) return;
        ByteBuffer bytes = frame.statistics.mapped();
        hits = Integer.toUnsignedLong(bytes.getInt(0));
        misses = Integer.toUnsignedLong(bytes.getInt(4));
        paths = Integer.toUnsignedLong(bytes.getInt(8));
        occupied = Integer.toUnsignedLong(bytes.getInt(12));
        frame.statisticsQueued = false;
    }

    private void updateDescriptors(Frame frame, Frame source) {
        try (var stack = MemoryStack.stackPush()) {
            var images = VkDescriptorImageInfo.calloc(SAMPLED_IMAGES, stack);
            for (int i = 0; i < SAMPLED_IMAGES; i++) images.get(i).sampler(sampler)
                .imageView((i < 4 ? frame : source).views[i % 4]).imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
            var write = VkWriteDescriptorSet.calloc(1, stack).sType$Default().dstSet(frame.descriptor).dstBinding(0)
                .descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER).descriptorCount(SAMPLED_IMAGES).pImageInfo(images);
            vkUpdateDescriptorSets(context.device(), write, null);
        }
    }

    private VulkanBuffer gpuBuffer(long bytes) {
        return context.buffer(bytes, VK_BUFFER_USAGE_STORAGE_BUFFER_BIT | VK_BUFFER_USAGE_SHADER_DEVICE_ADDRESS_BIT
            | VK_BUFFER_USAGE_TRANSFER_SRC_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
    }

    private static void vector(ByteBuffer bytes, float x, float y, float z) { bytes.putFloat(x).putFloat(y).putFloat(z).putFloat(0); }

    private static void copy(VkCommandBuffer command, VulkanBuffer source, VulkanBuffer target, MemoryStack stack) {
        vkCmdCopyBuffer(command, source.handle(), target.handle(), VkBufferCopy.calloc(1, stack).size(source.size()));
    }

    private static int divide(int value, int divisor) { return (value + divisor - 1) / divisor; }

    private static void dispatch(VkCommandBuffer command, long pipeline, int width, int height) {
        vkCmdBindPipeline(command, VK_PIPELINE_BIND_POINT_COMPUTE, pipeline);
        vkCmdDispatch(command, divide(width, 8), divide(height, 8), 1);
    }

    private static void computeBarrier(VkCommandBuffer command) {
        barrier(command, VK_ACCESS_SHADER_WRITE_BIT, VK_ACCESS_SHADER_READ_BIT | VK_ACCESS_SHADER_WRITE_BIT, COMPUTE, COMPUTE);
    }

    private static void barrier(VkCommandBuffer command, int source, int target, int sourceStage, int targetStage) {
        try (var stack = MemoryStack.stackPush()) {
            var memory = VkMemoryBarrier.calloc(1, stack).sType$Default().srcAccessMask(source).dstAccessMask(target);
            vkCmdPipelineBarrier(command, sourceStage, targetStage, 0, memory, null, null);
        }
    }

    private void createBindings(long raySceneLayout) {
        try (var stack = MemoryStack.stackPush()) {
            var handle = stack.mallocLong(1);
            var bindings = VkDescriptorSetLayoutBinding.calloc(1, stack).binding(0)
                .descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER).descriptorCount(SAMPLED_IMAGES).stageFlags(VK_SHADER_STAGE_COMPUTE_BIT);
            var layout = VkDescriptorSetLayoutCreateInfo.calloc(stack).sType$Default().pBindings(bindings);
            check(vkCreateDescriptorSetLayout(context.device(), layout, null, handle), "create SHARC G-buffer layout");
            descriptorLayout = handle.get(0);
            layout.pBindings(null);
            check(vkCreateDescriptorSetLayout(context.device(), layout, null, handle), "create SHARC empty layout");
            emptyLayout = handle.get(0);
            var range = VkPushConstantRange.calloc(1, stack).stageFlags(VK_SHADER_STAGE_COMPUTE_BIT).size(Long.BYTES);
            var info = VkPipelineLayoutCreateInfo.calloc(stack).sType$Default()
                .pSetLayouts(stack.longs(descriptorLayout, emptyLayout, emptyLayout, emptyLayout, raySceneLayout)).pPushConstantRanges(range);
            check(vkCreatePipelineLayout(context.device(), info, null, handle), "create SHARC compute pipeline layout");
            pipelineLayout = handle.get(0);
            var samplerInfo = VkSamplerCreateInfo.calloc(stack).sType$Default().magFilter(VK_FILTER_NEAREST).minFilter(VK_FILTER_NEAREST)
                .mipmapMode(VK_SAMPLER_MIPMAP_MODE_NEAREST).addressModeU(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE)
                .addressModeV(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE).addressModeW(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE).maxAnisotropy(1);
            check(vkCreateSampler(context.device(), samplerInfo, null, handle), "create SHARC G-buffer sampler");
            sampler = handle.get(0);
            var sizes = VkDescriptorPoolSize.calloc(1, stack).type(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
                .descriptorCount(frames.length * SAMPLED_IMAGES);
            var pool = VkDescriptorPoolCreateInfo.calloc(stack).sType$Default().maxSets(frames.length).pPoolSizes(sizes);
            check(vkCreateDescriptorPool(context.device(), pool, null, handle), "create SHARC descriptor pool");
            descriptorPool = handle.get(0);
            var layouts = stack.mallocLong(frames.length);
            for (int i = 0; i < frames.length; i++) layouts.put(i, descriptorLayout);
            var allocation = VkDescriptorSetAllocateInfo.calloc(stack).sType$Default().descriptorPool(descriptorPool).pSetLayouts(layouts);
            var sets = stack.mallocLong(frames.length);
            check(vkAllocateDescriptorSets(context.device(), allocation, sets), "allocate SHARC frame descriptors");
            for (int i = 0; i < frames.length; i++) frames[i].descriptor = sets.get(i);
        }
    }

    private long createPipeline(String name) {
        String path = SHADER_ROOT + name + ".spv";
        try (InputStream stream = VulkanSharc.class.getResourceAsStream(path)) {
            if (stream == null) throw new IllegalStateException("Missing optional SHARC SDK shader " + path);
            byte[] bytes = stream.readAllBytes();
            if (bytes.length == 0 || bytes.length % 4 != 0) throw new IllegalStateException("Invalid SHARC shader " + path);
            ByteBuffer code = MemoryUtil.memAlloc(bytes.length).order(ByteOrder.nativeOrder());
            try (var stack = MemoryStack.stackPush()) {
                code.put(bytes).flip();
                var moduleInfo = VkShaderModuleCreateInfo.calloc(stack).sType$Default().pCode(code);
                var handle = stack.mallocLong(1);
                check(vkCreateShaderModule(context.device(), moduleInfo, null, handle), "create SHARC shader " + name);
                long module = handle.get(0);
                try {
                    var info = VkComputePipelineCreateInfo.calloc(1, stack).sType$Default().layout(pipelineLayout);
                    info.stage().sType$Default().stage(VK_SHADER_STAGE_COMPUTE_BIT).module(module).pName(stack.UTF8("main"));
                    check(vkCreateComputePipelines(context.device(), VK_NULL_HANDLE, info, null, handle), "create SHARC pipeline " + name);
                    return handle.get(0);
                } finally { vkDestroyShaderModule(context.device(), module, null); }
            } finally { MemoryUtil.memFree(code); }
        } catch (IOException error) { throw new IllegalStateException("Cannot read optional SHARC SDK shader " + path, error); }
    }

    static Vector4 gridAnchor(Vector4 origin) {
        return new Vector4((float) (Math.rint(origin.x() / 1024.0) * 1024),
            (float) (Math.rint(origin.y() / 1024.0) * 1024), (float) (Math.rint(origin.z() / 1024.0) * 1024), 0);
    }

    static boolean farFromAnchor(Vector4 origin, Vector4 anchor) {
        return Math.abs(origin.x() - anchor.x()) > 512 || Math.abs(origin.y() - anchor.y()) > 512 || Math.abs(origin.z() - anchor.z()) > 512;
    }

    static boolean cameraJump(Matrix4 previousView, Vector4 previousOrigin, Matrix4 view, Vector4 origin) {
        double x = (double) origin.x() - previousOrigin.x(), y = (double) origin.y() - previousOrigin.y(),
            z = (double) origin.z() - previousOrigin.z();
        if (!Double.isFinite(x + y + z) || x * x + y * y + z * z > 64) return true;
        for (int i = 0; i < 16; i++) if (!Float.isFinite(view.get(i)) || !Float.isFinite(previousView.get(i))
            || Math.abs(view.get(i) - previousView.get(i)) > .75f) return true;
        return false;
    }

    private static int integer(String name, int fallback, int min, int max) {
        try { return Math.clamp(Integer.parseInt(System.getProperty(name, Integer.toString(fallback))), min, max); }
        catch (NumberFormatException ignored) { return fallback; }
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

    private void ensureOpen() { if (pipelineLayout == VK_NULL_HANDLE) throw new IllegalStateException("SHARC resources are closed"); }

    private void closeScreenTargets() {
        if (raw != null) raw.close();
        raw = null;
        rawFresh = true;
        for (Frame frame : frames) {
            if (frame.result != null) frame.result.close();
            frame.result = null;
            frame.recorded = false;
            frame.fresh = true;
            frame.historyFrames = 0;
        }
        width = height = 0;
    }

    /** Renderer submissions must finish before closing this global cache and its screen history. */
    @Override public void close() {
        closeScreenTargets();
        for (Frame frame : frames) {
            if (frame.parameters != null) frame.parameters.close();
            if (frame.statistics != null) frame.statistics.close();
            frame.parameters = frame.statistics = null;
        }
        for (VulkanBuffer buffer : new VulkanBuffer[]{hash, accumulation, resolved, queryHash, queryResolved, counters}) if (buffer != null) buffer.close();
        hash = accumulation = resolved = queryHash = queryResolved = counters = null;
        for (int i = 0; i < pipelines.length; i++) {
            if (pipelines[i] != VK_NULL_HANDLE) vkDestroyPipeline(context.device(), pipelines[i], null);
            pipelines[i] = VK_NULL_HANDLE;
        }
        if (descriptorPool != VK_NULL_HANDLE) vkDestroyDescriptorPool(context.device(), descriptorPool, null);
        if (sampler != VK_NULL_HANDLE) vkDestroySampler(context.device(), sampler, null);
        if (pipelineLayout != VK_NULL_HANDLE) vkDestroyPipelineLayout(context.device(), pipelineLayout, null);
        if (descriptorLayout != VK_NULL_HANDLE) vkDestroyDescriptorSetLayout(context.device(), descriptorLayout, null);
        if (emptyLayout != VK_NULL_HANDLE) vkDestroyDescriptorSetLayout(context.device(), emptyLayout, null);
        descriptorPool = sampler = pipelineLayout = descriptorLayout = emptyLayout = VK_NULL_HANDLE;
        previous = null;
    }
}
