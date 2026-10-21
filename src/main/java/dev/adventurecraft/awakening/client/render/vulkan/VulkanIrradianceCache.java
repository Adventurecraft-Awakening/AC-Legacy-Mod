package dev.adventurecraft.awakening.client.render.vulkan;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static dev.adventurecraft.awakening.client.render.vulkan.VulkanDevice.check;
import static org.lwjgl.vulkan.VK10.*;

/** Budgeted, world-space diffuse lighting updates; submission owns all scheduling advances. */
final class VulkanIrradianceCache implements AutoCloseable {
    static final int PUSH_BYTES = 32;
    static final int PUSH_STAGES = VK_SHADER_STAGE_VERTEX_BIT | VK_SHADER_STAGE_FRAGMENT_BIT | VK_SHADER_STAGE_COMPUTE_BIT;
    static final int VERTEX_BYTES = 32; // vec4(front RGB, samples), vec4(back RGB, samples)
    private static final int WORKGROUP_SIZE = 64;
    private static final int VERTEX_BUDGET = 32768;
    private static final int SAMPLES_PER_UPDATE = 16;
    private static final int TARGET_SAMPLES = 128;
    private static final int ADAPTATION_SAMPLES = 512;
    private static final int SEQUENCE_LIMIT = 1 << 30;
    private static final int REFRESH_FRAMES = 120;
    private static final int NEAR_MESHES = 64;
    private static final int VISIBLE_INSTANCE = 4;
    private static final int READ_STAGES = VK_PIPELINE_STAGE_VERTEX_SHADER_BIT | VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT;

    record Statistics(long jobs, long vertexUpdates, long submittedFrames, int cachedMeshes,
                      int visibleMeshes, int pendingVisibleMeshes, int minimumVisibleSamples) { }
    private record Environment(long materialRevision, long lighting) { }
    private record Advance(VulkanRayScene.Geometry geometry, Environment environment, long geometryRevision,
                           int sampleOffset, int samples, int epochSamples, int targetSamples) { }
    private record Selection(int nextCursor, int vertices) { }
    private record Reservation(VulkanRayScene.Geometry geometry, int nextCursor) { }

    private static final class State {
        Environment environment;
        long geometryRevision;
        int sequence;
        int samples;
        int targetSamples = TARGET_SAMPLES;
        long completedFrame;
    }

    private static final class Frame {
        final List<Advance> advances = new ArrayList<>();
        final Set<VulkanRayScene.Geometry> scheduled = Collections.newSetFromMap(new IdentityHashMap<>());
        List<VulkanRayScene.Geometry> visible = List.of();
        Environment environment;
        int visibleCursor;
        int hiddenCursor;
        boolean recorded;
    }

    private final VulkanDevice context;
    private final long pipelineLayout;
    private final int maxVerticesPerDispatch;
    private final Frame[] frames = {new Frame(), new Frame()};
    private final Map<VulkanRayScene.Geometry, State> states = new IdentityHashMap<>();
    private long pipeline;
    private int visibleCursor;
    private int hiddenCursor;
    private long submittedFrames;
    private long submittedJobs;
    private long submittedVertices;
    private List<VulkanRayScene.Geometry> lastVisible = List.of();
    private Environment lastEnvironment;

    VulkanIrradianceCache(VulkanDevice context, long pipelineLayout) {
        this.context = context;
        this.pipelineLayout = pipelineLayout;
        try (var stack = MemoryStack.stackPush()) {
            var properties = VkPhysicalDeviceProperties.malloc(stack);
            vkGetPhysicalDeviceProperties(context.physicalDevice(), properties);
            this.maxVerticesPerDispatch = (int) Math.min(Integer.MAX_VALUE,
                Integer.toUnsignedLong(properties.limits().maxComputeWorkGroupCount(0)) * WORKGROUP_SIZE);
            long shader = loadShader(context);
            try {
                var info = VkComputePipelineCreateInfo.calloc(1, stack).sType$Default().layout(pipelineLayout);
                info.stage().sType$Default().stage(VK_SHADER_STAGE_COMPUTE_BIT).module(shader).pName(stack.UTF8("main"));
                var handle = stack.mallocLong(1);
                check(vkCreateComputePipelines(context.device(), VK_NULL_HANDLE, info, null, handle),
                    "create terrain irradiance compute pipeline");
                this.pipeline = handle.get(0);
            } finally {
                vkDestroyShaderModule(context.device(), shader, null);
            }
        }
    }

