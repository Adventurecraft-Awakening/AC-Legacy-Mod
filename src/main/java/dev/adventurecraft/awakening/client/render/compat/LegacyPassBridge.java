package dev.adventurecraft.awakening.client.render.compat;

import dev.adventurecraft.awakening.client.render.*;
import dev.adventurecraft.awakening.client.render.resource.RenderTexture;
import dev.adventurecraft.awakening.client.render.state.*;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.Textures;

/** The only entry boundary from inherited mutable state into explicit native passes. */
public final class LegacyPassBridge {
    private static boolean installed;
    private LegacyPassBridge() { }

    public static void installLifecycle() {
        if (installed) return;
        installed = true;
        RenderSystem.addLifecycle(new RenderSystem.Lifecycle() {
            @Override public void initialized(int width, int height) {
                LegacyGlAdapter.viewport(0, 0, width, height);
                LegacyGlAdapter.scissor(0, 0, width, height);
            }
            @Override public void submitted() { LegacyGlAdapter.retireResources(); }
            @Override public void closing() { LegacyGlAdapter.releaseResources(); }
        });
    }

    public static DrawParameters capture() { return LegacyGlAdapter.captureDrawState(true); }
    public static DrawParameters capture(boolean tint) { return LegacyGlAdapter.captureDrawState(tint); }
    public static DrawParameters capture(float[] modelView, float[] tint) { return LegacyGlAdapter.captureDrawState(modelView, tint); }
    /** Entity-dispatch boundary: model coordinates are camera-relative and the view is shared. */
    public static DrawParameters worldEntry() {
        var captured = capture();
        if (!RenderScene.hasWorld() || RenderSystem.phase() == RenderPhase.UI || RenderSystem.phase() == RenderPhase.HANDS) return captured;
        return captured.withView(RenderScene.world()).withObject(captured.object().withModel(Matrix4.identity()));
    }

    public static DrawParameters captureUi() { return RenderScene.ui() != null ? RenderScene.ui() : MaterialRecipe.UI.apply(capture(false)); }
    public static float[] modelView() { return LegacyGlAdapter.modelView(); }
    public static float[] currentTint() { return LegacyGlAdapter.currentTint(); }
    public static RenderTexture texture(int id) { return LegacyGlAdapter.textureForId(id); }
    public static RenderTexture currentTexture() { return texture(LegacyGlAdapter.currentTextureId()); }

    public static NativeRenderer.MeshPass modelPass(float[] modelView, float[] tint) { return NativeRenderer.pass(capture(modelView, tint)); }
    public static NativeRenderer.MeshPass scriptModelPass(float[] modelView, float[] tint) { return NativeRenderer.pass(MaterialRecipe.model(capture(modelView, tint))); }

    public static UiDrawContext uiContext(Font font, Textures textures) {
        return uiContext(captureUi(), font, textures);
    }
    public static UiDrawContext uiContext(DrawParameters parameters, Font font, Textures textures) {
        return new UiDrawContext(parameters, font,
            name -> name.startsWith("http")
                ? ((dev.adventurecraft.awakening.extension.client.ExTextureManager) textures).skin(name, "./pack.png")
                : ((dev.adventurecraft.awakening.extension.client.ExTextureManager) textures).texture(name),
            LegacyGlAdapter.textureReference(() -> font.fontTexture));
    }
    public static void publishView(FrameView view) { LegacyGlAdapter.installView(view); }
    public static void publishUi(DrawParameters parameters) { LegacyGlAdapter.installUi(parameters); }
    public static void publishFog(FogState fog) { LegacyGlAdapter.installFog(fog); }
    public static CameraView cameraView() { return new CameraView(capture()); }

    public static void drawLegacy(DrawParameters parameters, RenderTexture texture, Runnable callback) {
        LegacyGlAdapter.withDrawParameters(parameters, texture, callback);
    }
    public static void legacyUi(UiDrawContext context, Runnable callback, boolean depthLighting) {
        context.flush();
        var parameters = context.parameters();
        parameters = parameters.withObject(parameters.object().withModel(parameters.object().model().translated(
            (float) context.offsetX(), (float) context.offsetY(), 0)));
        if (depthLighting) parameters = HandLighting.apply(parameters,
            parameters.view().view().multiply(parameters.object().model()).rotated(120, 1, 0, 0))
            .withRaster(parameters.raster().withDepth(true, true));
        drawLegacy(parameters, context.fontTexture(), callback);
    }
}
