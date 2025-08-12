package dev.adventurecraft.awakening.client.gl;

import org.lwjgl.opengl.GL15;

public enum GLBufferUsage {

    STATIC_DRAW(GL15.GL_STATIC_DRAW),
    STATIC_COPY(GL15.GL_STATIC_COPY);

    public final int id;

    GLBufferUsage(int id) {
        this.id = id;
    }
}