package dev.adventurecraft.awakening.client.render.state;

public record LightState(boolean enabled, Vector4 position, Color4 diffuse) {
    public LightState { java.util.Objects.requireNonNull(position); java.util.Objects.requireNonNull(diffuse); }
}
