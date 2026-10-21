package dev.adventurecraft.awakening.client.render.vulkan;

import dev.adventurecraft.awakening.client.render.VertexFormat;
import dev.adventurecraft.awakening.client.render.RenderPhase;
import dev.adventurecraft.awakening.client.render.RenderMath;
import dev.adventurecraft.awakening.client.render.state.*;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Native Vulkan renderer for the game's fixed function render stream. All passes
 * preserve the order of draws and clears, including retained world and UI attachments.
 * The completed RGBA8 image is
 * copied directly to the Vulkan swapchain; CPU readback
 * is available separately for screenshots only. Render-thread ownership only.
 */
public final class VulkanRenderer implements AutoCloseable {
    public static final int VERTEX_STRIDE = VertexFormat.STRIDE;
    private static final int FRAMES_IN_FLIGHT = 2;
    private static final int COLOR_FORMAT = VK_FORMAT_R8G8B8A8_UNORM;
    private static final int WORLD_FORMAT = VK_FORMAT_R16G16B16A16_SFLOAT;
    private static final int DEPTH_FORMAT = VK_FORMAT_D32_SFLOAT;

    private final VulkanDevice context;
    private final Set<Mesh> meshes = new HashSet<>();
    private final Map<FrameView, PreparedView> preparedViews = boundedCache(256);
    private final Map<MaterialState, PreparedMaterial> preparedMaterials = boundedCache(512);
    private final List<Runnable> pendingRetirements = new ArrayList<>();
    private final VulkanUploadBatch uploads = new VulkanUploadBatch();
    private final List<VulkanArena.Slice> sharedQuadIndices = new ArrayList<>();
    private final VulkanArena geometry;
    private final VulkanArena staging;

    private FrameRecording recording;
    private final FrameResources[] frames = new FrameResources[FRAMES_IN_FLIGHT];
    private FrameResources current;
    private FrameResources lastFrame;
    private int nextFrame;
    private VulkanCommandSlot lastSubmission;

    private boolean wideLines;
    private float minLineWidth = 1;
    private float maxLineWidth = 1;
    private Mesh clearMesh;
    private Texture clearTexture;

    private VulkanPipelines pipelines;
    private VulkanBindings bindings;
    private VulkanTextures textures;
    private long renderPass;
    private VulkanRenderPasses renderPasses;
    private long meshIdentities;

    private long preparedPasses;
    private long uniformWrites;
    private long frameSubmissions;
    private long uploadedBytes;
    private long viewWrites;
    private long materialWrites;
    private long pipelineBinds;
    private long descriptorBinds;
    private long dynamicStateWrites;
    private boolean closed;