    int maxVerticesPerDispatch() { return this.maxVerticesPerDispatch; }

    /** A replacement continues the sample sequence of copied GPU estimates, never sixteen-sample black history. */
    void inherit(VulkanRayScene.Geometry geometry, int sequence) {
        if (sequence <= 0) return;
        State state = this.states.computeIfAbsent(geometry, ignored -> new State());
        state.sequence = sequence;
        state.samples = 0;
        state.targetSamples = ADAPTATION_SAMPLES;
    }

    int sequence(VulkanRayScene.Geometry geometry) {
        State state = this.states.get(geometry);
        return state == null ? 0 : state.sequence;
    }

    void record(VkCommandBuffer command, int slot, List<VulkanRayScene.Instance> instances,
                VulkanBuffer lighting, long descriptorSet, long materialRevision) {
        Frame frame = frame(slot);
        if (frame.recorded) throw new IllegalStateException("Irradiance recording must be submitted or aborted first");
        frame.advances.clear();
        frame.scheduled.clear();
        // Descriptor/image identities change during animated atlas uploads and copy-on-write.
        // Explicit resource reload revisions carry material changes; GPU handles are not epochs.
        var environment = new Environment(materialRevision, lightingHash(lighting.mapped()));
        var visible = new ArrayList<VulkanRayScene.Geometry>();
        var hidden = new ArrayList<VulkanRayScene.Geometry>();
        var priorities = new IdentityHashMap<VulkanRayScene.Geometry, Boolean>();
        var distances = new IdentityHashMap<VulkanRayScene.Geometry, Double>();
        for (var instance : instances) {
            priorities.merge(instance.geometry(), (instance.flags() & VISIBLE_INSTANCE) != 0, (a, b) -> a || b);
            distances.merge(instance.geometry(), instance.geometry().cameraDistanceSquared(instance.model()), Math::min);
        }
        var seen = new IdentityHashMap<VulkanRayScene.Geometry, Boolean>();
        for (var instance : instances) {
            var geometry = instance.geometry();
            if (seen.put(geometry, Boolean.TRUE) != null) continue;
            this.states.computeIfAbsent(geometry, ignored -> new State());
            (priorities.get(geometry) ? visible : hidden).add(geometry);
        }
        frame.visible = List.copyOf(visible);
        frame.environment = environment;

        schedule(frame, visible, hidden, distances, environment);

        try (var stack = MemoryStack.stackPush()) {
            if (!frame.advances.isEmpty()) {
                // A later frame can update a cache read by an earlier frame on this same queue.
                barrier(command, stack, VK_ACCESS_SHADER_READ_BIT | VK_ACCESS_SHADER_WRITE_BIT,
                    VK_ACCESS_SHADER_READ_BIT | VK_ACCESS_SHADER_WRITE_BIT,
                    READ_STAGES | VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT, VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT);
                vkCmdBindPipeline(command, VK_PIPELINE_BIND_POINT_COMPUTE, this.pipeline);
                vkCmdBindDescriptorSets(command, VK_PIPELINE_BIND_POINT_COMPUTE, this.pipelineLayout,
                    VulkanBindings.RAY_SCENE, stack.longs(descriptorSet), null);
                ByteBuffer push = stack.malloc(PUSH_BYTES).order(ByteOrder.nativeOrder());
                for (Advance advance : frame.advances) {
                    var geometry = advance.geometry;
                    push.clear().putLong(geometry.irradianceAddress()).putLong(geometry.vertexAddress())
                        .putInt(0).putInt(advance.sampleOffset).putInt(advance.samples).putInt(geometry.originalVertexCount()).flip();
                    vkCmdPushConstants(command, this.pipelineLayout, PUSH_STAGES, 0, push);
                    vkCmdDispatch(command, Math.toIntExact((geometry.originalVertexCount() + 63L) / WORKGROUP_SIZE), 1, 1);
                }
                barrier(command, stack, VK_ACCESS_SHADER_WRITE_BIT, VK_ACCESS_SHADER_READ_BIT,
                    VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT, READ_STAGES);
            }
            frame.recorded = true;
        } catch (RuntimeException | Error error) {
            abort(slot);
            throw error;
        }
    }

