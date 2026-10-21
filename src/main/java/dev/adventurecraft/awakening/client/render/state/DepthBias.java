package dev.adventurecraft.awakening.client.render.state;

public record DepthBias(float factor, float units) {
    public static final DepthBias NONE = new DepthBias(0, 0);
    public boolean enabled() { return factor != 0 || units != 0; }
}
