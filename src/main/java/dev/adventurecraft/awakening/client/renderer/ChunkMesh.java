package dev.adventurecraft.awakening.client.renderer;

import dev.adventurecraft.awakening.client.gl.GLBuffer;
import dev.adventurecraft.awakening.client.gl.GLDevice;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

import java.nio.ByteBuffer;

public class ChunkMesh {

    public static final int MAX_RENDER_LAYERS = 2;
    public static final int MAX_TEXTURES = 4;

    private static final int BYTE_STRIDE = 32;

    public final GLBuffer vertexBuffer;
    public final int textureId;

    public ChunkMesh(GLBuffer vertexBuffer, int textureId) {
        this.vertexBuffer = vertexBuffer;
        this.textureId = textureId;
    }

    public void draw(GLDevice device) {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vertexBuffer.handle());

        // TODO: use VAO
        GL11.glVertexPointer(3, GL11.GL_FLOAT, BYTE_STRIDE, 0L);
        GL11.glTexCoordPointer(2, GL11.GL_FLOAT, BYTE_STRIDE, 12L);
        GL11.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, BYTE_STRIDE, 20L);
        GL11.glNormalPointer(GL11.GL_BYTE, BYTE_STRIDE, 24L);

        GL11.glDrawArrays(GL11.GL_QUADS, 0, (int) (this.vertexBuffer.sizeInBytes() / BYTE_STRIDE));
    }

    public void delete(GLDevice device) {
        device.delete(this.vertexBuffer);
    }

    public static ChunkMesh fromMemory(GLDevice device, MemoryMesh mesh, int textureId) {
        long size = mesh.sizeInBytes();
        GLBuffer buffer = device.newBuffer(size);
        device.alloc(buffer, GL15.GL_STATIC_DRAW);

        long offset = 0;
        for (ByteBuffer block : mesh.vertexBlocks) {
            device.uploadData(buffer, offset, block);
            offset += Integer.toUnsignedLong(block.remaining());
        }
        assert offset == size;

        return new ChunkMesh(buffer, textureId);
    }
}
