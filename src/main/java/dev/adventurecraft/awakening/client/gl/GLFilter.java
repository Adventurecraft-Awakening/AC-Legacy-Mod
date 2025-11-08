package dev.adventurecraft.awakening.client.gl;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public enum GLFilter {

    NEAREST(GL11.GL_NEAREST),
    LINEAR(GL11.GL_LINEAR);

    public final int symbol;

    GLFilter(int symbol) {
        this.symbol = symbol;
    }

    public int getMinSymbol(GLMipMode mode) {
        return switch (mode) {
            case GLMipMode.NEAREST -> mode.nearestSymbol;
            case GLMipMode.LINEAR -> mode.linearSymbol;
            default -> this.symbol;
        };
    }

    public int getMagSymbol(GLMipMode mode) {
        return this.symbol;
    }

    public static GLFilter fromSymbol(int symbol) {
        return switch (symbol) {
            case GL11.GL_NEAREST, GL12.GL_NEAREST_MIPMAP_NEAREST, GL12.GL_NEAREST_MIPMAP_LINEAR -> NEAREST;
            case GL11.GL_LINEAR, GL12.GL_LINEAR_MIPMAP_NEAREST, GL12.GL_LINEAR_MIPMAP_LINEAR -> LINEAR;
            default -> throw new IllegalArgumentException("unexpected symbol: " + symbol);
        };
    }
}
