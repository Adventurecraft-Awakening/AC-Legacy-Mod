package dev.adventurecraft.awakening.client.gl;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL31;

public enum GLBufferTarget {

    ARRAY_BUFFER(GL15.GL_ARRAY_BUFFER),
    ELEMENT_BUFFER(GL15.GL_ELEMENT_ARRAY_BUFFER),
    COPY_READ_BUFFER(GL31.GL_COPY_READ_BUFFER),
    COPY_WRITE_BUFFER(GL31.GL_COPY_WRITE_BUFFER);

    public final int id;

    GLBufferTarget(int id) {
        this.id = id;
    }
}