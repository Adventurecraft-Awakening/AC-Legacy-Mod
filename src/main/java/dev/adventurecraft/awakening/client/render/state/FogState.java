package dev.adventurecraft.awakening.client.render.state;

public record FogState(FogMode mode, FogDistance distance, Color4 color, float start, float end, float density) {
    public static final FogState NONE = new FogState(FogMode.NONE, FogDistance.EYE_PLANE, Color4.TRANSPARENT, 0, 1, 0);
    public FogState { java.util.Objects.requireNonNull(mode); java.util.Objects.requireNonNull(distance); java.util.Objects.requireNonNull(color); }
}
