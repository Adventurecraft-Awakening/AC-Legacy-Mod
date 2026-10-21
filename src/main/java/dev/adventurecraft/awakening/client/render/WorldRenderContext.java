package dev.adventurecraft.awakening.client.render;

import dev.adventurecraft.awakening.client.render.state.*;

/** One immutable eye view and its camera-relative drawing policy. */
public record WorldRenderContext(FrameView view, Rect viewport, double cameraX, double cameraY, double cameraZ,
                                 int eye, ColorMask colorMask) {
    public WorldRenderContext {
        java.util.Objects.requireNonNull(view);
        java.util.Objects.requireNonNull(viewport);
        java.util.Objects.requireNonNull(colorMask);
    }

    public WorldRenderContext withFog(FogState fog) {
        return new WorldRenderContext(view.withFog(fog), viewport, cameraX, cameraY, cameraZ, eye, colorMask);
    }

    public WorldRenderContext withLights(LightingState lights) {
        return new WorldRenderContext(view.withLights(lights), viewport, cameraX, cameraY, cameraZ, eye, colorMask);
    }

    public WorldRenderContext withEnvironment(EnvironmentState environment) {
        return new WorldRenderContext(view.withEnvironment(environment), viewport, cameraX, cameraY, cameraZ, eye, colorMask);
    }

    public WorldRenderContext withColorMask(ColorMask mask) {
        return new WorldRenderContext(view, viewport, cameraX, cameraY, cameraZ, eye, mask);
    }

    public DrawParameters parameters() {
        return new DrawParameters(view, ObjectData.IDENTITY, MaterialRecipe.TERRAIN_OPAQUE.material(),
            MaterialRecipe.TERRAIN_OPAQUE.raster(RasterState.defaults()).withViewport(viewport).withColorMask(colorMask));
    }

    public DrawParameters terrain(boolean translucent) {
        var parameters = (translucent ? MaterialRecipe.TERRAIN_TRANSLUCENT : MaterialRecipe.TERRAIN_OPAQUE).apply(parameters());
        return parameters.withObject(parameters.object().withModel(Matrix4.identity().translated(
            (float) -cameraX, (float) -cameraY, (float) -cameraZ)));
    }
}
