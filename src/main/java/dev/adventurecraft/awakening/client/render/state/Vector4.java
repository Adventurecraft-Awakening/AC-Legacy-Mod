package dev.adventurecraft.awakening.client.render.state;

public record Vector4(float x, float y, float z, float w) {
    public static Vector4 of(float[] value) { return new Vector4(value[0], value[1], value[2], value[3]); }
}
