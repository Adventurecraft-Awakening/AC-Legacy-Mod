package dev.adventurecraft.awakening.client.gl;

import org.lwjgl.opengl.GL11;

public enum GLTextureTarget {

    TEXTURE_2D(GL11.GL_TEXTURE_2D);

    public final int symbol;

    GLTextureTarget(int symbol) {
        this.symbol = symbol;
    }
}
