package dev.adventurecraft.awakening.client.gl;

import org.lwjgl.opengl.GL15;

public final class GLBuffer extends GLResource {

    private final long sizeInBytes;

    private GLBuffer(int handle, long sizeInBytes) {
        super(handle);
        this.sizeInBytes = (int) sizeInBytes;
    }

    GLBuffer(long sizeInBytes) {
        this(GL15.glGenBuffers(), sizeInBytes);
    }

    public @Override long sizeInBytes() {
        return this.sizeInBytes;
    }
}