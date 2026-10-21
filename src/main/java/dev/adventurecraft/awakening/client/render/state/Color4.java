package dev.adventurecraft.awakening.client.render.state;

public record Color4(float r, float g, float b, float a) {
    public static final Color4 WHITE = new Color4(1, 1, 1, 1);
    public static final Color4 BLACK = new Color4(0, 0, 0, 1);
    public static final Color4 TRANSPARENT = new Color4(0, 0, 0, 0);
    public static Color4 of(float[] value) { return new Color4(value[0], value[1], value[2], value[3]); }
    public float[] copyArray() { return new float[]{r, g, b, a}; }
}
