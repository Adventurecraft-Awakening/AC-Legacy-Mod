package dev.adventurecraft.awakening.client.render;

import dev.adventurecraft.awakening.client.render.state.*;

/** Authoritative per-view camera data shared by world drawing, model extraction and picking. */
public final class RenderScene {
    private static WorldRenderContext world;
    private static DrawParameters ui;
    private RenderScene() { }

    public static void beginFrame() { world = null; ui = null; }
    public static void world(FrameView view, Rect pixels, double x, double y, double z, int currentEye) {
        world(new WorldRenderContext(view, pixels, x, y, z, currentEye, ColorMask.RGBA));
    }
    public static void world(WorldRenderContext context) { world = java.util.Objects.requireNonNull(context); }
    public static boolean hasWorld() { return world != null; }
    public static FrameView world() {
        return worldContext().view();
    }
    public static WorldRenderContext worldContext() {
        if (world == null) throw new IllegalStateException("The world camera has not been prepared");
        return world;
    }
    public static Rect viewport() { return worldContext().viewport(); }
    public static double cameraX() { return worldContext().cameraX(); }
    public static double cameraY() { return worldContext().cameraY(); }
    public static double cameraZ() { return worldContext().cameraZ(); }
    public static int eye() { return worldContext().eye(); }
    public static ColorMask colorMask() { return worldContext().colorMask(); }
    public static void colorMask(ColorMask mask) { world = worldContext().withColorMask(mask); }
    public static void fog(FogState fog) { if (world != null) world = world.withFog(fog); }
    public static void lighting(LightingState lights) { if (world != null) world = world.withLights(lights); }

    public static DrawParameters worldParameters() {
        return worldContext().parameters();
    }
    public static DrawParameters terrainParameters(boolean translucent, ColorMask mask) {
        return worldContext().withColorMask(mask).terrain(translucent);
    }
    public static DrawParameters ui(double width, double height) {
        float[] projection = {2F/(float)width,0,0,0,0,2F/(float)height,0,0,0,0,-1F/2000,0,-1,-1,-.5F,1};
        var view = new FrameView(new Matrix4(projection), Matrix4.identity().translated(0, 0, -2000), FogState.NONE, LightingState.NONE);
        ui = new DrawParameters(view, ObjectData.IDENTITY, MaterialRecipe.UI.material(),
            MaterialRecipe.UI.raster(RasterState.defaults()).withViewport(new Rect(0, 0, RenderSystem.width(), RenderSystem.height())));
        return ui;
    }
    public static DrawParameters ui() { return ui; }
    public static CameraView camera() { return new CameraView(worldParameters()); }
}
