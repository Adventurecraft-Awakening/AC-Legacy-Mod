package dev.adventurecraft.awakening.client.renderer;

import dev.adventurecraft.awakening.client.gl.*;
import dev.adventurecraft.awakening.client.rendering.MemoryTesselator;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;

public final class ChunkMesh {

    public static final int MAX_RENDER_LAYERS = 2;
    public static final int MAX_TEXTURES = 4;

    public final GLBuffer vertexBuffer;
    public final GLBuffer indexBuffer;
    public final GLElementType indexType;
    public final int textureId;

    public ChunkMesh(GLBuffer vertexBuffer, GLBuffer indexBuffer, GLElementType indexType, int textureId) {
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
        return this.indexBuffer.sizeInBytes() / this.indexType.size;
    }

    public void draw(GLDevice device) {
        device.bind(GLBufferTarget.ARRAY_BUFFER, this.vertexBuffer);
        device.bind(GLBufferTarget.ELEMENT_BUFFER, this.indexBuffer);

        // TODO: use VAO
        int stride = this.vertexStride();
        GL11.glVertexPointer(3, GL11.GL_FLOAT, stride, 0L);
        GL11.glTexCoordPointer(2, GL11.GL_FLOAT, stride, 12L);
        GL11.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, stride, 20L);
        GL11.glNormalPointer(GL11.GL_BYTE, stride, 24L);

        GL11.glDrawElements(GL11.GL_TRIANGLES, (int) this.getIndexCount(), this.indexType.id, 0);
    }

    public void delete(GLDevice device) {
        device.delete(this.vertexBuffer);
    }

    public static ChunkMesh fromMemory(GLDevice device, MemoryMesh mesh, int textureId) {
        var vertexBuffer = makeVertexBuffer(device, mesh);

        var indexType = GLElementType.fromCount(mesh.getVertexCount());
        var indexBuffer = makeIndexBuffer(device, mesh, indexType);

        return new ChunkMesh(vertexBuffer, indexBuffer, indexType, textureId);
    }

    private static GLBuffer makeVertexBuffer(GLDevice device, MemoryMesh mesh) {
        final var target = GLBufferTarget.ARRAY_BUFFER;

        // TODO: allocate from arena
        GLBuffer buffer = device.newBuffer(mesh.getSizeInBytes());
        device.bind(target, buffer);
        device.alloc(target, buffer.sizeInBytes(), GLBufferUsage.STATIC_DRAW);

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

    private static GLBuffer makeIndexBuffer(GLDevice device, MemoryMesh mesh, GLElementType indexType) {
        final var quadTarget = GLBufferTarget.COPY_READ_BUFFER;
        final var indexTarget = GLBufferTarget.ELEMENT_BUFFER;

        long quadCount = mesh.getVertexCount() / 4;
        long indexByteSize = device.bindQuadElements(quadTarget, indexType, quadCount);

        // TODO: allocate from arena
        GLBuffer buffer = device.newBuffer(indexByteSize);
        device.bind(indexTarget, buffer);
        device.alloc(indexTarget, buffer.sizeInBytes(), GLBufferUsage.STATIC_DRAW);

        device.copyBuffer(quadTarget, 0, indexTarget, 0, indexByteSize);
        return buffer;
    }
}
