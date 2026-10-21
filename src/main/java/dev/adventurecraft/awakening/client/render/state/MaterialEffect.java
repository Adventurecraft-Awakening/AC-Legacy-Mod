package dev.adventurecraft.awakening.client.render.state;

/** Explicit shader behavior; ordinary textured materials always use NORMAL. */
public enum MaterialEffect {
    NORMAL(0),
    ATMOSPHERE(1),
    CLOUDS(2),
    SKY_BACKGROUND(-1);

    private final int shaderCode;

    MaterialEffect(int shaderCode) { this.shaderCode = shaderCode; }

    public int shaderCode() { return this.shaderCode; }
}
