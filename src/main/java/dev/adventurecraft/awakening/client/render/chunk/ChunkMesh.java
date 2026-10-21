package dev.adventurecraft.awakening.client.render.chunk;

import dev.adventurecraft.awakening.client.render.NativeRenderer;
import dev.adventurecraft.awakening.client.render.mesh.MemoryMesh;
import dev.adventurecraft.awakening.client.render.resource.RenderTexture;
import dev.adventurecraft.awakening.client.render.resource.RenderDevice;
import dev.adventurecraft.awakening.client.render.vulkan.VulkanRenderer;

/** An immutable terrain allocation with shared quad indices owned by the renderer. */
public final class ChunkMesh {

    public static final int MAX_RENDER_LAYERS = 2;
    public static final int MAX_TEXTURES = 4;

    private final VulkanRenderer.Mesh mesh;
    private final int vertexCount;
    private final long sizeInBytes;
    private boolean deleted;
    public final int textureId;

    private ChunkMesh(VulkanRenderer.Mesh mesh, int vertexCount, long sizeInBytes, int textureId) {
        this.mesh = mesh;
        this.vertexCount = vertexCount;
        this.sizeInBytes = sizeInBytes;
        this.textureId = textureId;
    }

    public long getVertexCount() {
        return this.vertexCount;
    }

    public long getIndexCount() {
        return (long) this.vertexCount / 4 * 6;
    }

    public void draw(NativeRenderer.MeshPass pass, RenderTexture texture) {
        if (this.deleted) {
            throw new IllegalStateException("Drawing a deleted terrain mesh");
        }
        pass.draw(this.mesh, texture);
    }

    public void delete(RenderDevice device) {
        if (!this.deleted) {
            this.deleted = true;
            this.mesh.close();
            device.meshDeleted(this.sizeInBytes);
        }
    }

    public static ChunkMesh fromMemory(RenderDevice device, MemoryMesh source, int textureId, int layer) {
        int count = Math.toIntExact(source.getVertexCount());
        long size = source.getSizeInBytes();
        VulkanRenderer.Mesh mesh = NativeRenderer.uploadWorldQuads(source.vertexBlocks, count, textureId, layer != 0);
        device.meshAllocated(size);
        return new ChunkMesh(mesh, count, size, textureId);
    }

}
