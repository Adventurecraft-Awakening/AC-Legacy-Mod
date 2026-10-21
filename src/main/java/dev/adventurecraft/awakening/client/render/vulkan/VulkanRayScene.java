package dev.adventurecraft.awakening.client.render.vulkan;

import dev.adventurecraft.awakening.client.render.VertexFormat;
import dev.adventurecraft.awakening.client.render.state.Matrix4;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import static dev.adventurecraft.awakening.client.render.vulkan.VulkanDevice.check;
import static org.lwjgl.vulkan.KHRAccelerationStructure.*;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK12.VK_BUFFER_USAGE_SHADER_DEVICE_ADDRESS_BIT;

/** Retained terrain BLAS and two fence-owned TLAS/descriptor slots for fragment ray queries. */
final class VulkanRayScene implements AutoCloseable {
    static final int FRAME_COUNT = 2;
    static final int TEXTURE_COUNT = 16;
    private static final int INSTANCE_METADATA_BYTES = 16;
    private static final long HISTORY_BYTES = 64L * 1024 * 1024;
    private static final int HISTORY_MESHES = 64;
    private static final int HISTORY_FRAMES = 8;

    record Instance(Geometry geometry, Matrix4 model, int textureIndex, int flags) {
        Instance(Geometry geometry, Matrix4 model, int textureIndex) { this(geometry, model, textureIndex, 0); }
        Instance {
            Objects.requireNonNull(geometry, "geometry");
            Objects.requireNonNull(model, "model");
            if (textureIndex < 0 || textureIndex >= TEXTURE_COUNT) {
                throw new IllegalArgumentException("Ray scene texture index exceeds the fixed texture table");
            }
        }
    }

    record TextureBinding(long sampler, long view) {
        TextureBinding {
            if (sampler == VK_NULL_HANDLE || view == VK_NULL_HANDLE) {
                throw new IllegalArgumentException("Ray scene textures require a sampler and image view");
            }
        }
    }

    record Statistics(long geometryBuilds, long sceneBuilds, int instances, long triangles) { }

    private final VulkanDevice context;
    private final VulkanArena vertexPool;
    private final VulkanArena uploadPool;
    private final VulkanArena accelerationPool;
    private final VulkanArena scratchPool;
    private final VulkanArena irradiancePool;
    private final Frame[] frames = new Frame[FRAME_COUNT];
    private final Set<Geometry> geometries = Collections.newSetFromMap(new IdentityHashMap<>());
    private final ArrayDeque<Geometry> retiredHistory = new ArrayDeque<>();
    private final Set<Geometry> topologyChanges = Collections.newSetFromMap(new IdentityHashMap<>());
    private long retainedHistoryBytes;
    private VulkanIrradianceCache irradianceCache;
    private long materialRevision;
    private long descriptorPool;
    private long geometryBuilds;
    private long sceneBuilds;
    private int maxInstanceCount;
    private int lastInstanceCount;
    private long lastTriangleCount;
    private boolean closed;

    /** The caller owns the layout and destroys it after this scene and its pipelines. */
    static long createLayout(VulkanDevice context) {
        try (var stack = MemoryStack.stackPush()) {
            var bindings = VkDescriptorSetLayoutBinding.calloc(5, stack);
            bindings.get(0).binding(0).descriptorType(VK_DESCRIPTOR_TYPE_ACCELERATION_STRUCTURE_KHR)
                .descriptorCount(1).stageFlags(rayShaderStages());
            bindings.get(1).binding(1).descriptorType(VK_DESCRIPTOR_TYPE_STORAGE_BUFFER)
                .descriptorCount(1).stageFlags(rayShaderStages());
            bindings.get(2).binding(2).descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
                .descriptorCount(TEXTURE_COUNT).stageFlags(rayShaderStages());
            bindings.get(3).binding(3).descriptorType(VK_DESCRIPTOR_TYPE_STORAGE_BUFFER)
                .descriptorCount(1).stageFlags(rayShaderStages());
            bindings.get(4).binding(4).descriptorType(VK_DESCRIPTOR_TYPE_STORAGE_BUFFER)
                .descriptorCount(1).stageFlags(rayShaderStages());
            var info = VkDescriptorSetLayoutCreateInfo.calloc(stack).sType$Default().pBindings(bindings);
            var handle = stack.mallocLong(1);
            check(vkCreateDescriptorSetLayout(context.device(), info, null, handle), "create ray scene descriptor layout");
            return handle.get(0);
        }
    }

    private static int rayShaderStages() {
        return VK_SHADER_STAGE_VERTEX_BIT | VK_SHADER_STAGE_FRAGMENT_BIT | VK_SHADER_STAGE_COMPUTE_BIT;
    }

