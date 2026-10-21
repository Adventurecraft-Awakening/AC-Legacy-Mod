package dev.adventurecraft.awakening.client.render;

import dev.adventurecraft.awakening.client.render.resource.RenderTexture;
import dev.adventurecraft.awakening.client.render.state.DrawParameters;
import dev.adventurecraft.awakening.client.render.state.MaterialRecipe;
import dev.adventurecraft.awakening.client.render.vulkan.VulkanRenderer;
import dev.adventurecraft.awakening.client.render.texture.TextureStore;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Native submission with explicit immutable draw and texture inputs. */
public final class NativeRenderer {
    private static final ReferenceQueue<Object> OWNER_QUEUE = new ReferenceQueue<>();
    private static final Set<OwnedMesh> OWNED_MESHES = new HashSet<>();
    private static final Set<RetainedMeshCache<?>> SHARED_CACHES = new HashSet<>();
    private static long generation;

    private NativeRenderer() { }

    public static VulkanRenderer.Mesh uploadQuads(ByteBuffer vertices) {
        return backend().uploadQuads(vertices);
    }

    public static VulkanRenderer.Mesh uploadQuads(List<ByteBuffer> blocks, int vertexCount) {
        return backend().uploadQuads(blocks, vertexCount);
    }

    public static VulkanRenderer.Mesh uploadWorldQuads(List<ByteBuffer> blocks, int vertexCount, int atlasId) {
        return backend().uploadWorldQuads(blocks, vertexCount, atlasId);
    }

    public static VulkanRenderer.Mesh uploadWorldQuads(List<ByteBuffer> blocks, int vertexCount, int atlasId, boolean translucent) {
        return backend().uploadWorldQuads(blocks, vertexCount, atlasId, translucent);
    }

    public static VulkanRenderer.Mesh uploadOccluderQuads(List<ByteBuffer> blocks, int vertexCount) {
        return backend().uploadOccluderQuads(blocks, vertexCount);
    }

    /** Called only when an already resident chunk is rebuilt with different surface content. */
    public static void terrainGeometryChanged() {
        if (RenderSystem.isInitialized()) backend().terrainGeometryChanged();
    }

    public static void worldAtlas(int atlas, RenderTexture texture) {
        if (!backend().rayTracingSupported()) return;
        backend().terrainTextureGeneration(TextureStore.materialGeneration());
        try (var snapshot = TextureStore.prepare(texture)) {
            backend().worldAtlas(atlas, snapshot.nativeTexture());
        }
    }

    /** Albedo, LabPBR normal/AO/height and LabPBR specular/emission for one atlas. */
    public static void worldAtlas(int atlas, RenderTexture albedo, RenderTexture normal, RenderTexture specular) {
        if (!backend().rayTracingSupported()) return;
        backend().terrainTextureGeneration(TextureStore.materialGeneration());
        try (var a = TextureStore.prepare(albedo);
             var n = TextureStore.prepare(normal);
             var s = TextureStore.prepare(specular)) {
            backend().worldAtlas(atlas, a.nativeTexture(), n.nativeTexture(), s.nativeTexture());
        }
    }

    /** Retirement always happens on the render thread, including GC retirement. */
    public static void track(Object owner, VulkanRenderer.Mesh mesh) {
        OWNED_MESHES.add(new OwnedMesh(owner, mesh));
    }

    public static void collectRetiredMeshes() {
        OwnedMesh reference;
        while ((reference = (OwnedMesh) OWNER_QUEUE.poll()) != null) {
            OWNED_MESHES.remove(reference);
            reference.mesh.close();
        }
    }

    static void registerCache(RetainedMeshCache<?> cache) { SHARED_CACHES.add(cache); }

    public static void close() {
        for (var cache : SHARED_CACHES) cache.clear();
        for (OwnedMesh reference : OWNED_MESHES) reference.mesh.close();
        OWNED_MESHES.clear();
        while (OWNER_QUEUE.poll() != null) { }
        generation++;
    }

    public static long generation() { return generation; }

    public static MeshPass pass(DrawParameters parameters) { return new MeshPass(parameters,false); }
    public static MeshPass pass(MaterialRecipe recipe,DrawParameters parameters) { return pass(recipe.apply(parameters)); }
    public static void warmup(MaterialRecipe recipe,DrawParameters parameters) {
        backend().warmup(recipe.apply(parameters),recipe==MaterialRecipe.DEBUG_LINES?
            VulkanRenderer.PrimitiveTopology.LINES:VulkanRenderer.PrimitiveTopology.TRIANGLES);
    }

    /** Explicit opt-in for batches whose equal-depth order cannot affect their output. */
    public static MeshPass unorderedOpaquePass(DrawParameters parameters) { return new MeshPass(parameters, true); }

    public static void drawMesh(VulkanRenderer.Mesh mesh, RenderTexture texture, DrawParameters parameters) {
        pass(parameters).draw(mesh, texture);
    }

    public static void drawQuads(ByteBuffer vertices, RenderTexture texture, DrawParameters parameters) {
        if (vertices.hasRemaining()) pass(parameters).drawQuads(vertices, texture);
    }

    public static void drawLines(ByteBuffer vertices, RenderTexture texture, DrawParameters parameters) {
        if (vertices.hasRemaining()) pass(parameters).drawLines(vertices, texture);
    }

    private static VulkanRenderer backend() {
        return RenderSystem.renderer();
    }

    public static final class MeshPass implements AutoCloseable {
        private final VulkanRenderer.PreparedPass state;
        private final DrawParameters parameters;
        private final boolean reorderable;

        private MeshPass(DrawParameters parameters, boolean reorderable) { this.parameters = parameters; this.reorderable = reorderable; this.state = backend().preparePass(parameters); }

        public void draw(VulkanRenderer.Mesh mesh, RenderTexture texture) {
            RenderSystem.drawQueue().retained(mesh, texture, this.parameters, this.state, this.reorderable);
        }

        public void drawQuads(ByteBuffer vertices, RenderTexture texture) {
            if (!vertices.hasRemaining()) return;
            RenderSystem.drawQueue().streamed(vertices, false, texture, this.parameters, this.state, this.reorderable);
        }

        /** Frame-backed builders submit their existing reservation without copying vertex bytes. */
        public void drawQuads(QuadBuilder vertices,RenderTexture texture) {
            if(vertices.isEmpty()) return;
            if(vertices.isFrameOwned()) vertices.streamQuads(stream->RenderSystem.drawQueue().streamed(stream,texture,parameters,state,reorderable));
            else drawQuads(vertices.vertices(),texture);
        }
        public void drawLines(QuadBuilder vertices,RenderTexture texture) {
            if(vertices.isEmpty()) return;
            if(vertices.isFrameOwned()) vertices.streamLines(stream->RenderSystem.drawQueue().streamed(stream,texture,parameters,state,reorderable));
            else drawLines(vertices.records(),texture);
        }

        public void drawLines(ByteBuffer vertices, RenderTexture texture) {
            if (!vertices.hasRemaining()) return;
            RenderSystem.drawQueue().streamed(vertices, true, texture, this.parameters, this.state, this.reorderable);
        }

        public void close() { }
    }

    private static final class OwnedMesh extends WeakReference<Object> {
        private final VulkanRenderer.Mesh mesh;

        private OwnedMesh(Object owner, VulkanRenderer.Mesh mesh) {
            super(owner, OWNER_QUEUE);
            this.mesh = mesh;
        }
    }
}