    private void schedule(Frame frame, List<VulkanRayScene.Geometry> visible,
                          List<VulkanRayScene.Geometry> hidden, Map<VulkanRayScene.Geometry, Double> distances,
                          Environment environment) {
        // Keep the nearest unfinished meshes in the budget until they converge. Dedicated fair
        // turns still give far-visible and hidden initial work progress while the camera moves.
        boolean reserveHidden = this.submittedFrames % 4 == 3;
        boolean reserveAdaptation = this.submittedFrames % 4 == 1;
        Reservation reserve = reserveHidden ? firstPending(hidden, this.hiddenCursor, environment)
            : reserveAdaptation ? firstAdapting(visible, this.visibleCursor, environment)
            : this.submittedFrames % 8 == 6 ? firstPending(visible, this.visibleCursor, environment) : null;
        int reservedVertices = reserve == null ? 0 : reserve.geometry.originalVertexCount();
        var nearest = new ArrayList<>(visible);
        nearest.removeIf(geometry -> initialSamples(geometry, environment) >= TARGET_SAMPLES ||
            reserve != null && geometry == reserve.geometry);
        nearest.sort(Comparator.comparingDouble(distances::get));
        var nearWindow = nearest.subList(0, Math.min(NEAR_MESHES, nearest.size()));
        int vertices = select(frame, nearWindow, 0, Math.max(0, VERTEX_BUDGET - reservedVertices),
            reserve == null, environment, true).vertices;
        frame.visibleCursor = this.visibleCursor;
        frame.hiddenCursor = this.hiddenCursor;
        if (reserve != null) {
            vertices += select(frame, List.of(reserve.geometry), 0, Math.max(0, VERTEX_BUDGET - vertices),
                true, environment, !reserveAdaptation).vertices;
            if (reserveHidden) frame.hiddenCursor = reserve.nextCursor;
            else frame.visibleCursor = reserve.nextCursor;
        }
        vertices += select(frame, nearest, 0, Math.max(0, VERTEX_BUDGET - vertices),
            true, environment, true).vertices;
        Selection hiddenInitial = select(frame, hidden, frame.hiddenCursor, Math.max(0, VERTEX_BUDGET - vertices),
            true, environment, true);
        vertices += hiddenInitial.vertices;
        frame.hiddenCursor = hiddenInitial.nextCursor;

        boolean initialPending = visible.stream().anyMatch(geometry -> initialSamples(geometry, environment) < TARGET_SAMPLES)
            || hidden.stream().anyMatch(geometry -> initialSamples(geometry, environment) < TARGET_SAMPLES);
        if (initialPending) return;

        var adaptingVisible = new ArrayList<>(visible);
        adaptingVisible.removeIf(geometry -> !isAdapting(geometry, environment));
        adaptingVisible.sort(Comparator.comparingDouble(distances::get));
        vertices += select(frame, adaptingVisible, 0, Math.max(0, VERTEX_BUDGET - vertices),
            true, environment, false).vertices;
        var adaptingHidden = hidden.stream().filter(geometry -> isAdapting(geometry, environment)).toList();
        Selection hiddenAdaptation = select(frame, adaptingHidden, frame.hiddenCursor, Math.max(0, VERTEX_BUDGET - vertices),
            true, environment, false);
        vertices += hiddenAdaptation.vertices;
        if (!adaptingHidden.isEmpty()) frame.hiddenCursor = hiddenAdaptation.nextCursor;
        if (!adaptingVisible.isEmpty() || !adaptingHidden.isEmpty()) return;

        // Periodic refresh never displaces initial accumulation or adaptation to changed light.
        Selection visibleRefresh = select(frame, visible, frame.visibleCursor, Math.max(0, VERTEX_BUDGET - vertices),
            true, environment, false);
        vertices += visibleRefresh.vertices;
        frame.visibleCursor = visibleRefresh.nextCursor;
        frame.hiddenCursor = select(frame, hidden, frame.hiddenCursor, Math.max(0, VERTEX_BUDGET - vertices),
            true, environment, false).nextCursor;
    }

