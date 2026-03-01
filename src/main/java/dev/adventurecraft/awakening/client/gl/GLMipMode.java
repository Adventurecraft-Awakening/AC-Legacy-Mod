package dev.adventurecraft.awakening.client.gl;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public enum GLMipMode {

    NONE(GL12.GL_NEAREST, GL12.GL_LINEAR),
    NEAREST(GL12.GL_NEAREST_MIPMAP_NEAREST, GL12.GL_LINEAR_MIPMAP_NEAREST),
    LINEAR(GL12.GL_NEAREST_MIPMAP_LINEAR, GL12.GL_LINEAR_MIPMAP_LINEAR);

    public final int nearestSymbol;
    public final int linearSymbol;

    GLMipMode(int nearestSymbol, int linearSymbol) {
        this.nearestSymbol = nearestSymbol;
        this.linearSymbol = linearSymbol;
    }

    public static GLMipMode fromFilterSymbol(int symbol) {
        return switch (symbol) {
            case GL11.GL_NEAREST, GL11.GL_LINEAR -> NONE;
            case GL12.GL_NEAREST_MIPMAP_NEAREST, GL12.GL_LINEAR_MIPMAP_NEAREST -> NEAREST;
            case GL12.GL_NEAREST_MIPMAP_LINEAR, GL12.GL_LINEAR_MIPMAP_LINEAR -> LINEAR;
            default -> throw new IllegalArgumentException("unexpected symbol: " + symbol);
        };
    }
}
