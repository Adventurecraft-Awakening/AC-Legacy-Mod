package dev.adventurecraft.awakening.client.gl;

import org.lwjgl.opengl.GL15;

import java.nio.ByteBuffer;

public final class GLDevice {

    public GLBuffer newBuffer(long sizeInBytes) {
        return new GLBuffer(sizeInBytes);
    }

    public void delete(GLResource resource) {
        resource.delete();
    }

    public void alloc(GLBuffer buffer, int usage) {
        int target = GL15.GL_ARRAY_BUFFER;
        GL15.glBindBuffer(target, buffer.handle());
        GL15.glBufferData(target, buffer.sizeInBytes(), usage);
    }

    public void uploadData(GLBuffer buffer, long offset, ByteBuffer data) {
        int target = GL15.GL_ARRAY_BUFFER;
        GL15.glBindBuffer(target, buffer.handle());
        GL15.glBufferSubData(target, offset, data);
    }
}
