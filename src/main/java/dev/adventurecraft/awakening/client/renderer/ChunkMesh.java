package dev.adventurecraft.awakening.client.renderer;

import dev.adventurecraft.awakening.client.gl.*;
import dev.adventurecraft.awakening.client.rendering.MemoryTesselator;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;

public final class ChunkMesh {

    public static final int MAX_RENDER_LAYERS = 2;
    public static final int MAX_TEXTURES = 4;

    public final GLBuffer vertexBuffer;
    public final @Nullable GLBuffer indexBuffer;
    public final GLElementType indexType;
    public final int textureId;

    public ChunkMesh(GLBuffer vertexBuffer, @Nullable GLBuffer indexBuffer, GLElementType indexType, int textureId) {
        this.vertexBuffer = vertexBuffer;
        this.indexBuffer = indexBuffer;
        this.indexType = indexType;
        this.textureId = textureId;
    }

    private int vertexStride() {
        return MemoryTesselator.BYTE_STRIDE;
    }

    public long getVertexCount() {
        return this.vertexBuffer.sizeInBytes() / this.vertexStride();
    }

    public long getIndexCount() {
        if (this.indexBuffer == null) {
            return 0;
        }
        return this.indexBuffer.sizeInBytes() / this.indexType.size;
    }

    public void draw(GLDevice device) {
        device.bind(GLBufferTarget.ARRAY_BUFFER, this.vertexBuffer);

        long indexCount;
        if (this.indexBuffer != null) {
            device.bind(GLBufferTarget.ELEMENT_BUFFER, this.indexBuffer);
            indexCount = this.getIndexCount();
        }
        else {
            long quadCount = this.getVertexCount() / 4;
            device.bindQuadElements(GLBufferTarget.ELEMENT_BUFFER, this.indexType, quadCount);
            indexCount = quadCount * 6;
        }

        // TODO: use VAO
        int stride = this.vertexStride();
        GL11.glVertexPointer(3, GL11.GL_FLOAT, stride, 0L);
        GL11.glTexCoordPointer(2, GL11.GL_FLOAT, stride, 12L);
        GL11.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, stride, 20L);
        GL11.glNormalPointer(GL11.GL_BYTE, stride, 24L);

        GL11.glDrawElements(GL11.GL_TRIANGLES, Math.toIntExact(indexCount), this.indexType.symbol, 0);
    }

    public void delete(GLDevice device) {
        device.delete(this.vertexBuffer);
        if (this.indexBuffer != null) {
            device.delete(this.indexBuffer);
        }
    }

    public static ChunkMesh fromMemory(GLDevice device, MemoryMesh mesh, int textureId) {
        var vertexBuffer = makeVertexBuffer(device, mesh);

        var indexType = GLElementType.fromCount(mesh.getVertexCount());
        //var indexBuffer = makeIndexBuffer(device, mesh, indexType);

        return new ChunkMesh(vertexBuffer, null /*indexBuffer*/, indexType, textureId);
    }

    private static GLBuffer makeVertexBuffer(GLDevice device, MemoryMesh mesh) {
        final var target = GLBufferTarget.ARRAY_BUFFER;

        // TODO: allocate from arena
        GLBuffer buffer = device.newBuffer(mesh.getSizeInBytes());
        device.bind(target, buffer);
        device.alloc(target, buffer.sizeInBytes(), GLBufferUsage.STATIC_DRAW);

        // TODO: upload to staging buffer and copy to target to avoid perf warning
        long offset = 0;
        for (ByteBuffer block : mesh.vertexBlocks) {
            device.uploadData(target, offset, block);
            offset += Integer.toUnsignedLong(block.remaining());
        }
        if (offset != buffer.sizeInBytes()) {
            throw new AssertionError("incorrect vertex offset");
        }
        return buffer;
    }

    // TODO: This may not be necessary anymore if we just use a big shared buffer.
    //       Keeping it for now as a reminder for non-quad chunk meshes (if we ever do that).
    private static GLBuffer makeIndexBuffer(GLDevice device, MemoryMesh mesh, GLElementType indexType) {
        final var indexTarget = GLBufferTarget.ELEMENT_BUFFER;
        long quadCount = mesh.getVertexCount() / 4;

        // TODO: allocate from arena
        long indexByteSize = GLDevice.byteSizeForQuadElements(indexType, quadCount);
        GLBuffer buffer = device.newBuffer(indexByteSize);
        device.bind(indexTarget, buffer);
        device.alloc(indexTarget, buffer.sizeInBytes(), GLBufferUsage.STATIC_DRAW);

        final var quadTarget = GLBufferTarget.COPY_READ_BUFFER;
        device.bindQuadElements(quadTarget, indexType, quadCount);
        device.copyBuffer(quadTarget, 0, indexTarget, 0, indexByteSize);

        return buffer;
    }
}
