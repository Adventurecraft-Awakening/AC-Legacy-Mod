package dev.adventurecraft.awakening.client.gl;

import org.lwjgl.opengl.GL15;

public enum GLBufferUsage {

    STATIC_DRAW(GL15.GL_STATIC_DRAW),
    STATIC_COPY(GL15.GL_STATIC_COPY),
    DYNAMIC_DRAW(GL15.GL_DYNAMIC_DRAW),
    DYNAMIC_COPY(GL15.GL_DYNAMIC_COPY);

    public final int symbol;

    GLBufferUsage(int symbol) {
        this.symbol = symbol;
    }
}