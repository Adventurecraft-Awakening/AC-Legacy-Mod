package dev.adventurecraft.awakening.client.render.vulkan;

import dev.adventurecraft.awakening.client.render.VertexFormat;
import dev.adventurecraft.awakening.client.render.RenderPhase;
import dev.adventurecraft.awakening.client.render.RayLighting;
import dev.adventurecraft.awakening.client.render.RenderMath;
import dev.adventurecraft.awakening.client.render.NativeRenderingOptions;
import dev.adventurecraft.awakening.client.render.material.TileMaterialTable;
import dev.adventurecraft.awakening.client.render.state.*;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.*;

import java.lang.ref.WeakReference;
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
import static org.lwjgl.vulkan.VK12.VK_BUFFER_USAGE_SHADER_DEVICE_ADDRESS_BIT;

/**
 * Native Vulkan renderer for the game's fixed function render stream. All passes
 * preserve the order of draws and clears. Ray-traced worlds use linear HDR when supported;
 * classic worlds retain display colors, including with temporal AA. UI follows the world resolve.
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
    private VulkanPipelines worldPipelines;
    private VulkanPipelines classicWorldPipelines;
    private VulkanPostProcess postProcess;
    private VulkanExposure imageExposure;
    private boolean exposureCapable;
    private ExposureCamera exposureCamera;
    private record ExposureCamera(Matrix4 viewProjection, Vector4 origin, Vector4 eye,
                                  TileMaterialTable materials, long textureGeneration,
                                  int width, int height, int worldWidth, int worldHeight, long time) { }
    private boolean hdrSupported;
    private int reactiveFormat;
    private boolean sampledDepth;
    private VulkanTemporalAa temporalAa;
    private VulkanNativeRendering nativeRendering;
    private boolean temporalAaEnabled;
    private VulkanUpscale upscale;
    private int renderScale;
    private int parallaxMapping;
    private int appliedParallaxMapping;
    private boolean depthBlitSupported;
    private boolean renderScaleCapable;
    private RenderScaleStatistics renderScaleStatistics = new RenderScaleStatistics(100, 0, 0, 0, 0, false, 0);
    private static final int[] RENDER_PERCENTAGES = {100, 85, 75, 67, 50};
    private TileMaterialTable temporalMaterials;
    private TileMaterialTable nativeMaterials;
    private long temporalTextureGeneration = Long.MIN_VALUE;
    private long temporalSequence;
    private VulkanBindings bindings;
    private VulkanTextures textures;
    private VulkanRayScene rayScene;
    private VulkanRestirDi restir;
    private VulkanPipelines restirPipelines;
    private VulkanSharc sharc;
    private int indirectLighting;
    private int appliedIndirectLighting;
    private String sharcFailure;
    private long restirMaterialRevision;
    private long sharcSceneRevision;
    private WeakReference<Object> streamedWorldIdentity = new WeakReference<>(null);
    private long restirGeometryFingerprint;
    private long meshIdentities;
    private boolean rayTracingEnabled;
    private long rayTextureGeneration = Long.MIN_VALUE;
    private TileMaterialTable rayMaterialTable;
    private long renderPass;
    private VulkanRenderPasses renderPasses;
    private long worldRenderPass;
    private VulkanRenderPasses worldRenderPasses;
    private float adaptedExposure = 1;
    private long exposureTime;

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
            exposureCapable = hdrSupported && VulkanExposure.supported(context);
            renderScaleCapable = hdrSupported && depthBlitSupported && VulkanUpscale.supported(context);
            initializeRasterLimits();
            bindings = new VulkanBindings(context, FRAMES_IN_FLIGHT);
            textures = new VulkanTextures(context, bindings, staging, uploads, this::retire,
                bytes -> uploadedBytes += bytes);
            createRenderPass();
            pipelines = new VulkanPipelines(context, bindings.pipelineLayout(), renderPass, false);
            if (hdrSupported) {
                worldPipelines = new VulkanPipelines(context, bindings.pipelineLayout(), worldRenderPass, true);
                classicWorldPipelines = new VulkanPipelines(context, bindings.pipelineLayout(), worldRenderPass,
                    false, false, true);
                postProcess = new VulkanPostProcess(context, FRAMES_IN_FLIGHT);
            }
            for (int i = 0; i < frames.length; i++) {
                frames[i] = new FrameResources(i);
            }
            nativeRendering = new VulkanNativeRendering(context, FRAMES_IN_FLIGHT);
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

    /** Terrain keeps a tracing allocation alongside the exact triangles used for rasterization. */
    public Mesh uploadWorldQuads(List<ByteBuffer> blocks, int vertexCount, int atlasId) {
        return uploadWorldQuads(blocks, vertexCount, atlasId, false);
    }

    public Mesh uploadWorldQuads(List<ByteBuffer> blocks, int vertexCount, int atlasId, boolean translucent) {
        if (atlasId < 0 || atlasId >= 4) throw new IllegalArgumentException("Invalid terrain atlas");
        Mesh mesh = uploadQuads(blocks, vertexCount);
        try {
            if (rayScene != null) mesh.rayGeometry = rayScene.createGeometry(blocks, vertexCount, atlasId, translucent);
            mesh.rayAtlas = atlasId;
            mesh.rayTranslucent = translucent;
            return mesh;
        } catch (RuntimeException | Error error) {
            mesh.close();
            throw error;
        }
    }

    /** Loaded invisible clip walls participate in light transport without entering the raster draw stream. */
    public Mesh uploadOccluderQuads(List<ByteBuffer> blocks, int vertexCount) {
        return uploadWorldQuads(blocks, vertexCount, 0, false);
    }

    public boolean rayTracingAvailable() { return context.rayTracingAvailable(); }

    /** The gated option value; an open frame keeps its existing scene until the next frame. */
    public boolean rayTracingEnabled() { return rayTracingEnabled; }

    /** Active-frame compatibility check used by world lighting and atlas collection. */
    public boolean rayTracingSupported() { return rayScene != null; }

    public boolean sharcAvailable() {
        return VulkanSharc.supported(context) && VulkanRestirDi.supported(context);
    }

    public String sharcUnavailableReason() {
        if (!VulkanSharc.shadersAvailable()) return "This build has no separately licensed NVIDIA SHARC SDK shaders.";
        if (!context.sharcAvailable()) return context.sharcUnavailableReason();
        if (!VulkanRestirDi.supported(context)) return "SHARC requires terrain capture formats and sampler support.";
        return sharcFailure == null ? "" : sharcFailure;
    }

    /** 0 uses the vertex cache; 1–3 select 2^20, 2^21 or 2^22 spatial entries. */
    public void setIndirectLighting(int mode) {
        ensureOpen();
        indirectLighting = Math.clamp(mode, 0, 3);
        if (recording == null) applyIndirectLightingSetting();
    }

    public int indirectLighting() { return indirectLighting; }
    public boolean sharcEnabled() { return sharc != null; }

    private void applyIndirectLightingSetting() {
        int requested = rayScene != null && sharcAvailable() ? indirectLighting : 0;
        if (requested == appliedIndirectLighting) return;
        if (nativeRendering != null) nativeRendering.invalidate();
        if (temporalAa != null) temporalAa.invalidateHistory();
        context.waitIdle();
        for (FrameResources frame : frames) if (frame != null) frame.slot.await();
        if (sharc != null) sharc.close();
        sharc = null;
        sharcFailure = null;
        appliedIndirectLighting = requested;
        if (requested > 0) {
            try {
                sharc = new VulkanSharc(context, FRAMES_IN_FLIGHT, bindings.rayLayout(), 19 + requested);
            } catch (RuntimeException error) {
                sharcFailure = "SHARC initialization failed; using the vertex cache: " + error.getMessage();
                System.getLogger(VulkanRenderer.class.getName()).log(System.Logger.Level.WARNING, sharcFailure);
            }
        }
    }

    public void setRayTracingEnabled(boolean enabled) {
        ensureOpen();
        rayTracingEnabled = enabled && rayTracingAvailable();
        if (recording == null) applyRayTracingSetting();
    }

    private void applyRayTracingSetting() {
        if (rayTracingEnabled == (rayScene != null)) return;
        if (nativeRendering != null) nativeRendering.invalidate();
        invalidateExposure();
        if (temporalAa != null) temporalAa.invalidateHistory();
        // Descriptor pools, AS slices and source-history copies must outlive every old frame.
        // Keep queued raster uploads/retirements intact; their allocations are independent.
        context.waitIdle();
        for (FrameResources frame : frames) if (frame != null) frame.slot.await();
        if (rayTracingEnabled) {
            try {
                rayScene = new VulkanRayScene(context, bindings.rayLayout(), bindings.pipelineLayout());
                if (VulkanRestirDi.supported(context)) {
                    restir = new VulkanRestirDi(context, FRAMES_IN_FLIGHT, bindings.rayLayout());
                    restirPipelines = new VulkanPipelines(context, bindings.pipelineLayout(), restir.renderPass(), true, true);
                }
            } catch (RuntimeException | Error error) {
                if (restirPipelines != null) restirPipelines.close();
                restirPipelines = null;
                if (restir != null) restir.close();
                restir = null;
                if (rayScene != null) rayScene.close();
                rayScene = null;
                rayTracingEnabled = false;
                throw error;
            }
        } else {
            if (sharc != null) sharc.close();
            sharc = null;
            appliedIndirectLighting = 0;
            if (restirPipelines != null) restirPipelines.close();
            restirPipelines = null;
            if (restir != null) restir.close();
            restir = null;
            for (Mesh mesh : meshes) {
                if (mesh.rayGeometry != null) {
                    mesh.rayGeometry.close();
                    mesh.rayGeometry = null;
                }
            }
            rayScene.close();
            rayScene = null;
            for (FrameResources frame : frames) {
                if (frame.rayLighting != null) {
                    frame.rayLighting.close();
                    frame.rayLighting = null;
                }
                if (frame.rayMaterials != null) {
                    frame.rayMaterials.close();
                    frame.rayMaterials = null;
                    frame.uploadedMaterials = null;
                }
                if (frame.rayEmitters != null) {
                    frame.rayEmitters.close();
                    frame.rayEmitters = null;
                }
            }
        }
        rayTextureGeneration = Long.MIN_VALUE;
        rayMaterialTable = null;
    }

    public void terrainTextureGeneration(long generation) {
        if (generation != temporalTextureGeneration) {
            if (nativeRendering != null) nativeRendering.invalidate();
            temporalTextureGeneration = generation;
            if (temporalAa != null) temporalAa.invalidateHistory();
            sharcSceneRevision++;
        }
        if (rayScene != null && generation != rayTextureGeneration) {
            rayTextureGeneration = generation;
            rayScene.invalidateIrradiance();
            restirMaterialRevision++;
        }
    }

    public void drawPhase(RenderPhase phase) {
        requireFrame();
        recording.phase = java.util.Objects.requireNonNull(phase);
        if (phase == RenderPhase.HANDS && recording.world && recording.handsStart < 0) {
            recording.handsStart = precedingDepthClear(recording);
        }
        if (phase == RenderPhase.UI && recording.world && recording.uiStart < 0) {
            recording.uiWorldEnd = precedingDepthClear(recording);
            recording.uiStart = recording.commands.size();
        }
    }

    private static int precedingDepthClear(FrameRecording frame) {
        int index = frame.commands.size();
        if (index > 0 && frame.commands.get(index - 1) instanceof Clear clear
            && clear.depth && !clear.color && clear.scissor == null) index--;
        return index;
    }

    /** Start the world segment before its first clear, including on raster-only devices. */
    public void worldEnvironment(EnvironmentState environment) {
        requireFrame();
        recording.world = true;
        recording.environment = java.util.Objects.requireNonNull(environment);
    }

    /** Chunk streaming changes the resident mesh set without changing existing surfaces' light transport. */
    public void worldTerrainStreaming() {
        requireFrame();
        recording.streamedTerrain = true;
    }

    /** Persistent world-space caches cannot be carried between distinct levels at the same coordinates. */
    public void worldTerrainStreaming(Object worldIdentity) {
        requireFrame();
        java.util.Objects.requireNonNull(worldIdentity, "worldIdentity");
        if (streamedWorldIdentity.get() != worldIdentity) {
            streamedWorldIdentity = new WeakReference<>(worldIdentity);
            terrainGeometryChanged();
            invalidateExposure();
        }
        recording.streamedTerrain = true;
    }

    /** A rebuilt resident chunk changed content; loading a new chunk does not call this. */
    public void terrainGeometryChanged() {
        ensureOpen();
        if (nativeRendering != null) nativeRendering.invalidate();
        restirMaterialRevision++;
        sharcSceneRevision++;
        if (temporalAa != null) temporalAa.invalidateHistory();
        if (rayScene != null) rayScene.invalidateIrradiance();
    }

    public boolean hdrSupported() { return hdrSupported; }

    public void setRayReconstruction(int mode) {
        ensureOpen(); nativeRendering.setReconstruction(Math.clamp(mode, 0, 2));
    }
    public void setUpscaler(int mode) {
        ensureOpen(); nativeRendering.setUpscaler(Math.clamp(mode, 0, 2));
    }
    public void setFrameGeneration(int mode) {
        ensureOpen(); nativeRendering.setGeneration(Math.clamp(mode, 0, 2));
    }
    public NativeRenderingOptions.Status nativeRenderingStatus(NativeRenderingOptions.Group group, int mode) {
        ensureOpen();
        if (mode != 0 && (!hdrSupported || !sampledDepth || !temporalAaSupported()))
            return NativeRenderingOptions.Status.unavailable("Native rendering requires sampled world depth and floating-point temporal targets");
        if (mode != 0 && group == NativeRenderingOptions.Group.RECONSTRUCTION && !rayTracingEnabled)
            return NativeRenderingOptions.Status.unavailable("Ray reconstruction requires Ray Tracing to be enabled");
        if (mode == 2 && group == NativeRenderingOptions.Group.UPSCALER && rayTracingEnabled
            && nativeRendering.selection().reconstruction() == 1
            && nativeRendering.status(NativeRenderingOptions.Group.RECONSTRUCTION, 1).available())
            return NativeRenderingOptions.Status.unavailable("DLSS Ray Reconstruction includes DLSS Super Resolution; select built-in reconstruction or NRD to use FSR");
        return nativeRendering.status(group, mode);
    }
    public int nativeRenderingEffective(NativeRenderingOptions.Group group) { return nativeRendering.effective(group); }

    public int parallaxMapping() { return parallaxMapping; }
    public void setParallaxMapping(int quality) {
        ensureOpen();
        parallaxMapping = Math.clamp(quality, 0, 2);
        if (recording == null) applyParallaxSetting();
    }

    private void applyParallaxSetting() {
        if (appliedParallaxMapping == parallaxMapping) return;
        if (nativeRendering != null) nativeRendering.invalidate();
        appliedParallaxMapping = parallaxMapping;
        if (rayScene == null) return;
        // Surface sampling changes in both the terrain capture and final color.
        // Keep world-space transport, but discard the old screen-space samples.
        if (temporalAa != null) temporalAa.invalidateHistory();
        if (restir != null) restir.invalidateHistory();
        if (sharc != null) sharc.invalidateHistory();
        invalidateExposure();
    }

    public record ExposureStatistics(boolean supported, boolean enabled, long frames, long resets, int width, int height) { }
    public ExposureStatistics exposureStatistics() {
        boolean enabled = exposureCapable && rayScene != null && autoExposureEnabled();
        if (imageExposure == null) return new ExposureStatistics(exposureCapable, enabled, 0, 0, 0, 0);
        var value = imageExposure.statistics();
        return new ExposureStatistics(exposureCapable, enabled, value.frames(), value.resets(), value.width(), value.height());
    }

    public boolean temporalAaSupported() { return hdrSupported && VulkanTemporalAa.supported(context); }
    public boolean temporalAaEnabled() { return temporalAaEnabled; }

    public boolean renderScaleSupported() { return renderScaleCapable; }
    public String renderScaleUnavailableReason() {
        return renderScaleSupported() ? "" : "Render scaling requires floating-point compute output and depth-image blits.";
    }
    public int renderScale() { return renderScale; }
    public record RenderScaleStatistics(int requestedPercent, int worldWidth, int worldHeight,
                                        int outputWidth, int outputHeight, boolean active, long scaledFrames) { }
    public RenderScaleStatistics renderScaleStatistics() { return renderScaleStatistics; }
    public void setRenderScale(int mode) {
        ensureOpen();
        int requested = Math.clamp(mode, 0, RENDER_PERCENTAGES.length - 1);
        if (renderScale == requested) return;
        if (nativeRendering != null) nativeRendering.invalidate();
        renderScale = requested;
        invalidateExposure();
        if (temporalAa != null) temporalAa.invalidateHistory();
        if (restir != null) restir.invalidateHistory();
        if (sharc != null) sharc.invalidateHistory();
    }
    public String temporalAaUnavailableReason() {
        if (!hdrSupported) return "Temporal AA requires floating-point world targets and compute output.";
        return temporalAaSupported() ? "" : "Temporal AA requires sampled depth, floating-point storage images, compute and independent blending.";
    }
    public void setTemporalAaEnabled(boolean enabled) {
        ensureOpen();
        boolean requested = enabled && temporalAaSupported();
        if (requested == temporalAaEnabled) return;
        temporalAaEnabled = requested;
        invalidateExposure();
        // An open recording retains its own jitter and resolve choice until submission.
        if (temporalAa != null) temporalAa.invalidateHistory();
    }
    public record TemporalAaStatistics(long frames, long resets, int width, int height, int historyFrames) { }
    public TemporalAaStatistics temporalAaStatistics() {
        if (temporalAa == null) return new TemporalAaStatistics(0, 0, 0, 0, 0);
        var value = temporalAa.statistics();
        return new TemporalAaStatistics(value.frames(), value.resets(), value.width(), value.height(), value.historyFrames());
    }

    /** Immutable map appearance snapshot; the owning slot uploads changes after its fence. */
    public void worldMaterials(TileMaterialTable materials) {
        requireFrame();
        if (recording.rayModel != null) {
            throw new IllegalStateException("Set world materials before submitting terrain draws");
        }
        recording.materials = java.util.Objects.requireNonNull(materials);
    }

    /** Snapshot captured on the world thread; it never reads mutable game state on the GPU path. */
    public void worldLighting(RayLighting.Snapshot lighting, Color4 skyColor) {
        requireFrame();
        recording.lighting = java.util.Objects.requireNonNull(lighting);
        recording.skyColor = java.util.Objects.requireNonNull(skyColor);
    }

    public void worldAtlas(int atlas, Texture texture) {
        worldAtlas(atlas, texture, null, null);
    }

    /** Binds the albedo plus its LabPBR normal/AO/height and specular/emission companions. */
    public void worldAtlas(int atlas, Texture albedo, Texture normal, Texture specular) {
        requireFrame();
        if (atlas < 0 || atlas >= recording.rayAtlases.length) throw new IllegalArgumentException("Invalid terrain atlas");
        requireTexture(albedo);
        recording.rayAtlases[atlas] = albedo;
        recording.images.add(albedo.nativeTexture.image());
        if (normal != null) {
            requireTexture(normal);
            recording.rayNormalAtlases[atlas] = normal;
            recording.images.add(normal.nativeTexture.image());
        }
        if (specular != null) {
            requireTexture(specular);
            recording.raySpecAtlases[atlas] = specular;
            recording.images.add(specular.nativeTexture.image());
        }
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
        applyRayTracingSetting();
        applyParallaxSetting();
        applyIndirectLightingSetting();
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
        recording.temporalRequested = temporalAaEnabled;
        recording.nativeSelection = nativeRendering.selection();
        recording.renderScale = renderScale;
        recording.parallaxMapping = appliedParallaxMapping;
        recording.linearWorld = hdrSupported && rayScene != null;
        recording.floatWorld = hdrSupported && (recording.linearWorld || recording.temporalRequested
            || recording.nativeSelection.upscaler() != 0 || recording.nativeSelection.generation() != 0
            || renderScale > 0 && renderScaleSupported());
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
        boolean traced = mesh.rayGeometry != null &&
            (recording.phase == RenderPhase.TERRAIN_OPAQUE || recording.phase == RenderPhase.TERRAIN_TRANSLUCENT);
        if (traced) {
            recording.rayAtlases[mesh.rayAtlas] = texture;
            recording.rayModel = pass.model;
            recording.rayMeshes.add(mesh);
        }
        enqueueGeometry(mesh.geometry, texture, pass, traced ? mesh.rayGeometry : null, traced ? mesh.rayAtlas * 3 : 0);
    }

    public void draw(StreamGeometry stream, Texture texture, PreparedPass pass) {
        requireFrame();
        if (stream == null || stream.owner != this || stream.frame != current || stream.serial != current.serial) {
            throw new IllegalArgumentException("Stream geometry belongs to another frame");
        }
        enqueueGeometry(stream.geometry, texture, pass, null, 0);
    }

    private void enqueueGeometry(Geometry geometry, Texture texture, PreparedPass pass, VulkanRayScene.Geometry rayGeometry,
                                 int atlasBase) {
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
        boolean traced = rayGeometry != null;
        boolean floating = recording.floatWorld && recording.world && recording.uiStart < 0;
        recording.commands.add(new Draw(geometry, texture,
            pass.pipeline(geometry.topology, traced, floating, floating && recording.linearWorld), entry, traced,
            rayGeometry, atlasBase, recording.phase, recording.world && recording.uiStart < 0));
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

        private long pipeline(int topology, boolean traced, boolean floating, boolean linear) {
            if (topology < 0 || topology >= 4) {
                throw new IllegalArgumentException("Unsupported primitive topology");
            }
            int index = topology + (traced ? 4 : 0) + (floating ? linear ? 16 : 8 : 0);
            long pipeline = topologies[index];
            if (pipeline == VK_NULL_HANDLE) {
                VulkanPipelines selected = floating ? linear ? worldPipelines : classicWorldPipelines : pipelines;
                pipeline = selected.get(new VulkanPipelines.Key(topology, key.depthTest(), key.depthWrite(),
                    key.depthFunc(), key.blend(), key.blendSrc(), key.blendDst(), key.cullMode(), key.frontFace(),
                    key.colorMask(), key.depthBias(), traced ? key.variant().traced() : key.variant()));
                topologies[index] = pipeline;
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
            prepareWorldScale(frame, plan);
            prepareNativeRendering(frame, plan);
            prepareTemporalAa(frame, plan);
            prepareExposure(frame, plan);
            prepareRestir(frame);
            if (frame.nativePlan.exportLobes() && frame.restirPass == null) {
                frame.nativePlan = VulkanNativeRendering.Plan.NONE;
                nativeRendering.prepare(current.index, frame.nativePlan, frame.worldWidth, frame.worldHeight, frame.width, frame.height, frame.linearWorld);
                frame.temporalAa = frame.jitterWorld;
            }
            VkCommandBuffer command = current.slot.begin();
            try (var stack = MemoryStack.stackPush()) {
                uploads.record(command, stack);
                recordRayScene(command, frame);
                recordRestir(command, frame, stack);
                recordNativeGuides(command, frame);
                preserveAttachments(command, plan);
                if (frame.floatWorld && frame.world) {
                    int split = frame.uiStart < 0 ? frame.commands.size() : frame.uiStart;
                    int worldEnd = frame.jitterWorld || frame.scaled || frame.exposureCamera != null || frame.nativePlan.active() ? frame.worldEnd : split;
                    VulkanFrameTargets worldTargets = frame.scaled ? current.scaledTargets : current.targets;
                    recordRenderPass(command, frame, plan, stack, true, plan.consumedClears, worldEnd, worldTargets);
                    VulkanImage nativeResolved = null;
                    if (frame.nativePlan.active()) {
                        nativeRendering.recordWorldMotion(command, current.index, worldTargets.depth, worldTargets.reactive);
                        nativeResolved = nativeRendering.reconstruct(command, current.index, worldTargets.world, worldTargets.depth);
                        if (frame.nativePlan.temporal() && nativeResolved == null) frame.temporalAa = true;
                    }
                    if (frame.temporalAa) {
                        recordTemporalAa(command, frame);
                    }
                    if (frame.exposureCamera != null) {
                        VulkanImage source = frame.temporalAa ? temporalAa.result(current.index) : worldTargets.world;
                        imageExposure.record(command, current.index, source, frame.exposureDelta, frame.exposureReset);
                        frame.exposureBuffer = imageExposure.exposureBuffer(current.index);
                    }
                    if (frame.scaled) {
                        VulkanImage source = frame.temporalAa ? temporalAa.result(current.index) : worldTargets.world;
                        if (nativeResolved != null) current.targets.resolveWorldFrom(command, nativeResolved);
                        else upscale.record(command, current.index, source, current.targets.world);
                        current.targets.upscaleDepthFrom(command, worldTargets);
                    } else if (nativeResolved != null) {
                        current.targets.resolveWorldFrom(command, nativeResolved);
                    } else if (frame.temporalAa) {
                        current.targets.resolveWorldFrom(command, temporalAa.result(current.index));
                    }
                    if (worldEnd < split) {
                        var handsPlan = new AttachmentPlan(AttachmentIntent.LoadOp.LOAD, AttachmentIntent.LoadOp.LOAD,
                            Color4.TRANSPARENT, 1, worldEnd);
                        recordRenderPass(command, frame, handsPlan, stack, true, worldEnd, split);
                    }
                    postProcess.record(command, current.index, current.targets.world, current.targets.color,
                        frame.linearWorld ? frame.exposureBuffer != null ? manualExposure() : exposure(frame) : 1,
                        frame.linearWorld, frame.exposureBuffer);
                    if (frame.nativePlan.generation() != 0) nativeRendering.recordHudless(command, current.index, current.targets.color);
                    // Keep the world depth until UI applies its own ordered clear.
                    var uiPlan = new AttachmentPlan(AttachmentIntent.LoadOp.LOAD, AttachmentIntent.LoadOp.LOAD,
                        Color4.TRANSPARENT, 1, split);
                    recordRenderPass(command, frame, uiPlan, stack, false, split, frame.commands.size());
                    if (frame.nativePlan.generation() != 0) nativeRendering.generate(command, current.index, current.targets.color);
                } else {
                    recordRenderPass(command, frame, plan, stack, false, plan.consumedClears, frame.commands.size());
                }
            }
            submitFrame(frame);
            return new GpuFrame(frame.width, frame.height,
                nativeRendering.realImage(current.index, current.targets.color.handle), nativeRendering.generatedImage(current.index), current.index, current.serial);
        } catch (RuntimeException | Error error) {
            // A failed recording/submission must not free resources still used by older frames.
            context.waitIdle();
            if (rayScene != null) rayScene.abort(current.index);
            if (restir != null) restir.abort(current.index);
            if (sharc != null) sharc.abort(current.index);
            if (temporalAa != null) temporalAa.abort(current.index);
            if (imageExposure != null) imageExposure.abort(current.index);
            nativeRendering.abort(current.index);
            exposureCamera = null;
            if (error instanceof VulkanNativeRendering.NativeRecordingFailure && !frame.nativeRetried) {
                // Uploads and collected draws are immutable until successful submission.
                // begin() resets the abandoned command pool before recording the same
                // scene again, with every native dispatch disabled for this retry.
                frame.nativeRetried = true;
                frame.nativePlan = VulkanNativeRendering.Plan.NONE;
                frame.temporalAa = frame.jitterWorld = false;
                frame.jitterX = frame.jitterY = 0;
                frame.restirPass = null;
                frame.restirCompute = frame.sharc = false;
                frame.exposureBuffer = null;
                frame.exposureCamera = null;
                return finishFrame();
            }
            throw error;
        } finally {
            recording = null;
        }
    }

    private void recordRayScene(VkCommandBuffer command, FrameRecording frame) {
        if (rayScene == null || frame.rayModel == null) return;
        Texture fallbackAlbedo = frame.rayAtlases[0];
        if (fallbackAlbedo == null) return;
        Texture fallbackNormal = frame.rayNormalAtlases[0] != null ? frame.rayNormalAtlases[0] : fallbackAlbedo;
        Texture fallbackSpecular = frame.raySpecAtlases[0] != null ? frame.raySpecAtlases[0] : fallbackAlbedo;
        for (int atlas = 1; atlas < frame.rayAtlases.length; atlas++) {
            if (frame.rayNormalAtlases[atlas] != null) { fallbackNormal = frame.rayNormalAtlases[atlas]; break; }
        }
        for (int atlas = 1; atlas < frame.rayAtlases.length; atlas++) {
            if (frame.raySpecAtlases[atlas] != null) { fallbackSpecular = frame.raySpecAtlases[atlas]; break; }
        }
        // Fixed triplet layout: atlas N occupies scene texture slots 3N, 3N+1, 3N+2.
        // This keeps the shader index arithmetic independent of which atlases are used.
        var rayTextures = new ArrayList<VulkanRayScene.TextureBinding>(12);
        for (int atlas = 0; atlas < frame.rayAtlases.length; atlas++) {
            Texture albedo = frame.rayAtlases[atlas] != null ? frame.rayAtlases[atlas] : fallbackAlbedo;
            Texture normal = frame.rayNormalAtlases[atlas] != null ? frame.rayNormalAtlases[atlas] : fallbackNormal;
            Texture specular = frame.raySpecAtlases[atlas] != null ? frame.raySpecAtlases[atlas] : fallbackSpecular;
            frame.boundRayNormals[atlas] = normal;
            frame.boundRaySpecular[atlas] = specular;
            rayTextures.add(albedo.nativeTexture.rayBinding());
            rayTextures.add(normal.nativeTexture.rayBinding());
            rayTextures.add(specular.nativeTexture.rayBinding());
        }
        // Fill the remaining descriptor slots so no sampled-image binding is left undefined.
        rayTextures.add(fallbackAlbedo.nativeTexture.rayBinding());
        rayTextures.add(fallbackNormal.nativeTexture.rayBinding());
        rayTextures.add(fallbackSpecular.nativeTexture.rayBinding());
        rayTextures.add(fallbackAlbedo.nativeTexture.rayBinding());
        var instances = new ArrayList<VulkanRayScene.Instance>();
        long geometryFingerprint = 0;
        // Include loaded terrain outside the view frustum: an unseen wall may cast a visible shadow.
        // All terrain builders use world coordinates and the same camera-relative model transform.
        for (Mesh mesh : meshes) {
            if (mesh.rayGeometry != null && (!mesh.closed && !mesh.closeRequested || frame.rayMeshes.contains(mesh))) {
                int flags = (mesh.rayTranslucent ? 1 : 0) | (frame.rayMeshes.contains(mesh) ? 4 : 0);
                instances.add(new VulkanRayScene.Instance(mesh.rayGeometry, frame.rayModel, mesh.rayAtlas * 3, flags));
                // Order-independent and camera-independent: remeshing/offscreen occluders reset shadow history.
                long identity = mesh.rayGeometry.vertexAddress() ^ Long.rotateLeft(mesh.cacheIdentity, 17) ^ ((long) mesh.rayAtlas << 48)
                    ^ (mesh.rayTranslucent ? 0x9e3779b97f4a7c15L : 0);
                identity = (identity ^ (identity >>> 30)) * 0xbf58476d1ce4e5b9L;
                geometryFingerprint += identity ^ (identity >>> 27);
            }
        }
        if (geometryFingerprint != restirGeometryFingerprint) {
            restirGeometryFingerprint = geometryFingerprint;
            if (!frame.streamedTerrain) {
                restirMaterialRevision++;
                sharcSceneRevision++;
                if (temporalAa != null) temporalAa.invalidateHistory();
                nativeRendering.invalidate();
            }
        }
        if (current.rayLighting == null) current.rayLighting = context.hostBuffer(VulkanRayLighting.BYTES, VK_BUFFER_USAGE_STORAGE_BUFFER_BIT);
        VulkanRayLighting.write(current.rayLighting, frame.lighting, frame.skyColor, frame.rayModel);
        if (current.rayMaterials == null) {
            current.rayMaterials = context.hostBuffer(TileMaterialTable.BYTES, VK_BUFFER_USAGE_STORAGE_BUFFER_BIT);
        }
        if (current.uploadedMaterials != frame.materials) {
            current.rayMaterials.write(frame.materials::writeTo);
            current.uploadedMaterials = frame.materials;
        }
        if (rayMaterialTable != frame.materials) {
            rayScene.invalidateIrradiance();
            restirMaterialRevision++;
            sharcSceneRevision++;
            rayMaterialTable = frame.materials;
        }
        rayScene.recordBuild(command, current.index, instances, rayTextures, current.rayLighting, current.rayMaterials);
        if (frame.restirPass != null) {
            int bytes = VulkanRayLighting.manyLightBytes(frame.lighting);
            if (current.rayEmitters == null || current.rayEmitters.size() < bytes) {
                if (current.rayEmitters != null) current.rayEmitters.close();
                current.rayEmitters = context.hostBuffer(bytes, VK_BUFFER_USAGE_STORAGE_BUFFER_BIT | VK_BUFFER_USAGE_SHADER_DEVICE_ADDRESS_BIT);
            }
            current.rayEmitters.write(target -> VulkanRayLighting.writeEmitters(target, frame.lighting));
        }
    }

    /** A single terrain camera is required for static-surface reprojection. Stereo views use forward lighting. */
    private PreparedPass worldCamera(FrameRecording frame, AttachmentPlan plan) {
        int uiEnd = frame.uiStart < 0 ? frame.commands.size() : frame.uiStart;
        int end = frame.handsStart >= 0 ? frame.handsStart : frame.uiWorldEnd >= 0 ? frame.uiWorldEnd : uiEnd;
        boolean eligible = frame.world
            && plan.color == AttachmentIntent.LoadOp.CLEAR && plan.depth == AttachmentIntent.LoadOp.CLEAR
            && plan.clearDepth == 1 && end >= plan.consumedClears;
        PreparedPass surface = null;
        Rect full = new Rect(0, 0, frame.width, frame.height);
        // Scan through all eyes, even after the first hand pass, before choosing a single camera history.
        for (int index = plan.consumedClears; eligible && index < uiEnd; index++) {
            Command item = frame.commands.get(index);
            if (item instanceof Clear) {
                if (index < end) eligible = false;
                continue;
            }
            Draw draw = (Draw) item;
            PreparedPass pass = draw.entry.pass;
            if (pass.key.colorMask() != 0 && pass.key.colorMask() != ColorMask.RGBA.bits()) eligible = false;
            if (index < end) eligible &= draw.entry.viewport.equals(full)
                && (pass.scissor == null || pass.scissor.equals(full)) && pass.minDepth == 0 && pass.maxDepth == 1;
            if (draw.phase != RenderPhase.TERRAIN_OPAQUE) continue;
            eligible &= index < end && translationOnly(pass.model) && pass.key.depthTest() && pass.key.depthWrite()
                && !pass.key.blend() && pass.key.colorMask() == ColorMask.RGBA.bits()
                && (pass.key.depthFunc() == VK_COMPARE_OP_LESS || pass.key.depthFunc() == VK_COMPARE_OP_LESS_OR_EQUAL);
            if (surface == null) surface = pass;
            else eligible &= surface.model.equals(pass.model) && surface.view.projection().equals(pass.view.projection())
                && surface.view.view().equals(pass.view.view());
        }
        if (eligible && surface != null) {
            try {
                VulkanTemporalAa.inverse(surface.view.projection().multiply(surface.view.view()));
                RenderMath.inverse(surface.view.view());
            } catch (IllegalStateException | IllegalArgumentException invalidCamera) { eligible = false; }
        }
        return eligible ? surface : null;
    }

    private void prepareWorldScale(FrameRecording frame, AttachmentPlan plan) {
        int uiEnd = frame.uiStart < 0 ? frame.commands.size() : frame.uiStart;
        frame.worldEnd = frame.handsStart >= 0 ? frame.handsStart : frame.uiWorldEnd >= 0 ? frame.uiWorldEnd : uiEnd;
        if (frame.renderScale == 0 || !renderScaleSupported() || worldCamera(frame, plan) == null) {
            current.scaledTargets.close();
            return;
        }
        int percent = RENDER_PERCENTAGES[frame.renderScale];
        frame.worldWidth = Math.max(1, (int) (((long) frame.width * percent + 99) / 100));
        frame.worldHeight = Math.max(1, (int) (((long) frame.height * percent + 99) / 100));
        frame.scaled = frame.worldWidth != frame.width || frame.worldHeight != frame.height;
        if (!frame.scaled) return;
        if (upscale == null) upscale = new VulkanUpscale(context, FRAMES_IN_FLIGHT);
        if (current.scaledTargets.width != frame.worldWidth || current.scaledTargets.height != frame.worldHeight) {
            current.scaledTargets.resize(frame.worldWidth, frame.worldHeight, COLOR_FORMAT, DEPTH_FORMAT);
            uploads.initialize(current.scaledTargets.color, current.scaledTargets.depth,
                current.scaledTargets.world, current.scaledTargets.reactive);
        }
    }

    private void prepareTemporalAa(FrameRecording frame, AttachmentPlan plan) {
        PreparedPass surface = (frame.temporalRequested || frame.nativePlan.temporal()) && hdrSupported ? worldCamera(frame, plan) : null;
        if (surface == null) {
            if (temporalAa != null) temporalAa.invalidateHistory();
            return;
        }
        if (temporalAa == null) temporalAa = new VulkanTemporalAa(context, FRAMES_IN_FLIGHT);
        temporalAa.resize(frame.worldWidth, frame.worldHeight);
        if (temporalMaterials != frame.materials) temporalAa.invalidateHistory();
        temporalMaterials = frame.materials;
        Vector4 jitter = frame.nativePlan.temporal() ? nativeRendering.jitter(temporalSequence) : VulkanTemporalAa.jitter(temporalSequence);
        frame.jitterX = 2 * jitter.x() / frame.worldWidth;
        frame.jitterY = 2 * jitter.y() / frame.worldHeight;
        frame.temporalPass = surface;
        frame.temporalWorldEnd = frame.worldEnd;
        frame.temporalAa = !frame.nativePlan.temporal();
        frame.jitterWorld = true;
    }

    private void prepareNativeRendering(FrameRecording frame, AttachmentPlan plan) {
        PreparedPass surface = hdrSupported && sampledDepth && temporalAaSupported() ? worldCamera(frame, plan) : null;
        if (surface != null) {
            Matrix4 projection = surface.view.projection();
            if (Math.abs(projection.get(3)) > 1e-6f || Math.abs(projection.get(7)) > 1e-6f
                || Math.abs(projection.get(15)) > 1e-6f || Math.abs(projection.get(11)) < 1e-6f) surface = null;
        }
        frame.nativePlan = nativeRendering.plan(frame.nativeSelection, !frame.nativeRetried && surface != null && frame.floatWorld,
            frame.linearWorld, rayScene != null && restir != null);
        if (nativeMaterials != frame.materials) nativeRendering.invalidate();
        nativeMaterials = frame.materials;
        try {
            nativeRendering.prepare(current.index, frame.nativePlan, frame.worldWidth, frame.worldHeight, frame.width, frame.height, frame.linearWorld);
        } catch (RuntimeException error) {
            nativeRendering.disable("Native target allocation failed; using the built-in renderer: " + error.getMessage());
            frame.nativePlan = VulkanNativeRendering.Plan.NONE;
            nativeRendering.prepare(current.index, frame.nativePlan, frame.worldWidth, frame.worldHeight, frame.width, frame.height, frame.linearWorld);
        }
        frame.nativePass = frame.nativePlan.active() ? surface : null;
    }

    private void recordNativeGuides(VkCommandBuffer command, FrameRecording frame) {
        if (!frame.nativePlan.active()) return;
        PreparedPass surface = frame.nativePass;
        Matrix4 viewProjection = surface.view.projection().multiply(surface.view.view());
        if (frame.jitterWorld) viewProjection = VulkanTemporalAa.jittered(viewProjection, frame.jitterX, frame.jitterY);
        boolean lobes = frame.nativePlan.exportLobes();
        nativeRendering.recordGuides(command, current.index, frame.restirPass == null ? null : restir.colorViews(current.index),
            viewProjection, surface.view.view(), new Vector4(-surface.model.get(12), -surface.model.get(13), -surface.model.get(14), 1),
            frame.jitterX, frame.jitterY, false,
            lobes && frame.restirCompute ? restir.rawDiffuseAddress() : 0,
            lobes && frame.restirCompute ? restir.rawSpecularAddress() : 0,
            lobes && frame.sharc ? sharc.rawDiffuseAddress() : 0, frame.linearWorld);
        nativeRendering.denoise(command, current.index, frame.restirDirect ? restir.resultAddress(current.index) : 0,
            frame.sharc ? sharc.resultAddress(current.index) : 0);
    }

    private void recordTemporalAa(VkCommandBuffer command, FrameRecording frame) {
        PreparedPass surface = frame.temporalPass;
        Matrix4 viewProjection = VulkanTemporalAa.jittered(surface.view.projection().multiply(surface.view.view()),
            frame.jitterX, frame.jitterY);
        double[] inverseView = RenderMath.inverse(surface.view.view());
        Vector4 origin = new Vector4(-surface.model.get(12), -surface.model.get(13), -surface.model.get(14), 1);
        Vector4 eye = new Vector4((float) inverseView[12], (float) inverseView[13], (float) inverseView[14], 1);
        VulkanFrameTargets targets = frame.scaled ? current.scaledTargets : current.targets;
        temporalAa.record(command, current.index, targets.world, targets.depth, targets.reactive,
            viewProjection, origin, eye, frame.jitterX, frame.jitterY, false, frame.linearWorld);
    }

    private static boolean autoExposureEnabled() {
        return Boolean.parseBoolean(System.getProperty("ac.vulkan.autoExposure", "true"));
    }

    private void invalidateExposure() {
        exposureCamera = null;
        if (imageExposure != null) imageExposure.invalidateHistory();
    }

    private void prepareExposure(FrameRecording frame, AttachmentPlan plan) {
        PreparedPass surface = frame.linearWorld && exposureCapable && autoExposureEnabled() ? worldCamera(frame, plan) : null;
        if (surface == null) {
            invalidateExposure();
            return;
        }
        if (imageExposure == null) imageExposure = new VulkanExposure(context, FRAMES_IN_FLIGHT);
        double[] inverseView = RenderMath.inverse(surface.view.view());
        var camera = new ExposureCamera(surface.view.projection().multiply(surface.view.view()),
            new Vector4(-surface.model.get(12), -surface.model.get(13), -surface.model.get(14), 1),
            new Vector4((float) inverseView[12], (float) inverseView[13], (float) inverseView[14], 1),
            frame.materials, temporalTextureGeneration, frame.width, frame.height, frame.worldWidth, frame.worldHeight, System.nanoTime());
        ExposureCamera previous = exposureCamera;
        frame.exposureReset = previous == null || previous.materials != camera.materials
            || previous.textureGeneration != camera.textureGeneration
            || previous.width != camera.width || previous.height != camera.height
            || previous.worldWidth != camera.worldWidth || previous.worldHeight != camera.worldHeight
            || VulkanTemporalAa.cameraJump(previous.viewProjection, previous.origin, previous.eye,
                camera.viewProjection, camera.origin, camera.eye);
        frame.exposureDelta = previous == null ? 0 : Math.clamp((camera.time - previous.time) * 1e-9f, 0, 1);
        frame.exposureCamera = camera;
    }

    private void prepareRestir(FrameRecording frame) {
        if (restir == null) return;
        PreparedPass surface = null;
        boolean endedTerrainDepth = false;
        frame.restirDirect = frame.lighting != null && !frame.lighting.emitters().isEmpty()
            && !Boolean.getBoolean("ac.vulkan.disableRestir");
        boolean eligible = frame.world && frame.rayModel != null && frame.lighting != null
            && (frame.restirDirect || sharc != null || frame.nativePlan.exportLobes()) && translationOnly(frame.rayModel);
        Rect full = new Rect(0, 0, frame.width, frame.height);
        int end = frame.uiStart < 0 ? frame.commands.size() : frame.uiStart;
        for (int index = 0; eligible && index < end; index++) {
            Command command = frame.commands.get(index);
            if (command instanceof Clear clear) {
                if (surface != null && clear.depth) endedTerrainDepth = true;
                continue;
            }
            Draw draw = (Draw) command;
            if (!draw.traced || draw.phase != RenderPhase.TERRAIN_OPAQUE) continue;
            PreparedPass pass = draw.entry.pass;
            // Later clouds/water may use depth-only color masks, and hands clear their own depth.
            // Only a new opaque terrain depth epoch or a masked terrain view invalidates this capture.
            eligible &= !endedTerrainDepth && pass.key.colorMask() == ColorMask.RGBA.bits()
                && pass.key.depthTest() && pass.key.depthWrite() && !pass.key.blend()
                && pass.minDepth == 0 && pass.maxDepth == 1
                && (pass.key.depthFunc() == VK_COMPARE_OP_LESS_OR_EQUAL || pass.key.depthFunc() == VK_COMPARE_OP_LESS)
                && draw.entry.viewport.equals(full) && (pass.scissor == null || pass.scissor.equals(full))
                && pass.model.equals(frame.rayModel);
            if (surface == null) surface = pass;
            else eligible &= surface.view.projection().equals(pass.view.projection())
                && surface.view.view().equals(pass.view.view());
        }
        if (!eligible || surface == null) {
            restir.invalidateHistory();
            if (sharc != null) sharc.invalidateHistory();
            return;
        }
        // Reject invalid compatibility matrices before allocating or recording GPU work.
        try { RenderMath.inverse(surface.view.view()); }
        catch (IllegalStateException ignored) {
            restir.invalidateHistory();
            if (sharc != null) sharc.invalidateHistory();
            return;
        }
        try {
            restir.setReconstructionExport(frame.nativePlan.exportLobes());
            if (sharc != null) sharc.setReconstructionExport(frame.nativePlan.exportLobes());
            restir.resize(frame.worldWidth, frame.worldHeight);
            if (sharc != null) sharc.resize(frame.worldWidth, frame.worldHeight);
        } catch (RuntimeException error) {
            if (sharc == null) throw error;
            context.waitIdle();
            sharc.close();
            sharc = null;
            sharcFailure = "SHARC screen allocation failed; using the vertex cache: " + error.getMessage();
            System.getLogger(VulkanRenderer.class.getName()).log(System.Logger.Level.WARNING, sharcFailure);
            if (!frame.restirDirect) {
                restir.invalidateHistory();
                return;
            }
            // Releasing the optional cache may leave enough memory for the ordinary direct-light path.
            restir.resize(frame.worldWidth, frame.worldHeight);
        }
        frame.restirPass = surface;
        frame.restirCompute = frame.restirDirect || frame.nativePlan.exportLobes();
    }

    private static boolean translationOnly(Matrix4 matrix) {
        for (int index = 0; index < 16; index++) {
            if (!Float.isFinite(matrix.get(index))) return false;
            if (index >= 12 && index <= 14) continue;
            if (matrix.get(index) != (index % 5 == 0 ? 1 : 0)) return false;
        }
        return true;
    }

    /** Quantized transport changes reset cached light without tying the epoch to camera motion. */
    static long sharcLightingKey(RayLighting.Snapshot lighting) {
        boolean capturedTransport = lighting.transportRevision() != RayLighting.UNTRACKED_TRANSPORT_REVISION;
        long hash = capturedTransport ? lighting.transportRevision() : lighting.emitterFingerprint();
        float[] celestial = {lighting.sunX(), lighting.sunY(), lighting.sunZ(), lighting.sunR(), lighting.sunG(),
            lighting.sunB(), lighting.skyIntensity(), lighting.environment().sky().r(), lighting.environment().sky().g(),
            lighting.environment().sky().b(), lighting.environment().weather().x(), lighting.environment().weather().y()};
        for (float value : celestial) hash = (hash ^ Math.round(value * 16)) * 0x100000001b3L;
        if (capturedTransport) return hash;
        for (RayLighting.Emitter emitter : lighting.emitters()) {
            RayLighting.Light light = emitter.light();
            hash = (hash ^ Math.round(light.red() * 16)) * 0x100000001b3L;
            hash = (hash ^ Math.round(light.green() * 16)) * 0x100000001b3L;
            hash = (hash ^ Math.round(light.blue() * 16)) * 0x100000001b3L;
            hash = (hash ^ Math.round(light.intensity() * 16)) * 0x100000001b3L;
            hash = (hash ^ Math.round(light.radius() * 4)) * 0x100000001b3L;
            hash = (hash ^ Math.round(emitter.emitterRadius() * 16)) * 0x100000001b3L;
        }
        return hash;
    }

    private void recordRestir(VkCommandBuffer command, FrameRecording frame, MemoryStack stack) {
        if (frame.restirPass == null) return;
        restir.beginCapture(command, current.index);
        restir.setTaaActive(frame.jitterWorld);
        if (sharc != null) sharc.setTaaActive(frame.jitterWorld);
        var encoder = new DrawEncoder(command, frame, stack, true, frame.jitterWorld, frame.worldWidth, frame.worldHeight);
        int end = frame.uiStart < 0 ? frame.commands.size() : frame.uiStart;
        for (int index = 0; index < end; index++) {
            Command item = frame.commands.get(index);
            if (item instanceof Draw draw && draw.traced && draw.phase == RenderPhase.TERRAIN_OPAQUE) encoder.record(draw);
        }
        restir.endCapture(command);
        PreparedPass surface = frame.restirPass;
        double[] inverseView = RenderMath.inverse(surface.view.view());
        Matrix4 viewProjection = surface.view.projection().multiply(surface.view.view());
        if (frame.jitterWorld) viewProjection = VulkanTemporalAa.jittered(viewProjection, frame.jitterX, frame.jitterY);
        Vector4 worldOrigin = new Vector4(-surface.model.get(12), -surface.model.get(13), -surface.model.get(14), 1);
        Vector4 eyeOrigin = new Vector4((float) inverseView[12], (float) inverseView[13], (float) inverseView[14], 1);
        if (frame.restirCompute) {
            restir.recordCompute(command, current.index, rayScene.descriptorSet(current.index), viewProjection,
                worldOrigin, eyeOrigin, restirMaterialRevision, frame.lighting.emitterFingerprint(),
                current.rayEmitters.deviceAddress(), false);
        }
        if (sharc != null) {
            sharc.record(command, current.index, rayScene.descriptorSet(current.index), restir.colorViews(current.index),
                viewProjection, worldOrigin, eyeOrigin, sharcSceneRevision, sharcLightingKey(frame.lighting),
                current.rayEmitters.deviceAddress(), false);
            frame.sharc = true;
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
        var initialClear = VkClearValue.calloc(hdr ? 3 : 2, stack);
        Color4 color = plan.clearColor;
        initialClear.get(0)
            .color()
            .float32(0, hdr && frame.linearWorld ? linearColor(color.r()) : color.r())
            .float32(1, hdr && frame.linearWorld ? linearColor(color.g()) : color.g())
            .float32(2, hdr && frame.linearWorld ? linearColor(color.b()) : color.b())
            .float32(3, color.a());
        if (hdr) initialClear.get(1).color().float32(0, frame.jitterWorld || frame.nativePlan.active() ? .5f : 1)
            .float32(1, 1).float32(2, 1).float32(3, 1);
        initialClear.get(hdr ? 2 : 1).depthStencil().depth(clamp(plan.clearDepth)).stencil(0);
        long frameRenderPass =
            (hdr ? worldRenderPasses : renderPasses).get(plan.color, plan.depth,
                hdr || frame.retention.color(), hdr || frame.retention.depth());
        var pass = VkRenderPassBeginInfo.calloc(stack)
                       .sType$Default()
                       .renderPass(frameRenderPass)
                       .framebuffer(hdr ? targets.worldFramebuffer : targets.framebuffer)
                       .pClearValues(initialClear);
        pass.renderArea().offset().set(0, 0);
        pass.renderArea().extent().set(targets.width, targets.height);
        vkCmdBeginRenderPass(command, pass, VK_SUBPASS_CONTENTS_INLINE);
        var encoder = new DrawEncoder(command, frame, stack, false,
            hdr && frame.jitterWorld && end <= frame.temporalWorldEnd, targets.width, targets.height);
        for (int index = start; index < end; index++) {
            Command item = frame.commands.get(index);
            if (item instanceof Clear clear) {
                recordClear(command, clear, stack, hdr, targets.width, targets.height);
            } else {
                encoder.record((Draw) item);
            }
        }
        vkCmdEndRenderPass(command);
    }

    private float exposure(FrameRecording frame) {
        long now = System.nanoTime();
        float dt = exposureTime == 0 ? 1 : Math.min((now - exposureTime) * 1e-9f, 1);
        exposureTime = now;
        // The captured eye skylight avoids camera-dependent fog colors driving adaptation.
        float eye = frame.environment.fog().x() > 0 ? clamp(frame.environment.fog().z()) : 1;
        float target = 1 + (1 - eye) * 0.6f;
        adaptedExposure += (target - adaptedExposure) * (1 - (float) Math.exp(-dt * 1.5f));
        return adaptedExposure * manualExposure();
    }

    private static float manualExposure() {
        float manual;
        try { manual = Float.parseFloat(System.getProperty("ac.vulkan.exposure", "1")); }
        catch (NumberFormatException ignored) { manual = 1; }
        return Float.isFinite(manual) ? Math.max(0.05f, Math.min(manual, 16)) : 1;
    }

    private static float linearColor(float value) {
        return value <= 0.04045f ? value / 12.92f : (float) Math.pow((value + 0.055f) / 1.055f, 2.4);
    }

    private void submitFrame(FrameRecording frame) {
        current.slot.submit();
        if (frame.exposureBuffer != null) {
            imageExposure.submitted(current.index);
            exposureCamera = frame.exposureCamera;
        }
        if (restir != null && frame.restirPass != null) {
            if (frame.restirCompute) restir.submitted(current.index);
            else restir.submittedCapture(current.index);
        }
        if (sharc != null && frame.sharc) sharc.submitted(current.index);
        if (frame.temporalAa) temporalAa.submitted(current.index);
        if (frame.jitterWorld) temporalSequence++;
        nativeRendering.submitted(current.index);
        if (rayScene != null && frame.rayModel != null) rayScene.submitted(current.index, current.slot::defer);
        frameSubmissions++;
        lastSubmission = current.slot;
        lastFrame = current;
        current.retention = frame.retention;
        current.world = frame.floatWorld && frame.world;
        current.linearWorld = frame.linearWorld;
        renderScaleStatistics = new RenderScaleStatistics(RENDER_PERCENTAGES[frame.renderScale], frame.worldWidth,
            frame.worldHeight, frame.width, frame.height, frame.scaled,
            renderScaleStatistics.scaledFrames() + (frame.scaled ? 1 : 0));
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
        private final ByteBuffer rayConstants;
        private final ByteBuffer fragmentConstants;
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
            rayConstants = stack.calloc(32);
            fragmentConstants = stack.calloc(32);
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
            if (capture) {
                var key = pass.key;
                pipeline = restirPipelines.get(new VulkanPipelines.Key(draw.geometry.topology, true, true,
                    key.depthFunc(), false, VK_BLEND_FACTOR_ONE, VK_BLEND_FACTOR_ZERO, key.cullMode(), key.frontFace(),
                    ColorMask.RGBA.bits(), key.depthBias(), key.variant().traced()));
            }
            if (boundPipeline != pipeline) {
                vkCmdBindPipeline(command, VK_PIPELINE_BIND_POINT_GRAPHICS, pipeline);
                pipelineBinds++;
                boundPipeline = pipeline;
            }
            bindGeometry(draw.geometry);
            bindDescriptors(draw);
            Geometry geometry = draw.geometry;
            if (draw.traced) {
                rayConstants.putLong(0, draw.rayGeometry.irradianceAddress()).putLong(8, draw.rayGeometry.vertexAddress())
                    .putInt(16, geometry.firstVertex).putInt(20, 0).putInt(24, 0)
                    .putInt(28, parallaxQuality(draw));
                vkCmdPushConstants(command, bindings.pipelineLayout(), VK_SHADER_STAGE_VERTEX_BIT | VK_SHADER_STAGE_FRAGMENT_BIT | VK_SHADER_STAGE_COMPUTE_BIT,
                    0, rayConstants);
                boolean reuse = !capture && frame.restirPass != null && frame.restirDirect && draw.worldSegment
                    && draw.phase == RenderPhase.TERRAIN_OPAQUE;
                fragmentConstants.putInt(0, draw.atlasBase).putInt(4, reuse ? 1 : 0)
                    .putLong(8, reuse ? frame.nativePlan.reconstruction() == 1 ? restir.rawResultAddress() : restir.resultAddress(current.index) : 0)
                    .putInt(16, frame.worldWidth).putInt(20, frame.worldHeight)
                    .putLong(24, !capture && frame.sharc && draw.worldSegment && draw.phase == RenderPhase.TERRAIN_OPAQUE
                        ? frame.nativePlan.reconstruction() == 1 ? sharc.rawResultAddress() : sharc.resultAddress(current.index) : 0);
                vkCmdPushConstants(command, bindings.pipelineLayout(), VK_SHADER_STAGE_VERTEX_BIT | VK_SHADER_STAGE_FRAGMENT_BIT,
                    32, fragmentConstants);
            }
            temporalConstants.putFloat(0, jitterWorld ? frame.jitterX : 0).putFloat(4, jitterWorld ? frame.jitterY : 0)
                .putFloat(8, draw.phase == RenderPhase.TERRAIN_OPAQUE && !pass.key.blend() ? 0 : 1)
                .putFloat(12, jitterWorld ? 1 : 0);
            writeTextureReactivity(draw);
            vkCmdPushConstants(command, bindings.pipelineLayout(), VK_SHADER_STAGE_VERTEX_BIT | VK_SHADER_STAGE_FRAGMENT_BIT,
                64, temporalConstants);
            if (geometry.indices != VK_NULL_HANDLE) {
                vkCmdDrawIndexed(command, geometry.count, 1, geometry.firstIndex, geometry.firstVertex, 0);
            } else {
                vkCmdDraw(command, geometry.count, 1, geometry.firstVertex, 0);
            }
        }

        private int parallaxQuality(Draw draw) {
            if (frame.parallaxMapping == 0 || !draw.worldSegment || draw.phase != RenderPhase.TERRAIN_OPAQUE) return 0;
            PreparedPass pass = draw.entry.pass;
            if (pass.key.blend() || !pass.key.depthTest() || !pass.key.depthWrite()) return 0;
            Matrix4 projection = pass.view.projection();
            // A perspective eye ray is required; custom/orthographic views use flat UVs.
            if (Math.abs(projection.get(3)) > 1e-6f || Math.abs(projection.get(7)) > 1e-6f
                || Math.abs(projection.get(15)) > 1e-6f || Math.abs(projection.get(11)) < 1e-6f) return 0;
            for (int index = 0; index < 16; index++) if (!Float.isFinite(projection.get(index))) return 0;
            return frame.parallaxMapping;
        }

        private void writeTextureReactivity(Draw draw) {
            for (int word = 0; word < 8; word++) temporalConstants.putInt(16 + word * 4, 0);
            SamplerState sampler = draw.texture.nativeTexture.samplerState();
            temporalConstants.putInt(48, textureAddressMode(sampler.wrapU()))
                .putInt(52, textureAddressMode(sampler.wrapV())).putInt(56, 0).putInt(60, 0);
            if ((!frame.jitterWorld && !frame.nativePlan.active()) || capture || !draw.worldSegment
                || draw.phase != RenderPhase.TERRAIN_OPAQUE || draw.entry.pass.key.blend()) return;
            appendTextureChanges(draw.texture, draw.texture.nativeTexture);
            if (draw.traced) {
                int atlas = draw.atlasBase / 3;
                appendTextureChanges(frame.boundRayNormals[atlas], draw.texture.nativeTexture);
                appendTextureChanges(frame.boundRaySpecular[atlas], draw.texture.nativeTexture);
            }
        }

        private void appendTextureChanges(Texture texture, VulkanTextures.Texture primary) {
            if (texture == null) return;
            var nativeTexture = texture.nativeTexture;
            int[] words = frame.textureChanges.computeIfAbsent(nativeTexture, source -> {
                ByteBuffer mask = ByteBuffer.allocate(32).order(java.nio.ByteOrder.nativeOrder());
                source.writeReactiveTiles(mask, 0, frameSubmissions);
                int[] result = new int[8];
                for (int word = 0; word < result.length; word++) result[word] = mask.getInt(word * 4);
                return result;
            });
            SamplerState sampler = primary.samplerState();
            SamplerState companion = nativeTexture.samplerState();
            if (sampler.wrapU() != companion.wrapU() || sampler.wrapV() != companion.wrapV()
                || primary.sampledWidth() != nativeTexture.sampledWidth()
                || primary.sampledHeight() != nativeTexture.sampledHeight()) {
                // Differing wrapping or filter footprints can affect UVs outside
                // the albedo's changed cells, including its border-only samples.
                for (int word : words) {
                    if (word == 0) continue;
                    for (int index = 0; index < 8; index++) temporalConstants.putInt(16 + index * 4, -1);
                    temporalConstants.putInt(56, 1);
                    return;
                }
            }
            for (int word = 0; word < words.length; word++) {
                int offset = 16 + word * 4;
                temporalConstants.putInt(offset, temporalConstants.getInt(offset) | words[word]);
            }
        }

        private static int textureAddressMode(SamplerState.AddressMode mode) {
            return switch (mode) {
                case REPEAT -> 0;
                case MIRRORED_REPEAT -> 1;
                case CLAMP_TO_EDGE -> 2;
                case CLAMP_TO_BORDER -> 3;
            };
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
            if (draw.traced && !boundRayScene) {
                handle.put(0, rayScene.descriptorSet(current.index));
                vkCmdBindDescriptorSets(command, VK_PIPELINE_BIND_POINT_GRAPHICS, bindings.pipelineLayout(),
                    VulkanBindings.RAY_SCENE, handle, null);
                descriptorBinds++;
                boundRayScene = true;
            }
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
        boolean world;
        boolean floatWorld;
        boolean linearWorld;
        int uiStart = -1;
        int uiWorldEnd = -1;
        int handsStart = -1;
        boolean temporalRequested;
        VulkanNativeRendering.Selection nativeSelection;
        VulkanNativeRendering.Plan nativePlan = VulkanNativeRendering.Plan.NONE;
        PreparedPass nativePass;
        boolean nativeRetried;
        int renderScale;
        int parallaxMapping;
        int worldWidth;
        int worldHeight;
        int worldEnd;
        boolean scaled;
        boolean streamedTerrain;
        boolean temporalAa;
        boolean jitterWorld;
        ExposureCamera exposureCamera;
        VulkanBuffer exposureBuffer;
        float exposureDelta;
        boolean exposureReset;
        int temporalWorldEnd;
        PreparedPass temporalPass;
        float jitterX, jitterY;
        EnvironmentState environment = EnvironmentState.NONE;
        TileMaterialTable materials = TileMaterialTable.empty();
        Matrix4 rayModel;
        final Texture[] rayAtlases = new Texture[4];
        final Texture[] rayNormalAtlases = new Texture[4];
        final Texture[] raySpecAtlases = new Texture[4];
        final Texture[] boundRayNormals = new Texture[4];
        final Texture[] boundRaySpecular = new Texture[4];
        final Set<Mesh> rayMeshes = java.util.Collections.newSetFromMap(new IdentityHashMap<>());
        RayLighting.Snapshot lighting;
        PreparedPass restirPass;
        boolean restirDirect;
        boolean restirCompute;
        boolean sharc;
        Color4 skyColor = new Color4(.55f, .7f, 1, 1);

        FrameRecording(int width, int height, AttachmentIntent intent, AttachmentRetention retention) {
            this.width = width;
            this.height = height;
            this.worldWidth = width;
            this.worldHeight = height;
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
                if (world && uiStart >= 0 && consumed >= uiStart) break;
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
            boolean preserveWorld = recording.floatWorld && recording.world && lastFrame.world
                && recording.linearWorld == lastFrame.linearWorld && plan.color == AttachmentIntent.LoadOp.LOAD;
            current.targets.preserveFrom(command, lastFrame.targets, plan.color == AttachmentIntent.LoadOp.LOAD,
                plan.depth == AttachmentIntent.LoadOp.LOAD, preserveWorld);
            if (recording.floatWorld && recording.world && !preserveWorld && plan.color == AttachmentIntent.LoadOp.LOAD) {
                postProcess.restore(command, current.index, current.targets.color, current.targets.world, recording.linearWorld);
            }
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
                .float32(0, hdr && recording.linearWorld ? linearColor(value.r()) : value.r())
                .float32(1, hdr && recording.linearWorld ? linearColor(value.g()) : value.g())
                .float32(2, hdr && recording.linearWorld ? linearColor(value.b()) : value.b())
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
            pipelines.size() + (worldPipelines == null ? 0 : worldPipelines.size())
                + (classicWorldPipelines == null ? 0 : classicWorldPipelines.size())
                + (restirPipelines == null ? 0 : restirPipelines.size()), preparedPasses, uniformWrites, viewWrites, materialWrites, pipelineBinds, descriptorBinds,
            dynamicStateWrites);
    }

    public record RayStatistics(long geometryBuilds, long sceneBuilds, int instances, long triangles, int memoryPages) { }

    public RayStatistics rayStatistics() {
        if (rayScene == null) return new RayStatistics(0, 0, 0, 0, 0);
        var statistics = rayScene.statistics();
        return new RayStatistics(statistics.geometryBuilds(), statistics.sceneBuilds(), statistics.instances(),
            statistics.triangles(), rayScene.memoryPages());
    }

    public record IrradianceStatistics(long jobs, long vertexUpdates, long submittedFrames, int cachedMeshes,
                                       int visibleMeshes, int pendingVisibleMeshes, int minimumVisibleSamples) { }

    public IrradianceStatistics irradianceStatistics() {
        if (rayScene == null) return new IrradianceStatistics(0, 0, 0, 0, 0, 0, 0);
        var statistics = rayScene.irradianceStatistics();
        return new IrradianceStatistics(statistics.jobs(), statistics.vertexUpdates(), statistics.submittedFrames(),
            statistics.cachedMeshes(), statistics.visibleMeshes(), statistics.pendingVisibleMeshes(), statistics.minimumVisibleSamples());
    }

    public record RestirStatistics(long frames, long resets, int width, int height, int historyFrames) { }

    public RestirStatistics restirStatistics() {
        if (restir == null) return new RestirStatistics(0, 0, 0, 0, 0);
        var statistics = restir.statistics();
        return new RestirStatistics(statistics.frames(), statistics.resets(), statistics.width(), statistics.height(), statistics.historyFrames());
    }

    public record SharcStatistics(long frames, long resets, int width, int height, int capacity, long cacheBytes,
                                  int historyFrames, long queryHits, long queryMisses, long updatePaths, long occupied) { }

    public SharcStatistics sharcStatistics() {
        if (sharc == null) return new SharcStatistics(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        var statistics = sharc.statistics();
        return new SharcStatistics(statistics.frames(), statistics.resets(), statistics.width(), statistics.height(),
            statistics.capacity(), statistics.cacheBytes(), statistics.historyFrames(), statistics.queryHits(),
            statistics.queryMisses(), statistics.updatePaths(), statistics.occupied());
    }

    public record GpuFrame(int width, int height, long image, long generatedImage, int frameSlot, long frameSerial) {
        public GpuFrame(int width, int height, long image) { this(width, height, image, 0, -1, 0); }
    }

    /** Native interpolation decisions become readable only after the producing render fence. */
    public long generatedFrame(GpuFrame frame) {
        ensureOpen();
        if (frame.generatedImage == 0 || frame.frameSlot < 0 || frame.frameSlot >= frames.length) return 0;
        FrameResources owner = frames[frame.frameSlot];
        if (owner.serial != frame.frameSerial) return 0;
        owner.slot.await();
        return nativeRendering.interpolationAllowed(frame.frameSlot) ? frame.generatedImage : 0;
    }

    public record Frame(int width, int height, ByteBuffer rgba, ByteBuffer depth) { }

    private interface Command { }

    private record PassEntry(PreparedPass pass, int viewIndex, int objectIndex, int materialIndex, Rect viewport) { }

    private record Geometry(long vertices, long indices, int firstVertex, int firstIndex, int count, int topology) { }

    private record Draw(Geometry geometry, Texture texture, long pipeline, PassEntry entry,
                        boolean traced, VulkanRayScene.Geometry rayGeometry, int atlasBase, RenderPhase phase,
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
        private VulkanRayScene.Geometry rayGeometry;
        private int rayAtlas;
        private boolean rayTranslucent;
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
                if (rayGeometry != null) rayGeometry.retire();
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
            if (rayGeometry != null) rayGeometry.close();
            rayGeometry = null;
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
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkFormatProperties props = VkFormatProperties.calloc(stack);
            vkGetPhysicalDeviceFormatProperties(context.physicalDevice(), DEPTH_FORMAT, props);
            if ((props.optimalTilingFeatures() & VK_FORMAT_FEATURE_DEPTH_STENCIL_ATTACHMENT_BIT) == 0) {
                throw new IllegalStateException("Vulkan device does not support D32 depth attachments");
            }
            sampledDepth = (props.optimalTilingFeatures() & VK_FORMAT_FEATURE_SAMPLED_IMAGE_BIT) != 0;
            int depthBlits = VK_FORMAT_FEATURE_BLIT_SRC_BIT | VK_FORMAT_FEATURE_BLIT_DST_BIT;
            depthBlitSupported = (props.optimalTilingFeatures() & depthBlits) == depthBlits;
            vkGetPhysicalDeviceFormatProperties(context.physicalDevice(), COLOR_FORMAT, props);
            int flags = VK_FORMAT_FEATURE_COLOR_ATTACHMENT_BIT | VK_FORMAT_FEATURE_SAMPLED_IMAGE_BIT;
            if ((props.optimalTilingFeatures() & flags) != flags) {
                throw new IllegalStateException("Vulkan device does not support RGBA8 color attachments and textures");
            }
            boolean outputStorage = (props.optimalTilingFeatures() & VK_FORMAT_FEATURE_STORAGE_IMAGE_BIT) != 0;
            vkGetPhysicalDeviceFormatProperties(context.physicalDevice(), WORLD_FORMAT, props);
            int hdrFlags = flags | VK_FORMAT_FEATURE_COLOR_ATTACHMENT_BLEND_BIT | VK_FORMAT_FEATURE_STORAGE_IMAGE_BIT
                | VK_FORMAT_FEATURE_SAMPLED_IMAGE_FILTER_LINEAR_BIT;
            var count = stack.mallocInt(1);
            vkGetPhysicalDeviceQueueFamilyProperties(context.physicalDevice(), count, null);
            var queues = VkQueueFamilyProperties.malloc(count.get(0), stack);
            vkGetPhysicalDeviceQueueFamilyProperties(context.physicalDevice(), count, queues);
            boolean compute = (queues.get(context.queueFamily()).queueFlags() & VK_QUEUE_COMPUTE_BIT) != 0;
            reactiveFormat = VulkanTemporalAa.reactiveFormat(context);
            if (reactiveFormat == VK_FORMAT_UNDEFINED) reactiveFormat = WORLD_FORMAT;
            hdrSupported = outputStorage && compute && (props.optimalTilingFeatures() & hdrFlags) == hdrFlags
                && !Boolean.getBoolean("ac.vulkan.disableHdr");
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
        renderPass = renderPasses.get(AttachmentIntent.LoadOp.LOAD, AttachmentIntent.LoadOp.LOAD, true, true);
        if (hdrSupported) {
            worldRenderPasses = new VulkanRenderPasses(context, WORLD_FORMAT, DEPTH_FORMAT, reactiveFormat);
            worldRenderPass = worldRenderPasses.get(AttachmentIntent.LoadOp.LOAD, AttachmentIntent.LoadOp.LOAD, true, true);
        }
    }

    private void resize(int width, int height) {
        current.targets.resize(width, height, COLOR_FORMAT, DEPTH_FORMAT);
        if (hdrSupported) uploads.initialize(current.targets.color, current.targets.depth, current.targets.world, current.targets.reactive);
        else uploads.initialize(current.targets.color, current.targets.depth);
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
        final VulkanFrameTargets targets = new VulkanFrameTargets(context, renderPass, worldRenderPass, reactiveFormat, sampledDepth);
        final VulkanFrameTargets scaledTargets = new VulkanFrameTargets(context, renderPass, worldRenderPass, reactiveFormat, sampledDepth);
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
        boolean world;
        boolean linearWorld;
        VulkanBuffer rayLighting;
        VulkanBuffer rayMaterials;
        VulkanBuffer rayEmitters;
        TileMaterialTable uploadedMaterials;

        FrameResources(int index) { this.index = index; }

        @Override
        public void close() {
            slot.close();
            viewUniforms.close();
            objectUniforms.close();
            materialUniforms.close();
            stream.close();
            targets.close();
            scaledTargets.close();
            if (rayLighting != null) rayLighting.close();
            if (rayMaterials != null) rayMaterials.close();
            if (rayEmitters != null) rayEmitters.close();
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
        if (nativeRendering != null) nativeRendering.close();
        nativeRendering = null;
        for (Runnable action : pendingRetirements) {
            action.run();
        }
        pendingRetirements.clear();
        for (Mesh mesh : List.copyOf(meshes)) {
            mesh.release();
        }
        if (rayScene != null) rayScene.close();
        rayScene = null;
        if (sharc != null) sharc.close();
        sharc = null;
        if (restirPipelines != null) restirPipelines.close();
        restirPipelines = null;
        if (restir != null) restir.close();
        restir = null;
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
        if (worldPipelines != null) worldPipelines.close();
        worldPipelines = null;
        if (classicWorldPipelines != null) classicWorldPipelines.close();
        classicWorldPipelines = null;
        if (temporalAa != null) temporalAa.close();
        temporalAa = null;
        if (upscale != null) upscale.close();
        upscale = null;
        if (imageExposure != null) imageExposure.close();
        imageExposure = null;
        if (postProcess != null) postProcess.close();
        postProcess = null;
        if (worldRenderPasses != null) worldRenderPasses.close();
        worldRenderPasses = null;
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
