package dev.adventurecraft.awakening.client.render.state;

/** Vulkan clip-space projection and independent camera view; rectangles use a top-left origin. */
public record FrameView(Matrix4 projection, Matrix4 view, FogState fog, LightingState lights, EnvironmentState environment) {
    public FrameView { java.util.Objects.requireNonNull(projection); java.util.Objects.requireNonNull(view); java.util.Objects.requireNonNull(fog); java.util.Objects.requireNonNull(lights); java.util.Objects.requireNonNull(environment); }
    public FrameView(Matrix4 projection, Matrix4 view, FogState fog, LightingState lights) { this(projection, view, fog, lights, EnvironmentState.NONE); }
    public static FrameView ui(int width, int height) { return new FrameView(Matrix4.orthographicTopLeft(width, height), Matrix4.identity(), FogState.NONE, LightingState.NONE); }
    public FrameView withFog(FogState value) { return new FrameView(projection, view, value, lights, environment); }
    public FrameView withLights(LightingState value) { return new FrameView(projection, view, fog, value, environment); }
    public FrameView withProjection(Matrix4 value) { return new FrameView(value, view, fog, lights, environment); }
    public FrameView withView(Matrix4 value) { return new FrameView(projection, value, fog, lights, environment); }
    public FrameView withEnvironment(EnvironmentState value) { return new FrameView(projection, view, fog, lights, value); }
}
