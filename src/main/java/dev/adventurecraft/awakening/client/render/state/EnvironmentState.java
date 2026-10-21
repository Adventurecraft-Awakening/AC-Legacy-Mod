package dev.adventurecraft.awakening.client.render.state;

/** One world-frame atmosphere snapshot, laid out as six vec4 values on the GPU. */
public record EnvironmentState(Vector4 sun, Color4 sky, Vector4 weather, Vector4 camera,
                               Vector4 clouds, Vector4 fog) {
    private static final Vector4 ZERO = new Vector4(0, 0, 0, 0);
    public static final EnvironmentState NONE = new EnvironmentState(
        ZERO, Color4.TRANSPARENT, ZERO, ZERO, ZERO, ZERO);

    public EnvironmentState {
        java.util.Objects.requireNonNull(sun);
        java.util.Objects.requireNonNull(sky);
        java.util.Objects.requireNonNull(weather);
        java.util.Objects.requireNonNull(camera);
        java.util.Objects.requireNonNull(clouds);
        java.util.Objects.requireNonNull(fog);
    }
}