    public VulkanRenderer(VulkanDevice context) {
        this.context = java.util.Objects.requireNonNull(context, "context");
        geometry = new VulkanArena(context,
            VK_BUFFER_USAGE_VERTEX_BUFFER_BIT | VK_BUFFER_USAGE_INDEX_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
            VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
        staging = new VulkanArena(context, VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
            VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
        try {
            requireFormats();
            initializeRasterLimits();
            bindings = new VulkanBindings(context, FRAMES_IN_FLIGHT);
            textures = new VulkanTextures(context, bindings, staging, uploads, this::retire,
                bytes -> uploadedBytes += bytes);
            createRenderPass();
            pipelines = new VulkanPipelines(context, bindings.pipelineLayout(), renderPass, false);
            for (int i = 0; i < frames.length; i++) frames[i] = new FrameResources(i);
        } catch (RuntimeException | Error error) {
            close();
            throw error;
        }
    }

    public enum PrimitiveTopology {
        POINTS(VK_PRIMITIVE_TOPOLOGY_POINT_LIST),
        LINES(VK_PRIMITIVE_TOPOLOGY_LINE_LIST),
        TRIANGLES(VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST);
        private final int value;

        PrimitiveTopology(int value) {
            this.value = value;
        }
    }

    /** Retained geometry in device-local, suballocated buffers; indices are optional uint32 values. */
    public Mesh uploadMesh(ByteBuffer vertices, ByteBuffer indices, PrimitiveTopology topology) {
        ensureOpen();
        java.util.Objects.requireNonNull(topology, "topology");
        int vertexCount = vertexCount(vertices);
        if (indices != null) {
            if (indices.remaining() == 0 || indices.remaining() % 4 != 0) {
                throw new IllegalArgumentException("Indices must contain complete uint32 values");
            }
            ByteBuffer validation = indices.duplicate().order(ByteOrder.nativeOrder());
            while (validation.hasRemaining()) {
                if (Integer.toUnsignedLong(validation.getInt()) >= vertexCount) {
                    throw new IllegalArgumentException("Index exceeds the vertex allocation");
                }
            }
        }
        VulkanArena.Slice vertexSlice = geometry.allocate(vertices.remaining(), VERTEX_STRIDE);
        VulkanArena.Slice indexSlice = null;
        try {
            queueBufferUpload(vertexSlice, 0, vertices);
            if (indices != null) {
                indexSlice = geometry.allocate(indices.remaining(), 4);
                queueBufferUpload(indexSlice, 0, indices);
            }
            Mesh mesh = new Mesh(
                vertexSlice, indexSlice, indices == null ? vertexCount : indices.remaining() / 4, topology.value, true);
            meshes.add(mesh);
            return mesh;
        } catch (RuntimeException | Error error) {
            retire(vertexSlice::close);
            if (indexSlice != null) {
                var failed = indexSlice;
                retire(failed::close);
            }
            throw error;
        }
    }

    public Mesh uploadQuads(ByteBuffer vertices) {
        return uploadQuads(List.of(vertices), vertexCount(vertices));
    }

    /** Copies chunk-builder blocks directly into a single retained allocation. */
    public Mesh uploadQuads(List<ByteBuffer> blocks, int vertexCount) {
        ensureOpen();
        if (vertexCount <= 0 || vertexCount % 4 != 0) {
            throw new IllegalArgumentException("Quads require complete groups of four vertices");
        }
        long bytes = Math.multiplyExact((long) vertexCount, VERTEX_STRIDE);
        long actualBytes = 0;
        for (ByteBuffer block : blocks) {
            if (block.remaining() % VERTEX_STRIDE != 0) {
                throw new IllegalArgumentException("Chunk blocks must contain complete vertices");
            }
            actualBytes = Math.addExact(actualBytes, block.remaining());
        }
        if (actualBytes != bytes) {
            throw new IllegalArgumentException("Vertex count does not match chunk blocks");
        }
        VulkanArena.Slice vertices = geometry.allocate(bytes, VERTEX_STRIDE);
        try {
            long offset = 0;
            for (ByteBuffer block : blocks) {
                if (block.hasRemaining()) {
                    queueBufferUpload(vertices, offset, block);
                }
                offset += block.remaining();
            }
            Mesh mesh = new Mesh(vertices, quadIndices(vertexCount), Math.multiplyExact(vertexCount / 4, 6),
                VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST, false);
            meshes.add(mesh);
            return mesh;
        } catch (RuntimeException | Error error) {
            retire(vertices::close);
            throw error;
        }
    }

    public Mesh uploadWorldQuads(List<ByteBuffer> blocks, int vertexCount, int atlasId) {
        return uploadWorldQuads(blocks, vertexCount, atlasId, false);
    }

    public Mesh uploadWorldQuads(List<ByteBuffer> blocks, int vertexCount, int atlasId, boolean translucent) {
        if (atlasId < 0 || atlasId >= 4) throw new IllegalArgumentException("Invalid terrain atlas");
        return uploadQuads(blocks, vertexCount);
    }

    public void drawPhase(RenderPhase phase) {
        requireFrame();
        recording.phase = java.util.Objects.requireNonNull(phase);
    }

    /** Compatibility wrapper; native collection records StreamGeometry directly. */
    public Mesh streamQuads(ByteBuffer vertices) {
        return compatibilityMesh(streamQuadsPacket(vertices));
    }

    /** Compatibility wrapper for inherited primitive submission. */
    public Mesh streamMesh(ByteBuffer vertices, ByteBuffer indices, PrimitiveTopology topology) {
        return compatibilityMesh(streamMeshPacket(vertices, indices, topology));
    }

    /** Writable mapped storage owned by the current asynchronous frame slot. */
    public StreamReservation reserveStream(int vertexCapacity) {
        requireFrame();
        if (vertexCapacity <= 0) {
            throw new IllegalArgumentException("Stream capacity must be positive");
        }
        return new StreamReservation(current, current.serial,
            current.stream.reserve(Math.multiplyExact(vertexCapacity, VERTEX_STRIDE), VERTEX_STRIDE));
    }

    public StreamGeometry streamQuadsPacket(ByteBuffer vertices) {
        int count = vertexCount(vertices);
        if (count % 4 != 0) {
            throw new IllegalArgumentException("Quads require complete groups of four vertices");
        }
        var reservation = reserveStream(count);
        reservation.vertices().put(vertices.duplicate());
        return reservation.quads(count);
    }

    public StreamGeometry streamMeshPacket(ByteBuffer vertices, ByteBuffer indices, PrimitiveTopology topology) {
        int count = vertexCount(vertices);
        validateIndices(indices, count);
        java.util.Objects.requireNonNull(topology);
        var reservation = reserveStream(count);
        reservation.vertices().put(vertices.duplicate());
        if (indices == null) {
            return reservation.mesh(count, topology);
        }
        var indexData = current.stream.reserve(indices.remaining(), Integer.BYTES);
        indexData.mapping().put(indices.duplicate());
        return reservation.finish(count, indexData.buffer(), Math.toIntExact(indexData.offset() / Integer.BYTES),
            indices.remaining() / Integer.BYTES, topology.value);
    }

    public final class StreamReservation {
        private final FrameResources frame;
        private final long serial;
        private final VulkanStreamArena.Allocation allocation;
        private boolean sealed;

        private StreamReservation(FrameResources frame, long serial, VulkanStreamArena.Allocation allocation) {
            this.frame = frame;
            this.serial = serial;
            this.allocation = allocation;
        }

        public int vertexCapacity() {
            return allocation.bytes() / VERTEX_STRIDE;
        }

        public void checkWritable() {
            requireWritable();
        }

        /** Do not retain or write this mapping after finalizing its packet or ending its frame. */
        public ByteBuffer vertices() {
            requireWritable();
            return allocation.mapping().duplicate().order(ByteOrder.nativeOrder());
        }

        public StreamGeometry quads(int vertexCount) {
            requireWritable();
            if (vertexCount <= 0 || vertexCount % 4 != 0 || vertexCount > vertexCapacity()) {
                throw new IllegalArgumentException("Quads exceed the reservation or have incomplete groups");
            }
            var indices = quadIndices(vertexCount);
            return finish(vertexCount, indices.handle(), Math.toIntExact(indices.offset() / Integer.BYTES),
                Math.multiplyExact(vertexCount / 4, 6), VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST);
        }

        public StreamGeometry mesh(int vertexCount, PrimitiveTopology topology) {
            java.util.Objects.requireNonNull(topology);
            return finish(vertexCount, VK_NULL_HANDLE, 0, vertexCount, topology.value);
        }

        private StreamGeometry finish(int vertexCount, long indices, int firstIndex, int count, int topology) {
            requireWritable();
            if (vertexCount <= 0 || vertexCount > vertexCapacity()) {
                throw new IllegalArgumentException("Stream vertex count exceeds reservation");
            }
            sealed = true;
            return new StreamGeometry(frame, serial,
                new Geometry(allocation.buffer(), indices, Math.toIntExact(allocation.offset() / VERTEX_STRIDE),
                    firstIndex, count, topology),
                vertexCount * VERTEX_STRIDE);
        }

        private void requireWritable() {
            requireFrame();
            if (sealed || frame != current || serial != frame.serial) {
                throw new IllegalStateException("Stream reservation is no longer writable");
            }
        }
    }

    /** Immutable page-relative draw packet. Valid only during its owning frame. */
    public final class StreamGeometry {
        private final VulkanRenderer owner = VulkanRenderer.this;
        private final FrameResources frame;
        private final long serial;
        private final Geometry geometry;
        private final int vertexBytes;

        private StreamGeometry(FrameResources frame, long serial, Geometry geometry, int vertexBytes) {
            this.frame = frame;
            this.serial = serial;
            this.geometry = geometry;
            this.vertexBytes = vertexBytes;
        }

        public int vertexBytes() {
            return vertexBytes;
        }
    }

    private Mesh compatibilityMesh(StreamGeometry stream) {
        Mesh mesh = new Mesh(stream);
        meshes.add(mesh);
        current.transientMeshes.add(mesh);
        return mesh;
    }

    private void requireFrame() {
        ensureOpen();
        if (recording == null) {
            throw new IllegalStateException("beginFrame must precede streamed geometry");
        }
    }

    private static void validateIndices(ByteBuffer indices, int count) {
        if (indices == null) {
            return;
        }
        if (indices.remaining() == 0 || indices.remaining() % 4 != 0) {
            throw new IllegalArgumentException("Incomplete uint32 index records");
        }
        var validation = indices.duplicate().order(ByteOrder.nativeOrder());
        while (validation.hasRemaining()) {
            if (Integer.toUnsignedLong(validation.getInt()) >= count) {
                throw new IllegalArgumentException("Index exceeds vertex allocation");
            }
        }
    }

    private static int vertexCount(ByteBuffer vertices) {
        if (vertices.remaining() == 0 || vertices.remaining() % VERTEX_STRIDE != 0) {
            throw new IllegalArgumentException("Vertices must contain complete 32-byte records");
        }
        return vertices.remaining() / VERTEX_STRIDE;
    }

    private VulkanArena.Slice quadIndices(int vertices) {
        long bytes = Math.multiplyExact((long) vertices / 4, 6 * Integer.BYTES);
        for (var indices : sharedQuadIndices) {
            if (indices.size() >= bytes) {
                return indices;
            }
        }
        int quads = Integer.highestOneBit(Math.max(vertices / 4 - 1, 255)) << 1;
        ByteBuffer indices = MemoryUtil.memAlloc(Math.multiplyExact(quads, 24)).order(ByteOrder.nativeOrder());
        try {
            for (int q = 0; q < quads; q++) {
                int i = q * 4;
                indices.putInt(i).putInt(i + 1).putInt(i + 2).putInt(i).putInt(i + 2).putInt(i + 3);
            }
            indices.flip();
            var allocation = geometry.allocate(indices.remaining(), 4);
            queueBufferUpload(allocation, 0, indices);
            sharedQuadIndices.add(allocation);
            return allocation;
        } finally {
            MemoryUtil.memFree(indices);
        }
    }

    private void queueBufferUpload(VulkanArena.Slice target, long targetOffset, ByteBuffer bytes) {
        int byteCount = bytes.remaining();
        if (context.memoryProfile().unified() && target.hostCoherent()) {
            target.pageUpload(targetOffset, bytes);
            uploadedBytes += byteCount;
            return;
        }
        VulkanArena.Slice source = stage(bytes);
        uploads.buffer(source, target, targetOffset, byteCount);
    }

    private VulkanArena.Slice stage(ByteBuffer bytes) {
        uploadedBytes += bytes.remaining();
        var source = staging.allocate(bytes.remaining(), 4);
        try {
            source.upload(bytes);
            uploads.retain(source);
            return source;
        } catch (RuntimeException | Error error) {
            source.close();
            throw error;
        }
    }

    /** Upload a single mip level with the default nearest/repeat sampler. */
    public Texture uploadTexture(int width, int height, ByteBuffer rgba) {
        return uploadTexture(width, height, 1, rgba, null, SamplerState.NEAREST_REPEAT);
    }

    public Texture uploadTexture(int width, int height, int mipLevels, ByteBuffer rgba, List<ByteBuffer> suppliedLevels,
        SamplerState samplerState) {
        ensureOpen();
        return new Texture(textures.upload(width, height, mipLevels, rgba, suppliedLevels, samplerState));
    }

    public record TextureRegion(int level, int x, int y, int width, int height, ByteBuffer rgba, int rowStrideBytes) {
        public TextureRegion(int level, int x, int y, int width, int height, ByteBuffer rgba) {
            this(level, x, y, width, height, rgba, Math.multiplyExact(width, 4));
        }
    }

    public Texture withSampler(Texture source, SamplerState samplerState) {
        requireTexture(source);
        return new Texture(textures.withSampler(source.nativeTexture, samplerState));
    }

    public Texture withSampler(Texture source, SamplerState samplerState, int baseLevel, int maxLevel) {
        requireTexture(source);
        return new Texture(textures.withSampler(source.nativeTexture, samplerState, baseLevel, maxLevel));
    }

    /** Queue region updates, preserving image snapshots already referenced by queued draws. */
    public Texture updateTexture(Texture source, List<TextureRegion> regions, boolean regenerateMipmaps) {
        return updateTexture(source, regions, regenerateMipmaps, false);
    }

    public Texture updateTexture(
        Texture source, List<TextureRegion> regions, boolean regenerateMipmaps, boolean preserveSource) {
        requireTexture(source);
        boolean usedByRecording = recording != null && recording.usesImage(source.nativeTexture.image());
        return new Texture(
            textures.update(source.nativeTexture, regions, regenerateMipmaps, preserveSource || usedByRecording, frameSubmissions));
    }

    private void requireTexture(Texture texture) {
        ensureOpen();
        if (texture == null || texture.owner != this) {
            throw new IllegalArgumentException("Texture belongs to another renderer");
        }
        textures.requireTexture(texture.nativeTexture);
    }

    public void beginFrame(int width, int height) {
        beginFrame(width, height, AttachmentIntent.PRESERVE);
    }

    /** Final attachment contents available to subsequent frames, presentation and readback. */
    public record AttachmentRetention(boolean color, boolean depth) {
        public static final AttachmentRetention ALL = new AttachmentRetention(true, true);
        public static final AttachmentRetention COLOR_ONLY = new AttachmentRetention(true, false);
    }

    public void beginFrame(int width, int height, AttachmentIntent intent) {
        beginFrame(width, height, intent, AttachmentRetention.ALL);
    }

    public void beginFrame(int width, int height, AttachmentIntent intent, AttachmentRetention retention) {
        ensureOpen();
        java.util.Objects.requireNonNull(intent);
        java.util.Objects.requireNonNull(retention);
        if (recording != null) {
            throw new IllegalStateException("The previous Vulkan frame is still open");
        }
        pixelBytes(width, height);
        current = frames[nextFrame];
        nextFrame = (nextFrame + 1) % frames.length;
        current.slot.await();
        current.stream.reset();
        current.serial++;
        if (current.targets.width != width || current.targets.height != height) {
            // Presentation may still read an old offscreen image after its render fence signals.
            context.waitIdle();
            resize(width, height);
        }
        recording = new FrameRecording(width, height, intent, retention);
    }

    private static <K, V> Map<K, V> boundedCache(int limit) {
        return new java.util.LinkedHashMap<K, V>(16, .75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> entry) {
                return size() > limit;
            }
        };
    }

    /** Packed immutable camera/environment data, reusable by all objects in the view. */
    public final class PreparedView {
        private final VulkanRenderer owner = VulkanRenderer.this;
        private final FrameView source;
        private final VulkanDrawConstants.Block constants;

        private PreparedView(FrameView source) {
            this.source = source;
            constants = VulkanDrawConstants.view(source);
        }
    }

    /** Packed immutable material policy; per-object UV transforms stay with object data. */
    public final class PreparedMaterial {
        private final VulkanRenderer owner = VulkanRenderer.this;
        private final MaterialState source;
        private final VulkanDrawConstants.Block constants;

        private PreparedMaterial(MaterialState source) {
            this.source = source;
            constants = VulkanDrawConstants.material(source);
        }
    }

    public PreparedView prepareView(FrameView view) {
        ensureOpen();
        return preparedViews.computeIfAbsent(java.util.Objects.requireNonNull(view), PreparedView::new);
    }

    public PreparedMaterial prepareMaterial(MaterialState material) {
        ensureOpen();
        return preparedMaterials.computeIfAbsent(java.util.Objects.requireNonNull(material), PreparedMaterial::new);
    }

    /** Freeze object state once; immutable view and material preparation is cached independently. */
    public PreparedPass preparePass(DrawParameters state) {
        return preparePass(
            prepareView(state.view()), state.object(), prepareMaterial(state.material()), state.raster());
    }

    public PreparedPass preparePass(
        PreparedView view, ObjectData object, PreparedMaterial material, RasterState raster) {
        ensureOpen();
        if (view == null || view.owner != this || material == null || material.owner != this) {
            throw new IllegalArgumentException("Prepared draw data belongs to another renderer");
        }
        var key = VulkanPipelines.key(0, raster,
            VulkanPipelines.Variant.of(material.source.lighting(), view.source.fog().mode() != FogMode.NONE));
        var constants = new VulkanDrawConstants.Blocks(view.constants,
            VulkanDrawConstants.object(view.source, object, material.source.lighting()), material.constants);
        preparedPasses++;
        return new PreparedPass(key, constants, object.model(), view.source, raster.viewport(), raster.scissor(), raster.minDepth(),
            raster.maxDepth(), raster.lineWidth(), raster.depthBias().factor(), raster.depthBias().units(),
            raster.blend().constants().copyArray());
    }

    /** Loading/option-change hook for exact native policies beyond the canonical recipe warmup. */
    public void warmup(DrawParameters parameters, PrimitiveTopology topology) {
        ensureOpen();
        java.util.Objects.requireNonNull(topology);
        pipelines.get(VulkanPipelines.key(topology.value, parameters.raster(),
            VulkanPipelines.Variant.of(
                parameters.material().lighting(), parameters.view().fog().mode() != FogMode.NONE)));
    }

    public void draw(Mesh mesh, Texture texture, DrawParameters state) {
        draw(mesh, texture, preparePass(state));
    }

    public void draw(Mesh mesh, Texture texture, PreparedPass pass) {
        enqueueDraw(mesh, texture, pass, false);
    }

    /** Consume a live collection pin, including a mesh its owner closed after collection. */
    public void draw(Mesh.Pin pin, Texture texture, PreparedPass pass) {
        if (pin == null || pin.closed) {
            throw new IllegalArgumentException("Mesh collection pin is closed");
        }
        enqueueDraw(pin.mesh, texture, pass, true);
    }

    private void enqueueDraw(Mesh mesh, Texture texture, PreparedPass pass, boolean pinned) {
        ensureOpen();
        if (recording == null) {
            throw new IllegalStateException("beginFrame must precede draws");
        }
        if (mesh == null || !meshes.contains(mesh) || mesh.closed || !pinned && mesh.closeRequested
            || !mesh.validFrame()) {
            throw new IllegalArgumentException("Mesh is closed or belongs to another renderer");
        }
        enqueueGeometry(mesh.geometry, texture, pass);
    }

    public void draw(StreamGeometry stream, Texture texture, PreparedPass pass) {
        requireFrame();
        if (stream == null || stream.owner != this || stream.frame != current || stream.serial != current.serial) {
            throw new IllegalArgumentException("Stream geometry belongs to another frame");
        }
        enqueueGeometry(stream.geometry, texture, pass);
    }

    private void enqueueGeometry(Geometry geometry, Texture texture, PreparedPass pass) {
        requireTexture(texture);
        if (pass == null || pass.owner != this) {
            throw new IllegalArgumentException("Pass belongs to another renderer");
        }
        PassEntry entry = recording.activePasses.get(pass);
        if (entry == null) {
            int viewIndex =
                recording.viewBlocks.computeIfAbsent(pass.constants.view(), ignored -> recording.viewBlocks.size());
            int objectIndex = recording.objectBlocks.computeIfAbsent(
                pass.constants.object(), ignored -> recording.objectBlocks.size());
            int materialIndex = recording.materialBlocks.computeIfAbsent(
                pass.constants.material(), ignored -> recording.materialBlocks.size());
            entry = new PassEntry(pass, viewIndex, objectIndex, materialIndex,
                pass.viewport == null ? new Rect(0, 0, recording.width, recording.height) : pass.viewport);
            recording.activePasses.put(pass, entry);
        }
        recording.commands.add(new Draw(geometry, texture, pass.pipeline(geometry.topology), entry,
            recording.phase, false));
        recording.images.add(texture.nativeTexture.image());
    }

    /** Immutable packed pass constants and a normalized Vulkan pipeline recipe. */
    public final class PreparedPass {
        private final VulkanRenderer owner = VulkanRenderer.this;
        private final VulkanPipelines.Key key;
        private final VulkanDrawConstants.Blocks constants;
        private final Matrix4 model;
        private final FrameView view;
        private final Rect viewport;
        private final Rect scissor;
        private final float minDepth;
        private final float maxDepth;
        private final float lineWidth;
        private final float polygonOffsetFactor;
        private final float polygonOffsetUnits;
        private final float[] blendConstants;
        private final long[] topologies = new long[24];

        private PreparedPass(VulkanPipelines.Key key, VulkanDrawConstants.Blocks constants, Matrix4 model, FrameView view, Rect viewport, Rect scissor,
            float minDepth, float maxDepth, float lineWidth, float polygonOffsetFactor, float polygonOffsetUnits,
            float[] blendConstants) {
            this.key = key;
            this.constants = constants;
            this.model = model;
            this.view = view;
            this.viewport = viewport;
            this.scissor = scissor;
            this.minDepth = minDepth;
            this.maxDepth = maxDepth;
            this.lineWidth = lineWidth;
            this.polygonOffsetFactor = polygonOffsetFactor;
            this.polygonOffsetUnits = polygonOffsetUnits;
            this.blendConstants = blendConstants;
        }

        private long pipeline(int topology) {
            if (topology < 0 || topology >= 4) throw new IllegalArgumentException("Unsupported primitive topology");
            long pipeline = topologies[topology];
            if (pipeline == VK_NULL_HANDLE) {
                pipeline = pipelines.get(new VulkanPipelines.Key(topology, key.depthTest(), key.depthWrite(),
                    key.depthFunc(), key.blend(), key.blendSrc(), key.blendDst(), key.cullMode(), key.frontFace(),
                    key.colorMask(), key.depthBias(), key.variant()));
                topologies[topology] = pipeline;
            }
            return pipeline;
        }
    }

    public void clear(Color4 color, float depth) {
        clear(ClearRequest.all(color, depth));
    }

    public void clear(ClearRequest request) {
        ensureOpen();
        if (recording == null) {
            throw new IllegalStateException("beginFrame must precede clears");
        }
        boolean color = request.clearColor() && request.colorMask().bits() != 0;
        if (color && !request.colorMask().equals(ColorMask.RGBA)) {
            maskedColorClear(request.color(), request.colorMask().bits(), request.region());
            color = false;
        }
        if (color || request.clearDepth()) {
            recording.commands.add(
                new Clear(color, request.clearDepth(), request.color(), request.depth(), request.region()));
        }
    }

    private void maskedColorClear(Color4 color, int mask, Rect scissor) {
        if (clearMesh == null) {
            ByteBuffer vertices = MemoryUtil.memCalloc(VERTEX_STRIDE * 4).order(ByteOrder.nativeOrder());
            ByteBuffer pixel = MemoryUtil.memAlloc(4);
            try {
                float[] coordinates = {-1, -1, 1, -1, 1, 1, -1, 1};
                for (int i = 0; i < 4; i++) {
                    VertexFormat.write(
                        vertices, i * VERTEX_STRIDE, coordinates[i * 2], coordinates[i * 2 + 1], 0, 0, 0, -1, 0);
                }
                clearMesh = uploadQuads(vertices);
                pixel.putInt(-1).flip();
                clearTexture = uploadTexture(1, 1, pixel);
            } finally {
                MemoryUtil.memFree(vertices);
                MemoryUtil.memFree(pixel);
            }
        }
        VulkanPipelines.Key key = new VulkanPipelines.Key(VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST, false, false,
            VK_COMPARE_OP_ALWAYS, false, VK_BLEND_FACTOR_ONE, VK_BLEND_FACTOR_ZERO, VK_CULL_MODE_NONE,
            VK_FRONT_FACE_COUNTER_CLOCKWISE, mask, false, VulkanPipelines.Variant.UNLIT);
        var constants = VulkanDrawConstants.clear(color);
        draw(clearMesh, clearTexture,
            new PreparedPass(key, constants, Matrix4.identity(), FrameView.ui(recording.width, recording.height), new Rect(0, 0, recording.width, recording.height), scissor, 0, 1, 1, 0, 0,
                new float[4]));
    }

    /** Submit asynchronously; the returned offscreen image is owned by this frame slot. */
    public GpuFrame finishFrame() {
        requireFrame();
        FrameRecording frame = recording;
        try {
            prepareUniforms(frame);
            AttachmentPlan plan = frame.planAttachments(lastFrame);
            VkCommandBuffer command = current.slot.begin();
            try (var stack = MemoryStack.stackPush()) {
                uploads.record(command, stack);
                preserveAttachments(command, plan);
                recordRenderPass(command, frame, plan, stack, false, plan.consumedClears, frame.commands.size());
            }
            submitFrame(frame);
            return new GpuFrame(frame.width, frame.height, current.targets.color.handle);
        } catch (RuntimeException | Error error) {
            context.waitIdle();
            throw error;
        } finally {
            recording = null;
        }
    }

    private void recordRenderPass(
        VkCommandBuffer command, FrameRecording frame, AttachmentPlan plan, MemoryStack stack,
        boolean hdr, int start, int end) {
        recordRenderPass(command, frame, plan, stack, hdr, start, end, current.targets);
    }

    private void recordRenderPass(
        VkCommandBuffer command, FrameRecording frame, AttachmentPlan plan, MemoryStack stack,
        boolean hdr, int start, int end, VulkanFrameTargets targets) {
        var initialClear = VkClearValue.calloc(2, stack);
        Color4 color = plan.clearColor;
        initialClear.get(0).color().float32(0, color.r()).float32(1, color.g())
            .float32(2, color.b()).float32(3, color.a());
        initialClear.get(1).depthStencil().depth(clamp(plan.clearDepth)).stencil(0);
        long frameRenderPass = renderPasses.get(plan.color, plan.depth, frame.retention.color(), frame.retention.depth());
        var pass = VkRenderPassBeginInfo.calloc(stack).sType$Default().renderPass(frameRenderPass)
            .framebuffer(targets.framebuffer).pClearValues(initialClear);
        pass.renderArea().offset().set(0, 0);
        pass.renderArea().extent().set(targets.width, targets.height);
        vkCmdBeginRenderPass(command, pass, VK_SUBPASS_CONTENTS_INLINE);
        var encoder = new DrawEncoder(command, frame, stack, false, false, targets.width, targets.height);
        for (int index = start; index < end; index++) {
            Command item = frame.commands.get(index);
            if (item instanceof Clear clear) recordClear(command, clear, stack, false, targets.width, targets.height);
            else encoder.record((Draw) item);
        }
        vkCmdEndRenderPass(command);
    }

    private void submitFrame(FrameRecording frame) {
        current.slot.submit();
        frameSubmissions++;
        lastSubmission = current.slot;
        lastFrame = current;
        current.retention = frame.retention;
        uploads.submitted(current.slot);
        for (Mesh mesh : current.transientMeshes) {
            mesh.close();
        }
        current.transientMeshes.clear();
        for (Runnable action : pendingRetirements) {
            current.slot.defer(action);
        }
        pendingRetirements.clear();
    }

    /** Binding state and stack scratch storage live for exactly one render pass. */
    private final class DrawEncoder {
        private final VkCommandBuffer command;
        private final VkViewport.Buffer viewport;
        private final VkRect2D.Buffer scissor;
        private final LongBuffer handle;
        private final LongBuffer zeroOffset;
        private final java.nio.IntBuffer dynamicOffset;
        private final java.nio.FloatBuffer blendConstants;
        private final ByteBuffer temporalConstants;
        private final boolean jitterWorld;
        private final FrameRecording frame;
        private final boolean capture;
        private final Rect fullRect;
        private final int targetWidth;
        private final int targetHeight;
        private long boundPipeline;
        private long boundVertex;
        private long boundIndex;
        private long boundTexture;
        private PreparedPass boundPass;
        private Rect boundViewport;
        private Rect boundScissor;
        private int boundView = -1;
        private int boundObject = -1;
        private int boundMaterial = -1;
        private boolean boundRayScene;

        DrawEncoder(VkCommandBuffer command, FrameRecording frame, MemoryStack stack, boolean capture, boolean jitterWorld,
                    int targetWidth, int targetHeight) {
            this.command = command;
            this.frame = frame;
            this.capture = capture;
            this.jitterWorld = jitterWorld;
            this.targetWidth = targetWidth;
            this.targetHeight = targetHeight;
            viewport = VkViewport.calloc(1, stack);
            scissor = VkRect2D.calloc(1, stack);
            handle = stack.mallocLong(1);
            zeroOffset = stack.longs(0L);
            dynamicOffset = stack.mallocInt(1);
            blendConstants = stack.mallocFloat(4);
            temporalConstants = stack.calloc(64);
            fullRect = new Rect(0, 0, targetWidth, targetHeight);
        }

        void record(Draw draw) {
            PreparedPass pass = draw.entry.pass;
            Rect requestedViewport = scaledRect(draw.entry.viewport, frame.width, frame.height, targetWidth, targetHeight);
            Rect requestedScissor = pass.scissor == null ? fullRect
                : scaledRect(pass.scissor, frame.width, frame.height, targetWidth, targetHeight);
            if (requestedViewport.width() <= 0 || requestedViewport.height() <= 0
                || !setRect(scissor.get(0), requestedScissor, targetWidth, targetHeight)) {
                return;
            }
            bindDynamicState(pass, requestedViewport, requestedScissor);
            long pipeline = draw.pipeline;
            if (boundPipeline != pipeline) {
                vkCmdBindPipeline(command, VK_PIPELINE_BIND_POINT_GRAPHICS, pipeline);
                pipelineBinds++;
                boundPipeline = pipeline;
            }
            bindGeometry(draw.geometry);
            bindDescriptors(draw);
            Geometry geometry = draw.geometry;
            temporalConstants.putFloat(0, jitterWorld ? frame.jitterX : 0).putFloat(4, jitterWorld ? frame.jitterY : 0)
                .putFloat(8, draw.phase == RenderPhase.TERRAIN_OPAQUE && !pass.key.blend() ? 0 : 1)
                .putFloat(12, jitterWorld ? 1 : 0);
            vkCmdPushConstants(command, bindings.pipelineLayout(), VK_SHADER_STAGE_VERTEX_BIT | VK_SHADER_STAGE_FRAGMENT_BIT,
                64, temporalConstants);
            if (geometry.indices != VK_NULL_HANDLE) {
                vkCmdDrawIndexed(command, geometry.count, 1, geometry.firstIndex, geometry.firstVertex, 0);
            } else {
                vkCmdDraw(command, geometry.count, 1, geometry.firstVertex, 0);
            }
        }

        private void bindDynamicState(PreparedPass pass, Rect requestedViewport, Rect requestedScissor) {
            if (!requestedViewport.equals(boundViewport) || boundPass == null || pass.minDepth != boundPass.minDepth
                || pass.maxDepth != boundPass.maxDepth) {
                viewport.x(requestedViewport.x())
                    .y(requestedViewport.y())
                    .width(requestedViewport.width())
                    .height(requestedViewport.height())
                    .minDepth(clamp(pass.minDepth))
                    .maxDepth(clamp(pass.maxDepth));
                vkCmdSetViewport(command, 0, viewport);
                dynamicStateWrites++;
                boundViewport = requestedViewport;
            }
            if (!requestedScissor.equals(boundScissor)) {
                vkCmdSetScissor(command, 0, scissor);
                dynamicStateWrites++;
                boundScissor = requestedScissor;
            }
            if (boundPass == null || pass.lineWidth != boundPass.lineWidth) {
                float lineWidth = wideLines ? Math.max(minLineWidth, Math.min(maxLineWidth, pass.lineWidth)) : 1;
                vkCmdSetLineWidth(command, lineWidth);
                dynamicStateWrites++;
            }
            if (boundPass == null || pass.polygonOffsetUnits != boundPass.polygonOffsetUnits
                || pass.polygonOffsetFactor != boundPass.polygonOffsetFactor) {
                vkCmdSetDepthBias(command, pass.polygonOffsetUnits, 0, pass.polygonOffsetFactor);
                dynamicStateWrites++;
            }
            if (boundPass == null || !java.util.Arrays.equals(pass.blendConstants, boundPass.blendConstants)) {
                blendConstants.clear().put(pass.blendConstants).flip();
                vkCmdSetBlendConstants(command, blendConstants);
                dynamicStateWrites++;
            }
            boundPass = pass;
        }

        private void bindGeometry(Geometry geometry) {
            if (boundVertex != geometry.vertices) {
                handle.put(0, geometry.vertices);
                vkCmdBindVertexBuffers(command, 0, handle, zeroOffset);
                boundVertex = geometry.vertices;
            }
            if (geometry.indices != VK_NULL_HANDLE && boundIndex != geometry.indices) {
                vkCmdBindIndexBuffer(command, geometry.indices, 0, VK_INDEX_TYPE_UINT32);
                boundIndex = geometry.indices;
            }
        }

        private void bindDescriptors(Draw draw) {
            if (boundView != draw.entry.viewIndex) {
                bindUniform(VulkanBindings.VIEW, current.viewUniforms, draw.entry.viewIndex);
                boundView = draw.entry.viewIndex;
            }
            if (boundObject != draw.entry.objectIndex) {
                bindUniform(VulkanBindings.OBJECT, current.objectUniforms, draw.entry.objectIndex);
                boundObject = draw.entry.objectIndex;
            }
            if (boundMaterial != draw.entry.materialIndex) {
                bindUniform(VulkanBindings.MATERIAL, current.materialUniforms, draw.entry.materialIndex);
                boundMaterial = draw.entry.materialIndex;
            }
            long texture = draw.texture.nativeTexture.descriptor();
            if (boundTexture != texture) {
                handle.put(0, texture);
                vkCmdBindDescriptorSets(command, VK_PIPELINE_BIND_POINT_GRAPHICS, bindings.pipelineLayout(),
                    VulkanBindings.TEXTURE, handle, null);
                descriptorBinds++;
                boundTexture = texture;
            }
        }

        private void bindUniform(int set, VulkanUniformStorage uniforms, int index) {
            handle.put(0, uniforms.descriptor);
            dynamicOffset.put(0, Math.multiplyExact(index, uniforms.stride));
            vkCmdBindDescriptorSets(
                command, VK_PIPELINE_BIND_POINT_GRAPHICS, bindings.pipelineLayout(), set, handle, dynamicOffset);
            descriptorBinds++;
        }
    }

    private record AttachmentPlan(AttachmentIntent.LoadOp color, AttachmentIntent.LoadOp depth, Color4 clearColor,
        float clearDepth, int consumedClears) { }

    /** CPU command collection ends at submission; GPU resources belong to reusable FrameResources. */
    private static final class FrameRecording {
        final int width;
        final int height;
        final AttachmentIntent intent;
        final AttachmentRetention retention;
        final List<Command> commands = new ArrayList<>();
        final Map<PreparedPass, PassEntry> activePasses = new IdentityHashMap<>();
        final Map<VulkanDrawConstants.Block, Integer> viewBlocks = new LinkedHashMap<>();
        final Map<VulkanDrawConstants.Block, Integer> objectBlocks = new LinkedHashMap<>();
        final Map<VulkanDrawConstants.Block, Integer> materialBlocks = new LinkedHashMap<>();
        final Set<VulkanImage> images = java.util.Collections.newSetFromMap(new IdentityHashMap<>());
        final Map<VulkanTextures.Texture, int[]> textureChanges = new IdentityHashMap<>();
        RenderPhase phase = RenderPhase.PREPARE;
        int uiStart = -1;
        int uiWorldEnd = -1;
        int handsStart = -1;
        boolean jitterWorld;
        float jitterX, jitterY;

        FrameRecording(int width, int height, AttachmentIntent intent, AttachmentRetention retention) {
            this.width = width;
            this.height = height;
            this.intent = intent;
            this.retention = retention;
        }

        boolean usesImage(VulkanImage image) {
            return images.contains(image);
        }

        AttachmentPlan planAttachments(FrameResources previous) {
            AttachmentPlan plan = foldInitialClears();
            if (previous != null && previous.targets.width == width && previous.targets.height == height) {
                if (plan.color == AttachmentIntent.LoadOp.LOAD && !previous.retention.color()) {
                    throw new IllegalStateException("Cannot load a color attachment discarded by the previous frame");
                }
                if (plan.depth == AttachmentIntent.LoadOp.LOAD && !previous.retention.depth()) {
                    throw new IllegalStateException("Cannot load a depth attachment discarded by the previous frame");
                }
            }
            return plan;
        }

        private AttachmentPlan foldInitialClears() {
            AttachmentIntent.LoadOp color = intent.color();
            AttachmentIntent.LoadOp depth = intent.depth();
            Color4 clearColor = intent.clearColor();
            float clearDepth = intent.clearDepth();
            int consumed = 0;
            // Only consecutive full clears become load operations. Masked/scissored clears stay ordered.
            for (Command item : commands) {
                
                if (!(item instanceof Clear clear) || clear.scissor != null) {
                    break;
                }
                if (clear.color) {
                    color = AttachmentIntent.LoadOp.CLEAR;
                    clearColor = clear.value;
                }
                if (clear.depth) {
                    depth = AttachmentIntent.LoadOp.CLEAR;
                    clearDepth = clear.depthValue;
                }
                consumed++;
            }
            return new AttachmentPlan(color, depth, clearColor, clearDepth, consumed);
        }
    }

    private void preserveAttachments(VkCommandBuffer command, AttachmentPlan plan) {
        if (lastFrame != null && lastFrame != current) {
            current.targets.preserveFrom(command, lastFrame.targets, plan.color == AttachmentIntent.LoadOp.LOAD,
                plan.depth == AttachmentIntent.LoadOp.LOAD, false);
        }
    }

    private void prepareUniforms(FrameRecording frame) {
        current.viewUniforms.write(frame.viewBlocks);
        current.objectUniforms.write(frame.objectBlocks);
        current.materialUniforms.write(frame.materialBlocks);
        uniformWrites += frame.objectBlocks.size();
        viewWrites += frame.viewBlocks.size();
        materialWrites += frame.materialBlocks.size();
    }

    private void recordClear(VkCommandBuffer command, Clear clear, MemoryStack stack, boolean hdr,
                             int targetWidth, int targetHeight) {
        VkClearRect.Buffer rect = VkClearRect.calloc(1, stack).baseArrayLayer(0).layerCount(1);
        Rect area = clear.scissor == null ? null
            : scaledRect(clear.scissor, recording.width, recording.height, targetWidth, targetHeight);
        if (!setRect(rect.rect(), area, targetWidth, targetHeight)) {
            return;
        }
        int count = (clear.color ? (hdr ? 2 : 1) : 0) + (clear.depth ? 1 : 0);
        VkClearAttachment.Buffer attachments = VkClearAttachment.calloc(count, stack);
        int index = 0;
        if (clear.color) {
            VkClearAttachment attachment =
                attachments.get(index++).aspectMask(VK_IMAGE_ASPECT_COLOR_BIT).colorAttachment(0);
            Color4 value = clear.value;
            attachment.clearValue()
                .color()
                .float32(0, value.r())
                .float32(1, value.g())
                .float32(2, value.b())
                .float32(3, value.a());
        }
        if (clear.color && hdr) attachments.get(index++).aspectMask(VK_IMAGE_ASPECT_COLOR_BIT).colorAttachment(1)
            .clearValue().color().float32(0, 1).float32(1, 1).float32(2, 1).float32(3, 1);
        if (clear.depth) {
            attachments.get(index)
                .aspectMask(VK_IMAGE_ASPECT_DEPTH_BIT)
                .clearValue()
                .depthStencil()
                .depth(clamp(clear.depthValue))
                .stencil(0);
        }
        vkCmdClearAttachments(command, attachments, rect);
    }

    private static Rect scaledRect(Rect area, int sourceWidth, int sourceHeight, int targetWidth, int targetHeight) {
        if (sourceWidth == targetWidth && sourceHeight == targetHeight) return area;
        int x = (int) Math.floor((double) area.x() * targetWidth / sourceWidth);
        int y = (int) Math.floor((double) area.y() * targetHeight / sourceHeight);
        int endX = (int) Math.ceil(((double) area.x() + Math.max(0, area.width())) * targetWidth / sourceWidth);
        int endY = (int) Math.ceil(((double) area.y() + Math.max(0, area.height())) * targetHeight / sourceHeight);
        return new Rect(x, y, Math.max(0, endX - x), Math.max(0, endY - y));
    }

    private static boolean setRect(VkRect2D rect, Rect area, int width, int height) {
        int x = area == null ? 0 : Math.max(0, area.x());
        int y = area == null ? 0 : Math.max(0, area.y());
        int endX = area == null ? width : (int) Math.min(width, (long) area.x() + Math.max(0, area.width()));
        int endY = area == null ? height : (int) Math.min(height, (long) area.y() + Math.max(0, area.height()));
        if (endX <= x || endY <= y) {
            return false;
        }
        rect.offset().set(x, y);
        rect.extent().set(endX - x, endY - y);
        return true;
    }

    /** Explicit screenshot readback. Normal frames never allocate or copy these buffers. */
    public Frame readbackFrame() {
        ensureOpen();
        if (lastFrame == null || recording != null) {
            throw new IllegalStateException("Finish a Vulkan frame before reading it");
        }
        if (!lastFrame.retention.color() || !lastFrame.retention.depth()) {
            throw new IllegalStateException("Readback requires retained color and depth attachments");
        }
        lastFrame.slot.await();
        var targets = lastFrame.targets;
        targets.readback();
        return new Frame(targets.width, targets.height, targets.colorPixels(), targets.depthPixels());
    }

    /** Compatibility readback API; game presentation uses finishFrame instead. */
    public Frame endFrame() {
        finishFrame();
        return readbackFrame();
    }

    /** uploadBytes counts copied host bytes; uniformWrites counts unique object records written. */
    public record Statistics(long frameSubmissions, long uploadBytes, int retainedPages, int stagingPages,
        int pipelines, long preparedPasses, long uniformWrites, long viewWrites, long materialWrites,
        long pipelineBinds, long descriptorBinds, long dynamicStateWrites) { }

    public Statistics statistics() {
        return new Statistics(frameSubmissions, uploadedBytes, geometry.pageCount(), staging.pageCount(),
            pipelines.size(), preparedPasses, uniformWrites, viewWrites, materialWrites, pipelineBinds, descriptorBinds,
            dynamicStateWrites);
    }

    public record GpuFrame(int width, int height, long image) { }

    public record Frame(int width, int height, ByteBuffer rgba, ByteBuffer depth) { }

    private interface Command { }

    private record PassEntry(PreparedPass pass, int viewIndex, int objectIndex, int materialIndex, Rect viewport) { }

    private record Geometry(long vertices, long indices, int firstVertex, int firstIndex, int count, int topology) { }

    private record Draw(Geometry geometry, Texture texture, long pipeline, PassEntry entry,
                        RenderPhase phase,
                        boolean worldSegment) implements Command {}
    private record Clear(boolean color, boolean depth, Color4 value, float depthValue, Rect scissor)
        implements Command {}

    public final class Mesh implements AutoCloseable {
        private final long cacheIdentity = ++meshIdentities;
        private final VulkanArena.Slice vertices;
        private final VulkanArena.Slice indices;
        private final Geometry geometry;
        private final FrameResources frame;
        private final long serial;
        private final boolean ownsIndices;
        private boolean closeRequested;
        private boolean closed;
        private boolean released;
        private int pins;

        private Mesh(
            VulkanArena.Slice vertices, VulkanArena.Slice indices, int indexCount, int topology, boolean ownsIndices) {
            this.vertices = vertices;
            this.indices = indices;
            this.frame = null;
            this.serial = 0;
            this.geometry = new Geometry(vertices.handle(), indices == null ? VK_NULL_HANDLE : indices.handle(),
                Math.toIntExact(vertices.offset() / VERTEX_STRIDE),
                indices == null ? 0 : Math.toIntExact(indices.offset() / Integer.BYTES), indexCount, topology);
            this.ownsIndices = ownsIndices;
        }

        private Mesh(StreamGeometry stream) {
            this.vertices = null;
            this.indices = null;
            this.ownsIndices = false;
            this.geometry = stream.geometry;
            this.frame = stream.frame;
            this.serial = stream.serial;
        }

        private boolean validFrame() {
            return frame == null || recording != null && frame == current && serial == frame.serial;
        }

        public int indexCount() {
            return geometry.count;
        }

        /** Keeps a mesh valid while a CPU draw queue prepares its textures. */
        public Pin retain() {
            ensureOpen();
            if (closeRequested || released || !validFrame()) {
                throw new IllegalStateException("Mesh is closing or its frame has ended");
            }
            Pin pin = new Pin();
            pins++;
            return pin;
        }

        public final class Pin implements AutoCloseable {
            private final Mesh mesh = Mesh.this;
            private boolean closed;

            private Pin() {
            }

            @Override
            public void close() {
                if (closed) {
                    return;
                }
                closed = true;
                pins--;
                retireIfUnpinned();
            }
        }

        @Override
        public void close() {
            if (closeRequested) {
                return;
            }
            closeRequested = true;
            retireIfUnpinned();
        }

        private void retireIfUnpinned() {
            if (closeRequested && pins == 0 && !closed && !released) {
                closed = true;
                retire(this::release);
            }
        }

        private void release() {
            if (released) {
                return;
            }
            released = true;
            closed = true;
            meshes.remove(this);
            if (indices != null && ownsIndices) {
                indices.close();
            }
            if (vertices != null) {
                vertices.close();
            }
        }
    }

    public final class Texture implements AutoCloseable {
        private final VulkanRenderer owner = VulkanRenderer.this;
        private final VulkanTextures.Texture nativeTexture;

        private Texture(VulkanTextures.Texture nativeTexture) {
            this.nativeTexture = nativeTexture;
        }

        public int width() {
            return nativeTexture.width();
        }

        public int height() {
            return nativeTexture.height();
        }

        @Override
        public void close() {
            nativeTexture.close();
        }
    }

    private void retire(Runnable action) {
        if (recording != null || !uploads.isEmpty()) {
            pendingRetirements.add(action);
        } else if (lastSubmission != null) {
            lastSubmission.retire(action);
        } else {
            action.run();
        }
    }

    private static float clamp(float value) {
        return Math.max(0, Math.min(1, value));
    }

    private void requireFormats() {
        try (var stack = MemoryStack.stackPush()) {
            var properties = VkFormatProperties.calloc(stack);
            vkGetPhysicalDeviceFormatProperties(context.physicalDevice(), COLOR_FORMAT, properties);
            if ((properties.optimalTilingFeatures() & VK_FORMAT_FEATURE_COLOR_ATTACHMENT_BIT) == 0)
                throw new IllegalStateException("Vulkan device has no RGBA8 color attachments");
            vkGetPhysicalDeviceFormatProperties(context.physicalDevice(), DEPTH_FORMAT, properties);
            if ((properties.optimalTilingFeatures() & VK_FORMAT_FEATURE_DEPTH_STENCIL_ATTACHMENT_BIT) == 0)
                throw new IllegalStateException("Vulkan device has no D32 depth attachments");
        }
    }

    private void initializeRasterLimits() {
        try (var stack = MemoryStack.stackPush()) {
            var properties = VkPhysicalDeviceProperties.calloc(stack);
            vkGetPhysicalDeviceProperties(context.physicalDevice(), properties);
            wideLines = context.wideLines();
            minLineWidth = properties.limits().lineWidthRange(0);
            maxLineWidth = properties.limits().lineWidthRange(1);
        }
    }

    private void createRenderPass() {
        renderPasses = new VulkanRenderPasses(context, COLOR_FORMAT, DEPTH_FORMAT);
        renderPass = renderPasses.get(AttachmentIntent.LoadOp.CLEAR, AttachmentIntent.LoadOp.CLEAR, true, true);
    }

    private void resize(int width, int height) {
        current.targets.resize(width, height, COLOR_FORMAT, DEPTH_FORMAT);
        uploads.initialize(current.targets.color, current.targets.depth);
    }

    private static int pixelBytes(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Image dimensions must be positive");
        }
        return Math.multiplyExact(Math.multiplyExact(width, height), 4);
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("Vulkan renderer is closed");
        }
    }

    private final class FrameResources implements AutoCloseable {
        final int index;
        final VulkanFrameTargets targets = new VulkanFrameTargets(context, renderPass, 0, 0, false);
        final VulkanCommandSlot slot = new VulkanCommandSlot(context);
        final VulkanStreamArena stream = new VulkanStreamArena(context);
        final List<Mesh> transientMeshes = new ArrayList<>();
        final VulkanUniformStorage viewUniforms =
            new VulkanUniformStorage(context, bindings, VulkanBindings.VIEW, VulkanDrawConstants.VIEW_BYTES);
        final VulkanUniformStorage objectUniforms =
            new VulkanUniformStorage(context, bindings, VulkanBindings.OBJECT, VulkanDrawConstants.OBJECT_BYTES);
        final VulkanUniformStorage materialUniforms =
            new VulkanUniformStorage(context, bindings, VulkanBindings.MATERIAL, VulkanDrawConstants.MATERIAL_BYTES);
        long serial;
        AttachmentRetention retention = AttachmentRetention.ALL;

        FrameResources(int index) { this.index = index; }

        @Override
        public void close() {
            slot.close();
            viewUniforms.close();
            objectUniforms.close();
            materialUniforms.close();
            stream.close();
            targets.close();
        }
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        context.waitIdle();
        closed = true;
        preparedViews.clear();
        preparedMaterials.clear();
        recording = null;
        uploads.close();
        for (var frame : frames) {
            if (frame != null) {
                frame.slot.await();
            }
        }
        for (Runnable action : pendingRetirements) {
            action.run();
        }
        pendingRetirements.clear();
        for (Mesh mesh : List.copyOf(meshes)) {
            mesh.release();
        }
        if (textures != null) {
            textures.close();
        }
        for (var frame : frames) {
            if (frame != null) {
                frame.close();
            }
        }
        geometry.close();
        staging.close();
        if (pipelines != null) {
            pipelines.close();
        }
        pipelines = null;
        if (renderPasses != null) {
            renderPasses.close();
        }
        renderPasses = null;
        renderPass = VK_NULL_HANDLE;
        if (bindings != null) {
            bindings.close();
        }
        bindings = null;
    }
}
