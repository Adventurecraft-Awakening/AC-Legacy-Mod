package dev.adventurecraft.awakening.client.render.state;

public record BlendState(boolean enabled, BlendFactor source, BlendFactor destination, Color4 constants) {
    public static final BlendState OPAQUE = new BlendState(false, BlendFactor.ONE, BlendFactor.ZERO, Color4.TRANSPARENT);
    public static final BlendState ALPHA = new BlendState(true, BlendFactor.SRC_ALPHA, BlendFactor.ONE_MINUS_SRC_ALPHA, Color4.TRANSPARENT);
    public BlendState { java.util.Objects.requireNonNull(source); java.util.Objects.requireNonNull(destination); java.util.Objects.requireNonNull(constants); }
}
