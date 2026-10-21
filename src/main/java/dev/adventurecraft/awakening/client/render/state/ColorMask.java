package dev.adventurecraft.awakening.client.render.state;

public record ColorMask(boolean red, boolean green, boolean blue, boolean alpha) {
    public static final ColorMask RGBA = new ColorMask(true, true, true, true);
    public static final ColorMask NONE = new ColorMask(false, false, false, false);
    public int bits() { return (red ? 1 : 0) | (green ? 2 : 0) | (blue ? 4 : 0) | (alpha ? 8 : 0); }
}