    VulkanRayScene(VulkanDevice context, long layout, long pipelineLayout) {
        this.context = Objects.requireNonNull(context, "context");
        if (!context.rayTracingSupported()) throw new IllegalStateException("Vulkan ray queries are not supported");
        this.vertexPool = new VulkanArena(context,
            VK_BUFFER_USAGE_ACCELERATION_STRUCTURE_BUILD_INPUT_READ_ONLY_BIT_KHR |
                VK_BUFFER_USAGE_SHADER_DEVICE_ADDRESS_BIT | VK_BUFFER_USAGE_STORAGE_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
            VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
        this.uploadPool = new VulkanArena(context, VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
            VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
        this.accelerationPool = new VulkanArena(context,
            VK_BUFFER_USAGE_ACCELERATION_STRUCTURE_STORAGE_BIT_KHR | VK_BUFFER_USAGE_SHADER_DEVICE_ADDRESS_BIT,
            VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
        this.scratchPool = new VulkanArena(context,
            VK_BUFFER_USAGE_STORAGE_BUFFER_BIT | VK_BUFFER_USAGE_SHADER_DEVICE_ADDRESS_BIT,
            VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
        this.irradiancePool = new VulkanArena(context,
            VK_BUFFER_USAGE_STORAGE_BUFFER_BIT | VK_BUFFER_USAGE_SHADER_DEVICE_ADDRESS_BIT |
                VK_BUFFER_USAGE_TRANSFER_SRC_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
            VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
        try (var stack = MemoryStack.stackPush()) {
            this.irradianceCache = new VulkanIrradianceCache(context, pipelineLayout);
            var properties = VkPhysicalDeviceProperties.calloc(stack);
            vkGetPhysicalDeviceProperties(context.physicalDevice(), properties);
            long metadataLimit = Integer.toUnsignedLong(properties.limits().maxStorageBufferRange()) / INSTANCE_METADATA_BYTES;
            this.maxInstanceCount = Math.toIntExact(Math.min(0x00ffffffL, Math.min(context.maxRayInstances(), metadataLimit)));
            var sizes = VkDescriptorPoolSize.calloc(3, stack);
            sizes.get(0).type(VK_DESCRIPTOR_TYPE_ACCELERATION_STRUCTURE_KHR).descriptorCount(FRAME_COUNT);
            sizes.get(1).type(VK_DESCRIPTOR_TYPE_STORAGE_BUFFER).descriptorCount(FRAME_COUNT * 3);
            sizes.get(2).type(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER).descriptorCount(FRAME_COUNT * TEXTURE_COUNT);
            var poolInfo = VkDescriptorPoolCreateInfo.calloc(stack).sType$Default().maxSets(FRAME_COUNT).pPoolSizes(sizes);
            var handle = stack.mallocLong(1);
            check(vkCreateDescriptorPool(context.device(), poolInfo, null, handle), "create ray scene descriptor pool");
            this.descriptorPool = handle.get(0);
            var allocation = VkDescriptorSetAllocateInfo.calloc(stack).sType$Default()
                .descriptorPool(this.descriptorPool).pSetLayouts(stack.longs(layout, layout));
            var sets = stack.mallocLong(FRAME_COUNT);
            check(vkAllocateDescriptorSets(context.device(), allocation, sets), "allocate ray scene descriptor sets");
            for (int i = 0; i < FRAME_COUNT; i++) this.frames[i] = new Frame(sets.get(i));
        } catch (RuntimeException | Error error) {
            close();
            throw error;
        }
    }

    long descriptorSet(int slot) {
        return frame(slot).descriptor;
    }

    /** Counts terrain storage pages; the two constant-size TLAS slots are separate. */
    int memoryPages() {
        return this.vertexPool.pageCount() + this.uploadPool.pageCount() +
            this.accelerationPool.pageCount() + this.scratchPool.pageCount() + this.irradiancePool.pageCount();
    }

    /** Scene pools own the backing memory; each geometry owns its slices until its final GPU use. */
    Geometry createGeometry(List<ByteBuffer> blocks, int vertexCount) {
        return createGeometry(blocks, vertexCount, 0, false);
    }

    Geometry createGeometry(List<ByteBuffer> blocks, int vertexCount, int atlas, boolean translucent) {
        if (this.closed) throw new IllegalStateException("Vulkan ray scene is closed");
        if (vertexCount <= 0 || vertexCount % 4 != 0) throw new IllegalArgumentException("Ray scene terrain requires complete quads");
        long inputBytes = 0;
        for (ByteBuffer block : blocks) {
            if (block.remaining() % VertexFormat.STRIDE != 0) throw new IllegalArgumentException("Incomplete terrain vertex block");
            inputBytes = Math.addExact(inputBytes, block.remaining());
        }
        if (inputBytes != Math.multiplyExact((long) vertexCount, VertexFormat.STRIDE)) {
            throw new IllegalArgumentException("Terrain vertex count does not match its blocks");
        }
        if (vertexCount > this.irradianceCache.maxVerticesPerDispatch()) {
            throw new IllegalArgumentException("Terrain mesh exceeds the device compute-dispatch vertex limit");
        }
        Geometry result = new Geometry(this, vertexCount, Bounds.of(blocks), SurfaceData.of(blocks, vertexCount), atlas, translucent);
        if (result.triangleCount > this.context.maxRayPrimitives()) {
            throw new IllegalArgumentException("Terrain mesh exceeds the device acceleration-structure primitive limit");
        }
        try {
            long bytes = Math.multiplyExact((long) result.triangleCount, 3L * VertexFormat.STRIDE);
            Math.toIntExact(bytes); // The mapped triangle stream is represented by one ByteBuffer.
            result.vertices = this.vertexPool.allocate(bytes, VertexFormat.STRIDE);
            result.irradiance = this.irradiancePool.allocate(
                Math.multiplyExact((long) vertexCount, VulkanIrradianceCache.VERTEX_BYTES), 16);
            result.upload = this.uploadPool.allocate(bytes, VertexFormat.STRIDE);
            Geometry.expandQuads(blocks, result.upload.mapped());
            try (var stack = MemoryStack.stackPush()) {
                var geometry = result.triangleGeometry(stack);
                var info = buildInfo(VK_ACCELERATION_STRUCTURE_TYPE_BOTTOM_LEVEL_KHR, geometry, stack);
                var sizes = buildSizes(this.context, info, result.triangleCount, stack);
                result.storage = this.accelerationPool.allocate(Math.max(sizes.accelerationStructureSize(), 256), 256);
                result.acceleration = Acceleration.create(this.context, VK_ACCELERATION_STRUCTURE_TYPE_BOTTOM_LEVEL_KHR,
                    result.storage);
                long alignment = this.context.accelerationScratchAlignment();
                result.scratch = this.scratchPool.allocate(
                    Math.addExact(Math.max(sizes.buildScratchSize(), 1), alignment - 1), alignment);
            }
            result.registered = true;
            inheritHistory(result);
            this.geometries.add(result);
            this.topologyChanges.add(result);
            return result;
        } catch (RuntimeException | Error error) {
            result.close();
            throw error;
        }
    }

    /**
     * The caller has awaited this slot's fence and retains every mesh/texture until submission completes.
     * Models use the same camera-relative coordinates as raster world rendering. Lighting is slot-owned.
     * Call submitted after successful queue submission, or abort when this command buffer is abandoned.
     */
    void recordBuild(VkCommandBuffer command, int slot, List<Instance> instances,
                     List<TextureBinding> textures, VulkanBuffer lighting, VulkanBuffer materials) {
        Frame frame = frame(slot);
        if (frame.recorded) throw new IllegalStateException("Ray scene recording must be submitted or aborted first");
        Objects.requireNonNull(command, "command");
        Objects.requireNonNull(lighting, "lighting");
        Objects.requireNonNull(materials, "materials");
        instances = List.copyOf(instances);
        textures = List.copyOf(textures);
        if (instances.size() > this.maxInstanceCount) throw new IllegalArgumentException("Ray scene exceeds device instance or metadata limits");
        if (textures.isEmpty() || textures.size() > TEXTURE_COUNT) {
            throw new IllegalArgumentException("Ray scene requires one fallback texture and at most sixteen textures");
        }
        for (Instance instance : instances) instance.geometry.requireOwner(this);
        applyTopologyChanges();
        pruneHistory();
        frame.pendingBuilds.clear();
        frame.instanceCount = instances.size();
        frame.triangleCount = 0;
        frame.ensureBuffers(instances.size());
        try (var stack = MemoryStack.stackPush()) {
            writeInstances(frame, instances);
            hostBarrier(command, stack);
            Set<Geometry> seen = Collections.newSetFromMap(new IdentityHashMap<>());
            for (Instance instance : instances) {
                Geometry geometry = instance.geometry;
                frame.triangleCount = Math.addExact(frame.triangleCount, geometry.triangleCount);
                if (!geometry.built && seen.add(geometry)) {
                    try (var uploadStack = MemoryStack.stackPush()) {
                        geometry.recordUpload(command, uploadStack);
                    }
                    frame.pendingBuilds.add(geometry);
                }
            }
            if (!frame.pendingBuilds.isEmpty()) {
                // Cache estimates may have been written by the previous submission. Preserve
                // those values before updating a replacement mesh in this submission.
                memoryBarrier(command, stack, VK_ACCESS_SHADER_WRITE_BIT | VK_ACCESS_TRANSFER_WRITE_BIT,
                    VK_ACCESS_TRANSFER_READ_BIT | VK_ACCESS_TRANSFER_WRITE_BIT,
                    cachePipelineStages() | VK_PIPELINE_STAGE_TRANSFER_BIT, VK_PIPELINE_STAGE_TRANSFER_BIT);
                for (Geometry geometry : frame.pendingBuilds) geometry.recordHistoryCopy(command, stack);
                memoryBarrier(command, stack, VK_ACCESS_TRANSFER_WRITE_BIT,
                    VK_ACCESS_ACCELERATION_STRUCTURE_READ_BIT_KHR | VK_ACCESS_SHADER_READ_BIT | VK_ACCESS_SHADER_WRITE_BIT,
                    VK_PIPELINE_STAGE_TRANSFER_BIT,
                    VK_PIPELINE_STAGE_ACCELERATION_STRUCTURE_BUILD_BIT_KHR | cachePipelineStages());
                for (Geometry geometry : frame.pendingBuilds) {
                    try (var buildStack = MemoryStack.stackPush()) {
                        geometry.recordBuild(command, buildStack);
                    }
                }
            }
            memoryBarrier(command, stack, VK_ACCESS_ACCELERATION_STRUCTURE_WRITE_BIT_KHR,
                VK_ACCESS_ACCELERATION_STRUCTURE_READ_BIT_KHR,
                VK_PIPELINE_STAGE_ACCELERATION_STRUCTURE_BUILD_BIT_KHR, VK_PIPELINE_STAGE_ACCELERATION_STRUCTURE_BUILD_BIT_KHR);
            frame.recordTopLevel(command, instances.size(), stack);
            memoryBarrier(command, stack, VK_ACCESS_ACCELERATION_STRUCTURE_WRITE_BIT_KHR,
                VK_ACCESS_ACCELERATION_STRUCTURE_READ_BIT_KHR | VK_ACCESS_SHADER_READ_BIT,
                VK_PIPELINE_STAGE_ACCELERATION_STRUCTURE_BUILD_BIT_KHR, cachePipelineStages());
            updateDescriptors(frame, textures, lighting, materials, stack);
            this.irradianceCache.record(command, slot, instances, lighting, frame.descriptor, this.materialRevision);
            frame.recorded = true;
        } catch (RuntimeException | Error error) {
            this.irradianceCache.abort(slot);
            frame.pendingBuilds.clear();
            throw error;
        }
    }

    /** The retirement callback must defer work until this slot's submitted fence completes. */
    void submitted(int slot, Consumer<Runnable> retire) {
        Frame frame = frame(slot);
        Objects.requireNonNull(retire, "retire");
        if (!frame.recorded) throw new IllegalStateException("No ray scene recording to submit");
        this.irradianceCache.submitted(slot);
        for (Geometry geometry : frame.pendingBuilds) {
            geometry.built = true;
            retire.accept(geometry::releaseBuildResources);
        }
        this.geometryBuilds += frame.pendingBuilds.size();
        this.sceneBuilds++;
        this.lastInstanceCount = frame.instanceCount;
        this.lastTriangleCount = frame.triangleCount;
        frame.pendingBuilds.clear();
        frame.recorded = false;
    }

    void abort(int slot) {
        Frame frame = frame(slot);
        this.irradianceCache.abort(slot);
        frame.pendingBuilds.clear();
        frame.recorded = false;
    }

    Statistics statistics() {
        return new Statistics(this.geometryBuilds, this.sceneBuilds, this.lastInstanceCount, this.lastTriangleCount);
    }

    VulkanIrradianceCache.Statistics irradianceStatistics() { return this.irradianceCache.statistics(); }

    /** In-place material/atlas changes can request refresh without discarding the previous smooth result. */
    void invalidateIrradiance() {
        this.materialRevision++;
    }

    private void invalidateNear(Bounds changed) {
        // Most changed transport is local. More distant occluders are picked up by periodic
        // rolling refresh, without resetting every loaded chunk while terrain is streaming.
        for (Geometry geometry : this.geometries) {
            if (!geometry.retired && geometry.bounds.distanceSquared(changed) <= 64 * 64) geometry.irradianceRevision++;
        }
    }

    /** Only overlapping terrain allocations are indexed, never the entire loaded world's faces. */
    private void inheritHistory(Geometry target) {
        pruneHistory();
        int quads = target.surfaces.count();
        Geometry[] sources = new Geometry[quads];
        int[] sourceQuads = new int[quads];
        var candidates = new ArrayList<>(this.geometries);
        for (Geometry geometry : this.retiredHistory) if (geometry.closed) candidates.add(geometry);
        Geometry predecessor = null;
        int bestMatches = 0;
        int[] predecessorMatches = null;
        for (Geometry candidate : candidates) {
            if (candidate.atlas != target.atlas || candidate.translucent != target.translucent ||
                candidate.bounds.distanceSquared(target.bounds) != 0) continue;
            var index = new HashMap<SurfaceKey, Integer>();
            for (int quad = 0; quad < candidate.surfaces.count(); quad++) index.put(candidate.surfaces.key(quad), quad);
            int[] matches = new int[quads];
            java.util.Arrays.fill(matches, -1);
            int matched = 0;
            int sequence = candidate.historySequence();
            boolean canCopy = candidate.built && sequence > 0 && candidate.irradiance != null;
            for (int quad = 0; quad < quads; quad++) {
                Integer source = index.get(target.surfaces.key(quad));
                if (source == null) continue;
                matches[quad] = source;
                matched++;
                if (canCopy && (sources[quad] == null || sequence > sources[quad].historySequence())) {
                    sources[quad] = candidate;
                    sourceQuads[quad] = source;
                }
            }
            if (matched > bestMatches && matched * 2L >= quads && matched * 2L >= candidate.surfaces.count()) {
                predecessor = candidate;
                predecessorMatches = matches;
                bestMatches = matched;
            }
        }
        if (predecessor != null) {
            // Ambient-occlusion RGB changes during a rebuild, even on unchanged faces. Surface
            // identities deliberately compare geometry, UV, alpha and unlit material instead.
            var matched = new BitSet(predecessor.surfaces.count());
            Bounds changes = null;
            for (int quad = 0; quad < quads; quad++) {
                int source = predecessorMatches[quad];
                if (source >= 0) matched.set(source);
                else changes = Bounds.union(changes, target.surfaces.bounds(quad));
            }
            for (int quad = 0; quad < predecessor.surfaces.count(); quad++) {
                if (!matched.get(quad)) changes = Bounds.union(changes, predecessor.surfaces.bounds(quad));
            }
            // Several CPU rebuilds can replace one another before any reaches the GPU.
            // Preserve the first edit's transport change through that unsubmitted chain.
            if (!predecessor.built) changes = Bounds.union(changes, predecessor.changedBounds);
            target.changedBounds = changes;
            predecessor.replacementAssociated = true;
        }
        int sequence = 0;
        for (int quad = 0; quad < quads;) {
            Geometry source = sources[quad];
            if (source == null) { quad++; continue; }
            int first = quad++;
            while (quad < quads && sources[quad] == source && sourceQuads[quad] == sourceQuads[first] + quad - first) quad++;
            target.historyCopies.add(new HistoryCopy(source, sourceQuads[first], first, quad - first));
            if (target.historySources.add(source)) source.historyReferences++;
            sequence = Math.max(sequence, source.historySequence());
        }
        if (sequence > 0) this.irradianceCache.inherit(target, sequence);
    }

    private void applyTopologyChanges() {
        for (Geometry geometry : this.topologyChanges) {
            if (geometry.retired) {
                if (geometry.built && !geometry.replacementAssociated) invalidateNear(geometry.bounds);
            } else if (geometry.changedBounds != null) invalidateNear(geometry.changedBounds);
        }
        this.topologyChanges.clear();
    }

    private void retainHistory(Geometry geometry) {
        if (!geometry.built || geometry.historySequence() == 0 || geometry.historyRetained) return;
        geometry.historyRetained = true;
        geometry.retiredFrame = this.sceneBuilds;
        this.retiredHistory.addLast(geometry);
        this.retainedHistoryBytes += geometry.irradiance.size();
        pruneHistory();
    }

    private void pruneHistory() {
        while (!this.retiredHistory.isEmpty()) {
            Geometry oldest = this.retiredHistory.peekFirst();
            if (this.retiredHistory.size() <= HISTORY_MESHES && this.retainedHistoryBytes <= HISTORY_BYTES &&
                this.sceneBuilds - oldest.retiredFrame < HISTORY_FRAMES) break;
            this.retiredHistory.removeFirst();
            this.retainedHistoryBytes -= oldest.irradiance.size();
            oldest.historyRetained = false;
            oldest.releaseIrradianceIfUnused();
        }
    }

    private record SurfaceKey(long first, long second) { }
    private record HistoryCopy(Geometry source, int sourceQuad, int targetQuad, int quads) { }

    /** Compact fingerprints plus conservative face bounds; no duplicate CPU vertex stream. */
    private record SurfaceData(long[] signatures, float[] centers) {
        int count() { return this.signatures.length / 2; }
        SurfaceKey key(int quad) { return new SurfaceKey(this.signatures[quad * 2], this.signatures[quad * 2 + 1]); }
        Bounds bounds(int quad) {
            int at = quad * 4;
            double x = this.centers[at], y = this.centers[at + 1], z = this.centers[at + 2], extent = this.centers[at + 3];
            return new Bounds(x - extent, y - extent, z - extent, x + extent, y + extent, z + extent);
        }

        static SurfaceData of(List<ByteBuffer> blocks, int vertices) {
            long[] signatures = new long[vertices / 2];
            float[] centers = new float[vertices];
            int vertex = 0;
            long first = 0xcbf29ce484222325L, second = 0x9e3779b97f4a7c15L;
            float minX = Float.POSITIVE_INFINITY, minY = minX, minZ = minX;
            float maxX = Float.NEGATIVE_INFINITY, maxY = maxX, maxZ = maxX;
            for (ByteBuffer block : blocks) {
                ByteBuffer source = block.duplicate().order(ByteOrder.nativeOrder());
                for (int at = source.position(); at < source.limit(); at += VertexFormat.STRIDE) {
                    // Ordered corners preserve the compute shader's normal, face inset and cache order.
                    for (int word = 0; word < 8; word++) {
                        int value = source.getInt(at + word * 4);
                        // Normals come from positions; skylight and wetness are live shading.
                        // Water alone changes ray transmission even with an opaque custom atlas.
                        if (word == VertexFormat.NORMAL_OFFSET / 4) value = value >>> 24 & VertexFormat.WATER;
                        if (word == VertexFormat.COLOR_OFFSET / 4) value >>>= 24; // Only alpha affects traced coverage.
                        first = (first ^ Integer.toUnsignedLong(value)) * 0x100000001b3L;
                        second = Long.rotateLeft(second ^ Integer.toUnsignedLong(value) * 0x9e3779b97f4a7c15L, 27) * 0x94d049bb133111ebL;
                    }
                    float x = source.getFloat(at), y = source.getFloat(at + 4), z = source.getFloat(at + 8);
                    minX = Math.min(minX, x); minY = Math.min(minY, y); minZ = Math.min(minZ, z);
                    maxX = Math.max(maxX, x); maxY = Math.max(maxY, y); maxZ = Math.max(maxZ, z);
                    if (++vertex % 4 == 0) {
                        int quad = vertex / 4 - 1;
                        signatures[quad * 2] = first;
                        signatures[quad * 2 + 1] = second;
                        int index = quad * 4;
                        centers[index] = (minX + maxX) * .5f;
                        centers[index + 1] = (minY + maxY) * .5f;
                        centers[index + 2] = (minZ + maxZ) * .5f;
                        centers[index + 3] = Math.max(maxX - minX, Math.max(maxY - minY, maxZ - minZ)) * .5f;
                        first = 0xcbf29ce484222325L; second = 0x9e3779b97f4a7c15L;
                        minX = minY = minZ = Float.POSITIVE_INFINITY;
                        maxX = maxY = maxZ = Float.NEGATIVE_INFINITY;
                    }
                }
            }
            return new SurfaceData(signatures, centers);
        }
    }

    private Frame frame(int slot) {
        if (this.closed) throw new IllegalStateException("Vulkan ray scene is closed");
        if (slot < 0 || slot >= FRAME_COUNT) throw new IllegalArgumentException("Invalid ray scene frame slot");
        return this.frames[slot];
    }

    private static void writeInstances(Frame frame, List<Instance> instances) {
        ByteBuffer metadata = frame.metadata.mapped();
        ByteBuffer instanceBytes = frame.instances.mapped();
        if (instances.isEmpty()) {
            metadata.putLong(0, 0).putLong(8, 0);
            return;
        }
        var nativeInstances = new VkAccelerationStructureInstanceKHR.Buffer(instanceBytes);
        for (int i = 0; i < instances.size(); i++) {
            Instance instance = instances.get(i);
            var nativeInstance = nativeInstances.get(i);
            for (int row = 0; row < 3; row++) {
                for (int column = 0; column < 4; column++) {
                    nativeInstance.transform().matrix(row * 4 + column, instance.model.get(column * 4 + row));
                }
            }
            nativeInstance.instanceCustomIndex(i).mask(0xff).instanceShaderBindingTableRecordOffset(0)
                .flags(VK_GEOMETRY_INSTANCE_TRIANGLE_FACING_CULL_DISABLE_BIT_KHR)
                .accelerationStructureReference(instance.geometry.acceleration.address);
            int offset = i * INSTANCE_METADATA_BYTES;
            metadata.putLong(offset, instance.geometry.vertices.deviceAddress())
                .putInt(offset + 8, instance.textureIndex).putInt(offset + 12, instance.flags);
        }
    }

    private void updateDescriptors(Frame frame, List<TextureBinding> textures, VulkanBuffer lighting,
                                   VulkanBuffer materials, MemoryStack stack) {
        var acceleration = VkWriteDescriptorSetAccelerationStructureKHR.calloc(stack).sType$Default()
            .pAccelerationStructures(stack.longs(frame.acceleration.handle));
        var metadata = VkDescriptorBufferInfo.calloc(1, stack).buffer(frame.metadata.handle()).offset(0).range(frame.metadata.size());
        var parameters = VkDescriptorBufferInfo.calloc(1, stack).buffer(lighting.handle()).offset(0).range(lighting.size());
        var tileParameters = VkDescriptorBufferInfo.calloc(1, stack).buffer(materials.handle()).offset(0).range(materials.size());
        var images = VkDescriptorImageInfo.calloc(TEXTURE_COUNT, stack);
        for (int i = 0; i < TEXTURE_COUNT; i++) {
            TextureBinding texture = textures.get(i < textures.size() ? i : 0);
            images.get(i).sampler(texture.sampler).imageView(texture.view).imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
        }
        var writes = VkWriteDescriptorSet.calloc(5, stack);
        writes.get(0).sType$Default().dstSet(frame.descriptor).dstBinding(0)
            .descriptorType(VK_DESCRIPTOR_TYPE_ACCELERATION_STRUCTURE_KHR).descriptorCount(1).pNext(acceleration.address());
        writes.get(1).sType$Default().dstSet(frame.descriptor).dstBinding(1)
            .descriptorType(VK_DESCRIPTOR_TYPE_STORAGE_BUFFER).descriptorCount(1).pBufferInfo(metadata);
        writes.get(2).sType$Default().dstSet(frame.descriptor).dstBinding(2)
            .descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER).descriptorCount(TEXTURE_COUNT).pImageInfo(images);
        writes.get(3).sType$Default().dstSet(frame.descriptor).dstBinding(3)
            .descriptorType(VK_DESCRIPTOR_TYPE_STORAGE_BUFFER).descriptorCount(1).pBufferInfo(parameters);
        writes.get(4).sType$Default().dstSet(frame.descriptor).dstBinding(4)
            .descriptorType(VK_DESCRIPTOR_TYPE_STORAGE_BUFFER).descriptorCount(1).pBufferInfo(tileParameters);
        vkUpdateDescriptorSets(this.context.device(), writes, null);
    }

    private static void hostBarrier(VkCommandBuffer command, MemoryStack stack) {
        memoryBarrier(command, stack, VK_ACCESS_HOST_WRITE_BIT,
            VK_ACCESS_ACCELERATION_STRUCTURE_READ_BIT_KHR | VK_ACCESS_SHADER_READ_BIT | VK_ACCESS_TRANSFER_READ_BIT,
            VK_PIPELINE_STAGE_HOST_BIT, VK_PIPELINE_STAGE_ACCELERATION_STRUCTURE_BUILD_BIT_KHR |
                cachePipelineStages() | VK_PIPELINE_STAGE_TRANSFER_BIT);
    }

    private static int cachePipelineStages() {
        return VK_PIPELINE_STAGE_VERTEX_SHADER_BIT | VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT | VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT;
    }

    private static void memoryBarrier(VkCommandBuffer command, MemoryStack stack, int sourceAccess, int targetAccess,
                                      int sourceStage, int targetStage) {
        var barrier = VkMemoryBarrier.calloc(1, stack).sType$Default().srcAccessMask(sourceAccess).dstAccessMask(targetAccess);
        vkCmdPipelineBarrier(command, sourceStage, targetStage, 0, barrier, null, null);
    }

    private record Bounds(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        static Bounds union(Bounds first, Bounds second) {
            if (first == null) return second;
            if (second == null) return first;
            return new Bounds(Math.min(first.minX, second.minX), Math.min(first.minY, second.minY), Math.min(first.minZ, second.minZ),
                Math.max(first.maxX, second.maxX), Math.max(first.maxY, second.maxY), Math.max(first.maxZ, second.maxZ));
        }

        static Bounds of(List<ByteBuffer> blocks) {
            double minX = Double.POSITIVE_INFINITY, minY = minX, minZ = minX;
            double maxX = Double.NEGATIVE_INFINITY, maxY = maxX, maxZ = maxX;
            for (ByteBuffer block : blocks) {
                ByteBuffer source = block.duplicate().order(ByteOrder.nativeOrder());
                for (int offset = source.position(); offset < source.limit(); offset += VertexFormat.STRIDE) {
                    float x = source.getFloat(offset + VertexFormat.POSITION_OFFSET);
                    float y = source.getFloat(offset + VertexFormat.POSITION_OFFSET + 4);
                    float z = source.getFloat(offset + VertexFormat.POSITION_OFFSET + 8);
                    if (!Float.isFinite(x) || !Float.isFinite(y) || !Float.isFinite(z)) continue;
                    minX = Math.min(minX, x); minY = Math.min(minY, y); minZ = Math.min(minZ, z);
                    maxX = Math.max(maxX, x); maxY = Math.max(maxY, y); maxZ = Math.max(maxZ, z);
                }
            }
            return minX == Double.POSITIVE_INFINITY ? new Bounds(0, 0, 0, 0, 0, 0)
                : new Bounds(minX, minY, minZ, maxX, maxY, maxZ);
        }

        double distanceSquared(Bounds other) {
            double x = Math.max(0, Math.max(this.minX - other.maxX, other.minX - this.maxX));
            double y = Math.max(0, Math.max(this.minY - other.maxY, other.minY - this.maxY));
            double z = Math.max(0, Math.max(this.minZ - other.maxZ, other.minZ - this.maxZ));
            return x * x + y * y + z * z;
        }

        double cameraDistanceSquared(Matrix4 model) {
            double x = (this.minX + this.maxX) * .5;
            double y = (this.minY + this.maxY) * .5;
            double z = (this.minZ + this.maxZ) * .5;
            double tx = x * model.get(0) + y * model.get(4) + z * model.get(8) + model.get(12);
            double ty = x * model.get(1) + y * model.get(5) + z * model.get(9) + model.get(13);
            double tz = x * model.get(2) + y * model.get(6) + z * model.get(10) + model.get(14);
            return tx * tx + ty * ty + tz * tz;
        }
    }

    /** Owns expanded terrain triangles and their BLAS. Close only when the renderer releases the actual mesh. */
    static final class Geometry implements AutoCloseable {
        private final VulkanRayScene owner;
        private final VulkanDevice context;
        private final int triangleCount;
        private final int originalVertexCount;
        private final Bounds bounds;
        private final SurfaceData surfaces;
        private final int atlas;
        private final boolean translucent;
        private final List<HistoryCopy> historyCopies = new ArrayList<>();
        private final Set<Geometry> historySources = Collections.newSetFromMap(new IdentityHashMap<>());
        private Bounds changedBounds;
        private long irradianceRevision;
        private int savedSequence;
        private int historyReferences;
        private long retiredFrame;
        private VulkanArena.Slice vertices;
        private VulkanArena.Slice irradiance;
        private VulkanArena.Slice upload;
        private VulkanArena.Slice storage;
        private VulkanArena.Slice scratch;
        private Acceleration acceleration;
        private boolean built;
        private boolean closed;
        private boolean registered;
        private boolean retired;
        private boolean replacementAssociated;
        private boolean historyRetained;

        private Geometry(VulkanRayScene owner, int originalVertexCount, Bounds bounds, SurfaceData surfaces,
                         int atlas, boolean translucent) {
            this.owner = owner;
            this.context = owner.context;
            this.originalVertexCount = originalVertexCount;
            this.bounds = bounds;
            this.changedBounds = bounds;
            this.surfaces = surfaces;
            this.atlas = atlas;
            this.translucent = translucent;
            this.triangleCount = Math.multiplyExact(originalVertexCount / 4, 2);
        }

        long irradianceAddress() { return this.irradiance.deviceAddress(); }
        long vertexAddress() { return this.vertices.deviceAddress(); }
        int originalVertexCount() { return this.originalVertexCount; }
        long irradianceRevision() { return this.irradianceRevision; }
        double cameraDistanceSquared(Matrix4 model) { return this.bounds.cameraDistanceSquared(model); }
        private int historySequence() { return Math.max(this.savedSequence, this.owner.irradianceCache.sequence(this)); }

        /** Logical scene removal happens before the fence-delayed destruction of GPU resources. */
        void retire() {
            if (this.retired || !this.registered) return;
            this.retired = true;
            this.savedSequence = historySequence();
            this.owner.topologyChanges.add(this);
            this.owner.retainHistory(this);
        }

        private static void expandQuads(List<ByteBuffer> blocks, ByteBuffer target) {
            byte[] quad = new byte[VertexFormat.STRIDE * 4];
            int[] triangleVertices = {0, 1, 2, 0, 2, 3};
            int filled = 0;
            for (ByteBuffer block : blocks) {
                ByteBuffer source = block.duplicate();
                while (source.hasRemaining()) {
                    int count = Math.min(source.remaining(), quad.length - filled);
                    source.get(quad, filled, count);
                    filled += count;
                    if (filled == quad.length) {
                        for (int vertex : triangleVertices) target.put(quad, vertex * VertexFormat.STRIDE, VertexFormat.STRIDE);
                        filled = 0;
                    }
                }
            }
        }

        private VkAccelerationStructureGeometryKHR.Buffer triangleGeometry(MemoryStack stack) {
            var geometry = VkAccelerationStructureGeometryKHR.calloc(1, stack).sType$Default()
                .geometryType(VK_GEOMETRY_TYPE_TRIANGLES_KHR).flags(0);
            var triangles = geometry.geometry().triangles().sType$Default().vertexFormat(VK_FORMAT_R32G32B32_SFLOAT)
                .vertexStride(VertexFormat.STRIDE).maxVertex(this.triangleCount * 3 - 1).indexType(VK_INDEX_TYPE_NONE_KHR);
            triangles.vertexData().deviceAddress(this.vertices.deviceAddress() + VertexFormat.POSITION_OFFSET);
            return geometry;
        }

        private void recordBuild(VkCommandBuffer command, MemoryStack stack) {
            var geometry = triangleGeometry(stack);
            var infos = VkAccelerationStructureBuildGeometryInfoKHR.calloc(1, stack);
            infos.get(0).set(buildInfo(VK_ACCELERATION_STRUCTURE_TYPE_BOTTOM_LEVEL_KHR, geometry, stack))
                .dstAccelerationStructure(this.acceleration.handle);
            infos.get(0).scratchData().deviceAddress(alignedScratchAddress(this.context, this.scratch.deviceAddress()));
            var range = VkAccelerationStructureBuildRangeInfoKHR.calloc(stack).primitiveCount(this.triangleCount);
            vkCmdBuildAccelerationStructuresKHR(command, infos, stack.pointers(range.address()));
        }

        private void recordUpload(VkCommandBuffer command, MemoryStack stack) {
            var copy = VkBufferCopy.calloc(1, stack).srcOffset(this.upload.offset())
                .dstOffset(this.vertices.offset()).size(this.vertices.size());
            vkCmdCopyBuffer(command, this.upload.handle(), this.vertices.handle(), copy);
            vkCmdFillBuffer(command, this.irradiance.handle(), this.irradiance.offset(), this.irradiance.size(), 0);
        }

        private void recordHistoryCopy(VkCommandBuffer command, MemoryStack stack) {
            if (this.historyCopies.isEmpty()) return;
            var copy = VkBufferCopy.calloc(1, stack);
            long quadBytes = 4L * VulkanIrradianceCache.VERTEX_BYTES;
            for (HistoryCopy history : this.historyCopies) {
                var source = history.source.irradiance;
                copy.srcOffset(source.offset() + history.sourceQuad * quadBytes)
                    .dstOffset(this.irradiance.offset() + history.targetQuad * quadBytes).size(history.quads * quadBytes);
                vkCmdCopyBuffer(command, source.handle(), this.irradiance.handle(), copy);
            }
        }

        private void requireOwner(VulkanRayScene owner) {
            if (this.closed || this.owner != owner) throw new IllegalArgumentException("Ray geometry is closed or belongs to another scene");
        }

        private void releaseBuildResources() {
            if (this.scratch != null) {
                this.scratch.close();
                this.scratch = null;
            }
            if (this.upload != null) {
                this.upload.close();
                this.upload = null;
            }
            for (Geometry source : this.historySources) {
                source.historyReferences--;
                source.releaseIrradianceIfUnused();
            }
            this.historySources.clear();
            this.historyCopies.clear();
        }

        private void releaseIrradianceIfUnused() {
            if (this.closed && !this.historyRetained && this.historyReferences == 0 && this.irradiance != null) {
                this.irradiance.close();
                this.irradiance = null;
            }
        }

        @Override public void close() {
            if (this.closed) return;
            retire();
            this.closed = true;
            if (this.registered) {
                this.owner.geometries.remove(this);
                this.owner.irradianceCache.forget(this);
            }
            if (this.acceleration != null) this.acceleration.close();
            if (this.storage != null) this.storage.close();
            releaseBuildResources();
            if (this.vertices != null) this.vertices.close();
            releaseIrradianceIfUnused();
        }
    }

    private final class Frame implements AutoCloseable {
        final long descriptor;
        final List<Geometry> pendingBuilds = new ArrayList<>();
        VulkanBuffer instances;
        VulkanBuffer metadata;
        VulkanBuffer scratch;
        Acceleration acceleration;
        int capacity;
        int instanceCount;
        long triangleCount;
        boolean recorded;

        Frame(long descriptor) { this.descriptor = descriptor; }

        void ensureBuffers(int count) {
            int required = Math.max(count, 1);
            if (required <= this.capacity) return;
            VulkanBuffer replacementInstances = null;
            VulkanBuffer replacementMetadata = null;
            VulkanBuffer replacementScratch = null;
            Acceleration replacementAcceleration = null;
            try (var stack = MemoryStack.stackPush()) {
                int capacity = Math.min(maxInstanceCount, Math.max(16, Integer.highestOneBit(required - 1) << 1));
                replacementInstances = context.hostBuffer(Math.multiplyExact((long) capacity, VkAccelerationStructureInstanceKHR.SIZEOF),
                    VK_BUFFER_USAGE_ACCELERATION_STRUCTURE_BUILD_INPUT_READ_ONLY_BIT_KHR | VK_BUFFER_USAGE_SHADER_DEVICE_ADDRESS_BIT);
                replacementMetadata = context.hostBuffer(Math.multiplyExact((long) capacity, INSTANCE_METADATA_BYTES), VK_BUFFER_USAGE_STORAGE_BUFFER_BIT);
                var geometry = topLevelGeometry(replacementInstances, stack);
                var info = buildInfo(VK_ACCELERATION_STRUCTURE_TYPE_TOP_LEVEL_KHR, geometry, stack);
                var sizes = buildSizes(context, info, capacity, stack);
                replacementAcceleration = Acceleration.create(context, VK_ACCELERATION_STRUCTURE_TYPE_TOP_LEVEL_KHR, sizes.accelerationStructureSize());
                replacementScratch = scratch(context, sizes.buildScratchSize());
                closeBuffers();
                this.instances = replacementInstances;
                this.metadata = replacementMetadata;
                this.scratch = replacementScratch;
                this.acceleration = replacementAcceleration;
                this.capacity = capacity;
            } catch (RuntimeException | Error error) {
                if (replacementAcceleration != null) replacementAcceleration.close();
                if (replacementScratch != null) replacementScratch.close();
                if (replacementMetadata != null) replacementMetadata.close();
                if (replacementInstances != null) replacementInstances.close();
                throw error;
            }
        }

        void recordTopLevel(VkCommandBuffer command, int count, MemoryStack stack) {
            var geometry = topLevelGeometry(this.instances, stack);
            var infos = VkAccelerationStructureBuildGeometryInfoKHR.calloc(1, stack);
            infos.get(0).set(buildInfo(VK_ACCELERATION_STRUCTURE_TYPE_TOP_LEVEL_KHR, geometry, stack))
                .dstAccelerationStructure(this.acceleration.handle);
            infos.get(0).scratchData().deviceAddress(alignedScratchAddress(context, this.scratch.deviceAddress()));
            var range = VkAccelerationStructureBuildRangeInfoKHR.calloc(stack).primitiveCount(count);
            vkCmdBuildAccelerationStructuresKHR(command, infos, stack.pointers(range.address()));
        }

        private void closeBuffers() {
            if (this.acceleration != null) this.acceleration.close();
            if (this.scratch != null) this.scratch.close();
            if (this.metadata != null) this.metadata.close();
            if (this.instances != null) this.instances.close();
        }

        @Override public void close() { closeBuffers(); pendingBuilds.clear(); }
    }

    private static VkAccelerationStructureGeometryKHR.Buffer topLevelGeometry(VulkanBuffer instances, MemoryStack stack) {
        var geometry = VkAccelerationStructureGeometryKHR.calloc(1, stack).sType$Default().geometryType(VK_GEOMETRY_TYPE_INSTANCES_KHR);
        geometry.geometry().instances().sType$Default().arrayOfPointers(false).data().deviceAddress(instances.deviceAddress());
        return geometry;
    }

    private static VkAccelerationStructureBuildGeometryInfoKHR buildInfo(int type, VkAccelerationStructureGeometryKHR.Buffer geometry,
                                                                        MemoryStack stack) {
        return VkAccelerationStructureBuildGeometryInfoKHR.calloc(stack).sType$Default().type(type)
            .flags(VK_BUILD_ACCELERATION_STRUCTURE_PREFER_FAST_TRACE_BIT_KHR).mode(VK_BUILD_ACCELERATION_STRUCTURE_MODE_BUILD_KHR)
            .pGeometries(geometry).geometryCount(geometry.remaining());
    }

    private static VkAccelerationStructureBuildSizesInfoKHR buildSizes(VulkanDevice context, VkAccelerationStructureBuildGeometryInfoKHR info,
                                                                     int primitiveCount, MemoryStack stack) {
        var sizes = VkAccelerationStructureBuildSizesInfoKHR.calloc(stack).sType$Default();
        vkGetAccelerationStructureBuildSizesKHR(context.device(), VK_ACCELERATION_STRUCTURE_BUILD_TYPE_DEVICE_KHR,
            info, stack.ints(primitiveCount), sizes);
        return sizes;
    }

    private static VulkanBuffer scratch(VulkanDevice context, long bytes) {
        long alignment = context.accelerationScratchAlignment();
        return context.buffer(Math.addExact(Math.max(bytes, 1), alignment - 1),
            VK_BUFFER_USAGE_STORAGE_BUFFER_BIT | VK_BUFFER_USAGE_SHADER_DEVICE_ADDRESS_BIT, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
    }

    private static long alignedScratchAddress(VulkanDevice context, long address) {
        long mask = context.accelerationScratchAlignment() - 1;
        return address + (-address & mask);
    }

    private static final class Acceleration implements AutoCloseable {
        final VulkanDevice context;
        final VulkanBuffer storage;
        final long handle;
        final long address;

        private Acceleration(VulkanDevice context, VulkanBuffer storage, long handle, long address) {
            this.context = context;
            this.storage = storage;
            this.handle = handle;
            this.address = address;
        }

        static Acceleration create(VulkanDevice context, int type, long size) {
            VulkanBuffer storage = context.buffer(Math.max(size, 256), VK_BUFFER_USAGE_ACCELERATION_STRUCTURE_STORAGE_BIT_KHR |
                VK_BUFFER_USAGE_SHADER_DEVICE_ADDRESS_BIT, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
            try {
                return create(context, type, storage.handle(), 0, storage.size(), storage);
            } catch (RuntimeException | Error error) {
                storage.close();
                throw error;
            }
        }

        /** The geometry owns this pooled slice and releases it after destroying the handle. */
        static Acceleration create(VulkanDevice context, int type, VulkanArena.Slice storage) {
            return create(context, type, storage.handle(), storage.offset(), storage.size(), null);
        }

        private static Acceleration create(VulkanDevice context, int type, long buffer, long offset, long size,
                                           VulkanBuffer dedicatedStorage) {
            long handle = VK_NULL_HANDLE;
            try (var stack = MemoryStack.stackPush()) {
                var info = VkAccelerationStructureCreateInfoKHR.calloc(stack).sType$Default()
                    .buffer(buffer).offset(offset).size(size).type(type);
                var result = stack.mallocLong(1);
                check(vkCreateAccelerationStructureKHR(context.device(), info, null, result), "create ray scene acceleration structure");
                handle = result.get(0);
                var addressInfo = VkAccelerationStructureDeviceAddressInfoKHR.calloc(stack).sType$Default().accelerationStructure(handle);
                long address = vkGetAccelerationStructureDeviceAddressKHR(context.device(), addressInfo);
                return new Acceleration(context, dedicatedStorage, handle, address);
            } catch (RuntimeException | Error error) {
                if (handle != VK_NULL_HANDLE) vkDestroyAccelerationStructureKHR(context.device(), handle, null);
                throw error;
            }
        }

        @Override public void close() {
            vkDestroyAccelerationStructureKHR(this.context.device(), this.handle, null);
            if (this.storage != null) this.storage.close();
        }
    }

    /** All slots must have completed and all mesh geometries must be closed before their pools. */
    @Override public void close() {
        if (this.closed) return;
        this.closed = true;
        for (Frame frame : this.frames) if (frame != null) frame.close();
        if (this.descriptorPool != VK_NULL_HANDLE) vkDestroyDescriptorPool(this.context.device(), this.descriptorPool, null);
        this.descriptorPool = VK_NULL_HANDLE;
        if (this.irradianceCache != null) this.irradianceCache.close();
        for (Geometry geometry : this.retiredHistory) {
            geometry.historyRetained = false;
            geometry.releaseIrradianceIfUnused();
        }
        this.retiredHistory.clear();
        this.topologyChanges.clear();
        this.retainedHistoryBytes = 0;
        this.irradiancePool.close();
        this.scratchPool.close();
        this.accelerationPool.close();
        this.uploadPool.close();
        this.vertexPool.close();
    }
}