    private Selection select(Frame frame, List<VulkanRayScene.Geometry> candidates, int cursor,
                             int budget, boolean allowOversize, Environment environment, boolean initial) {
        int vertices = 0;
        int nextCursor = cursor;
        for (int step = 0; step < candidates.size() && budget > vertices; step++) {
            int index = (int) ((Integer.toUnsignedLong(cursor) + step) % candidates.size());
            var geometry = candidates.get(index);
            if (frame.scheduled.contains(geometry)) continue;
            State state = this.states.get(geometry);
            int offset = sampleOffset(state, environment, geometry);
            int epochSamples = initialSamples(geometry, environment);
            if (offset < 0 || initial != (epochSamples < TARGET_SAMPLES)) continue;
            int count = geometry.originalVertexCount();
            if (count > budget - vertices && !(allowOversize && frame.advances.isEmpty())) continue;
            int target = validEpoch(state, environment, geometry) ? state.targetSamples
                : state.sequence == 0 ? TARGET_SAMPLES : ADAPTATION_SAMPLES;
            int accumulated = epochSamples(geometry, environment);
            frame.advances.add(new Advance(geometry, environment, geometry.irradianceRevision(), offset,
                SAMPLES_PER_UPDATE, Math.min(target, accumulated + SAMPLES_PER_UPDATE), target));
            frame.scheduled.add(geometry);
            vertices = Math.addExact(vertices, count);
            nextCursor = (index + 1) % candidates.size();
        }
        return new Selection(nextCursor, vertices);
    }

    private Reservation firstPending(List<VulkanRayScene.Geometry> candidates, int cursor, Environment environment) {
        for (int step = 0; step < candidates.size(); step++) {
            int index = (int) ((Integer.toUnsignedLong(cursor) + step) % candidates.size());
            var geometry = candidates.get(index);
            if (initialSamples(geometry, environment) < TARGET_SAMPLES) return new Reservation(geometry, (index + 1) % candidates.size());
        }
        return null;
    }

    private int initialSamples(VulkanRayScene.Geometry geometry, Environment environment) {
        return Math.min(TARGET_SAMPLES, epochSamples(geometry, environment));
    }

    private int epochSamples(VulkanRayScene.Geometry geometry, Environment environment) {
        State state = this.states.get(geometry);
        return validEpoch(state, environment, geometry) ? state.samples : 0;
    }

    private static boolean validEpoch(State state, Environment environment, VulkanRayScene.Geometry geometry) {
        return state != null && environment.equals(state.environment) && state.geometryRevision == geometry.irradianceRevision();
    }

    private boolean isAdapting(VulkanRayScene.Geometry geometry, Environment environment) {
        State state = this.states.get(geometry);
        return validEpoch(state, environment, geometry) && state.samples >= TARGET_SAMPLES && state.samples < state.targetSamples;
    }

    private Reservation firstAdapting(List<VulkanRayScene.Geometry> candidates, int cursor, Environment environment) {
        for (int step = 0; step < candidates.size(); step++) {
            int index = (int) ((Integer.toUnsignedLong(cursor) + step) % candidates.size());
            if (isAdapting(candidates.get(index), environment)) return new Reservation(candidates.get(index), (index + 1) % candidates.size());
        }
        return null;
    }

    private int sampleOffset(State state, Environment environment, VulkanRayScene.Geometry geometry) {
        // Refresh the estimate rather than replacing a converged cache with sixteen noisy samples.
        // Epoch confidence is separate from the continuing low-discrepancy sample sequence.
        if (!environment.equals(state.environment) || state.geometryRevision != geometry.irradianceRevision()
            || state.samples < state.targetSamples || this.submittedFrames - state.completedFrame >= REFRESH_FRAMES) return state.sequence;
        return -1;
    }

    void submitted(int slot) {
        Frame frame = frame(slot);
        if (!frame.recorded) throw new IllegalStateException("No irradiance recording to submit");
        for (Advance advance : frame.advances) {
            State state = this.states.get(advance.geometry);
            if (state == null) continue;
            state.environment = advance.environment;
            state.geometryRevision = advance.geometryRevision;
            state.sequence = advance.sampleOffset + advance.samples;
            if (state.sequence >= SEQUENCE_LIMIT) {
                // Bound the Java counter without losing the established GPU history weight.
                state.sequence = TARGET_SAMPLES + (state.sequence - TARGET_SAMPLES) % (SEQUENCE_LIMIT - TARGET_SAMPLES);
            }
            state.samples = advance.epochSamples;
            state.targetSamples = advance.targetSamples;
            if (state.samples >= state.targetSamples) state.completedFrame = this.submittedFrames + 1;
            this.submittedVertices += advance.geometry.originalVertexCount();
        }
        this.submittedJobs += frame.advances.size();
        this.submittedFrames++;
        this.visibleCursor = frame.visibleCursor;
        this.hiddenCursor = frame.hiddenCursor;
        this.lastVisible = frame.visible;
        this.lastEnvironment = frame.environment;
        frame.advances.clear();
        frame.scheduled.clear();
        frame.visible = List.of();
        frame.environment = null;
        frame.recorded = false;
    }

