package dev.adventurecraft.awakening.client.render.state;

public record LightingState(LightState first, LightState second, Color4 ambient) {
    public static final LightingState NONE = new LightingState(
        new LightState(false, new Vector4(.2f, 1, -.7f, 0), new Color4(.6f, .6f, .6f, 1)),
        new LightState(false, new Vector4(-.2f, 1, .7f, 0), new Color4(.6f, .6f, .6f, 1)), new Color4(.4f, .4f, .4f, 1));
    public LightingState { java.util.Objects.requireNonNull(first); java.util.Objects.requireNonNull(second); java.util.Objects.requireNonNull(ambient); }
}