    void abort(int slot) {
        Frame frame = frame(slot);
        frame.advances.clear();
        frame.scheduled.clear();
        frame.visible = List.of();
        frame.environment = null;
        frame.recorded = false;
    }

    void forget(VulkanRayScene.Geometry geometry) { this.states.remove(geometry); }

    Statistics statistics() {
        int pending = 0;
        int minimum = TARGET_SAMPLES;
        for (var geometry : this.lastVisible) {
            int samples = this.lastEnvironment == null ? 0 : initialSamples(geometry, this.lastEnvironment);
            if (samples < TARGET_SAMPLES) pending++;
            minimum = Math.min(minimum, samples);
        }
        return new Statistics(this.submittedJobs, this.submittedVertices, this.submittedFrames, this.states.size(),
            this.lastVisible.size(), pending, minimum);
    }

    private Frame frame(int slot) {
        if (slot < 0 || slot >= this.frames.length) throw new IllegalArgumentException("Invalid irradiance frame slot");
        return this.frames[slot];
    }

    /** The first three vec4s affect cached bounce; camera origin and live point lights do not. */
    private static long lightingHash(ByteBuffer lighting) {
        long hash = 0xcbf29ce484222325L;
        for (int offset = 0; offset < 48; offset += Float.BYTES) {
            if (offset == 28) continue; // Display brightness changes live shading, not cached transport.
            float value = lighting.getFloat(offset);
            int quantized = Float.isFinite(value) ? Math.round(value * 64) : Float.floatToIntBits(value);
            hash = (hash ^ quantized) * 0x100000001b3L;
        }
        return hash;
    }

    private static void barrier(VkCommandBuffer command, MemoryStack stack, int sourceAccess, int targetAccess,
                                int sourceStage, int targetStage) {
        var barrier = VkMemoryBarrier.calloc(1, stack).sType$Default().srcAccessMask(sourceAccess).dstAccessMask(targetAccess);
        vkCmdPipelineBarrier(command, sourceStage, targetStage, 0, barrier, null, null);
    }

    private static long loadShader(VulkanDevice context) {
        String path = "/assets/adventurecraft/shaders/vulkan/irradiance.comp.spv";
        try (var stream = VulkanIrradianceCache.class.getResourceAsStream(path)) {
            if (stream == null) throw new IllegalStateException("Missing compiled Vulkan shader " + path);
            byte[] bytes = stream.readAllBytes();
            if (bytes.length == 0 || bytes.length % 4 != 0) throw new IllegalStateException("Invalid SPIR-V shader " + path);
            ByteBuffer code = MemoryUtil.memAlloc(bytes.length).order(ByteOrder.nativeOrder());
            try (var stack = MemoryStack.stackPush()) {
                code.put(bytes).flip();
                var info = VkShaderModuleCreateInfo.calloc(stack).sType$Default().pCode(code);
                var handle = stack.mallocLong(1);
                check(vkCreateShaderModule(context.device(), info, null, handle), "create terrain irradiance compute shader");
                return handle.get(0);
            } finally {
                MemoryUtil.memFree(code);
            }
        } catch (IOException error) {
            throw new IllegalStateException("Cannot read shader " + path, error);
        }
    }

    @Override public void close() {
        if (this.pipeline != VK_NULL_HANDLE) vkDestroyPipeline(this.context.device(), this.pipeline, null);
        this.pipeline = VK_NULL_HANDLE;
        this.states.clear();
        this.lastVisible = List.of();
        this.lastEnvironment = null;
        for (Frame frame : this.frames) {
            frame.advances.clear();
            frame.scheduled.clear();
            frame.visible = List.of();
            frame.environment = null;
        }
    }
}
