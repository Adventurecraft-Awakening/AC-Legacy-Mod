package dev.adventurecraft.awakening.client.render;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.client.render.texture.TextureStore;
import dev.adventurecraft.awakening.client.render.state.AttachmentIntent;
import dev.adventurecraft.awakening.client.render.state.ClearRequest;
import dev.adventurecraft.awakening.client.render.vulkan.VulkanDevice;
import dev.adventurecraft.awakening.client.render.vulkan.VulkanRenderer;
import dev.adventurecraft.awakening.client.render.vulkan.VulkanSwapchain;
import dev.adventurecraft.awakening.extension.client.options.ExGameOptions;
import net.minecraft.client.Minecraft;
import org.lwjgl.BufferUtils;

import java.util.ArrayList;
import java.util.List;

/** Owns the Vulkan device, frame lifecycle, and window presentation. */
public final class RenderSystem {
    private static VulkanDevice device;
    private static VulkanRenderer renderer;
    private static VulkanSwapchain swapchain;
    private static VulkanRenderer.Texture white;
    private static int width = 1, height = 1;
    private static boolean frame;
    private static long frameSequence;
    private static boolean drawable = true;
    private static boolean rayTracingRequested;
    private static int indirectLightingRequested;
    private static boolean temporalAaRequested;
    private static int renderScaleRequested;
    private static int parallaxMappingRequested;
    private static int rayReconstructionRequested, upscalerRequested, frameGenerationRequested;
    private static boolean rayTracingOptionsApplied;
    private static final DrawQueue DRAW_QUEUE = new DrawQueue();
    private static RenderPhase phase = RenderPhase.PREPARE;
    private static final List<Lifecycle> LISTENERS = new ArrayList<>();

    /** Optional integrations receive lifecycle events; native ownership stays here. */
    public interface Lifecycle {
        default void initialized(int width, int height) { }
        default void submitted() { }
        default void closing() { }
    }

    public static void addLifecycle(Lifecycle listener) { LISTENERS.add(listener); }

    private RenderSystem() { }

    public static void initialize(long window, int w, int h) {
        if (renderer != null) return;
        width = Math.max(1, w);
        height = Math.max(1, h);
        drawable = w > 0 && h > 0;
        device = VulkanDevice.createForWindow(window, Boolean.getBoolean("ac.vulkan.validation"));
        try {
            renderer = new VulkanRenderer(device);
            renderer.setRayTracingEnabled(rayTracingRequested);
            renderer.setIndirectLighting(indirectLightingRequested);
            renderer.setTemporalAaEnabled(temporalAaRequested);
            renderer.setRenderScale(renderScaleRequested);
            renderer.setParallaxMapping(parallaxMappingRequested);
            renderer.setRayReconstruction(rayReconstructionRequested);
            renderer.setUpscaler(upscalerRequested);
            renderer.setFrameGeneration(frameGenerationRequested);
            applyInitialRayTracingOption();
            swapchain = new VulkanSwapchain(device);
            white = renderer.uploadTexture(1, 1, BufferUtils.createByteBuffer(4).putInt(-1).flip());
            for (var listener : LISTENERS) listener.initialized(width, height);
        } catch (RuntimeException | Error error) {
            close();
            throw error;
        }
        ACMod.LOGGER.info("Vulkan game renderer: {} (native meshes, asynchronous frame slots)", device.deviceName());
    }

    public static int width() { return width; }
    public static int height() { return height; }
    public static boolean frameOpen() { return frame; }
    public static long frameSequence() { return frameSequence; }
    public static String deviceName() { return device == null ? "Vulkan" : device.deviceName(); }
    public static boolean isInitialized() { return renderer != null; }
    public static boolean rayTracingAvailable() { return renderer != null && renderer.rayTracingAvailable(); }
    public static boolean rayTracingEnabled() { return renderer != null && renderer.rayTracingEnabled(); }
    public static boolean sharcAvailable() { return renderer != null && renderer.sharcAvailable(); }
    public static String sharcUnavailableReason() { return renderer == null ? "Renderer is not initialized" : renderer.sharcUnavailableReason(); }
    public static boolean temporalAaAvailable() { return renderer != null && renderer.temporalAaSupported(); }
    public static boolean temporalAaEnabled() { return renderer != null && renderer.temporalAaEnabled(); }
    public static String temporalAaUnavailableReason() {
        if (renderer == null) return "Renderer is not initialized";
        String reason = renderer.temporalAaUnavailableReason();
        return reason == null ? "" : reason;
    }

    public static void setTemporalAaEnabled(boolean enabled) {
        temporalAaRequested = enabled;
        if (renderer != null) renderer.setTemporalAaEnabled(enabled);
    }

    public static boolean renderScaleAvailable() { return renderer != null && renderer.renderScaleSupported(); }
    public static String renderScaleUnavailableReason() {
        return renderer == null ? "Renderer is not initialized" : renderer.renderScaleUnavailableReason();
    }
    public static void setRenderScale(int mode) {
        renderScaleRequested = Math.clamp(mode, 0, 4);
        if (renderer != null) renderer.setRenderScale(renderScaleRequested);
    }

    public static boolean parallaxMappingAvailable() { return rayTracingAvailable(); }

    public static void setParallaxMapping(int mode) {
        parallaxMappingRequested = Math.clamp(mode, 0, 2);
        if (renderer != null) renderer.setParallaxMapping(parallaxMappingRequested);
    }

    public static void setIndirectLighting(int mode) {
        indirectLightingRequested = Math.clamp(mode, 0, 3);
        if (renderer != null) renderer.setIndirectLighting(indirectLightingRequested);
    }

    public static void setRayReconstruction(int mode) {
        rayReconstructionRequested = Math.clamp(mode, 0, 2);
        if (renderer != null) renderer.setRayReconstruction(rayReconstructionRequested);
    }
    public static void setUpscaler(int mode) {
        upscalerRequested = Math.clamp(mode, 0, 2);
        if (renderer != null) renderer.setUpscaler(upscalerRequested);
    }
    public static void setFrameGeneration(int mode) {
        frameGenerationRequested = Math.clamp(mode, 0, 2);
        if (renderer != null) renderer.setFrameGeneration(frameGenerationRequested);
    }

    /** Video settings may change before the window or during a native UI frame. */
    public static void setRayTracingEnabled(boolean enabled) {
        rayTracingRequested = enabled;
        if (renderer != null) renderer.setRayTracingEnabled(enabled);
    }

    private static void applyInitialRayTracingOption() {
        if (rayTracingOptionsApplied) return;
        Minecraft client = Minecraft.instance;
        if (client != null && client.options instanceof ExGameOptions options) {
            setRayTracingEnabled(options.rayTracing());
            setIndirectLighting(options.indirectLighting());
            setTemporalAaEnabled(options.temporalAa());
            setRenderScale(options.renderScale());
            setParallaxMapping(options.parallaxMapping());
            setRayReconstruction(options.rayReconstruction());
            setUpscaler(options.upscaler());
            setFrameGeneration(options.frameGeneration());
            rayTracingOptionsApplied = true;
        }
    }

    public static VulkanRenderer renderer() {
        if (renderer == null) throw new IllegalStateException("Vulkan display has not been created");
        return renderer;
    }

    public static VulkanRenderer.Texture whiteTexture() { return white; }

    /** Explicit native frame entry; inherited entry points can join an already open frame. */
    public static void beginFrame(AttachmentIntent intent) {
        if (frame) return;
        ++frameSequence;
        // Window creation can precede GameOptions construction; apply persisted opt-in once
        // the options object exists, before any world frame prepares tracing allocations.
        applyInitialRayTracingOption();
        NativeRenderer.collectRetiredMeshes();
        DRAW_QUEUE.beginFrame();
        RenderScene.beginFrame();
        phase = RenderPhase.PREPARE;
        renderer().beginFrame(width, height, intent);
        frame = true;
    }

    public static void ensureFrame() { if (!frame) beginFrame(AttachmentIntent.PRESERVE); }
    static DrawQueue drawQueue() {
        if (!frame) throw new IllegalStateException("Begin a render frame before submitting native draws");
        return DRAW_QUEUE;
    }
    public static void flushDraws() { if (frame) DRAW_QUEUE.flush(renderer(), phase); }
    public static void clear(ClearRequest request) {
        ensureFrame();
        flushDraws();
        renderer().clear(java.util.Objects.requireNonNull(request));
    }
    public static void phase(RenderPhase next) {
        if (!frame) throw new IllegalStateException("Begin a frame before entering a render phase");
        if (phase != next) {
            flushDraws();
            phase = java.util.Objects.requireNonNull(next);
            // Clears belong to the new phase even when no geometry has been queued yet.
            renderer().drawPhase(phase);
        }
    }
    public static RenderPhase phase() { return phase; }
    public record FrameStatistics(long draws, long streamedBytes, long uploadPreparationNanos, long recordingNanos) { }
    public static FrameStatistics frameStatistics() {
        return new FrameStatistics(DRAW_QUEUE.draws(), DRAW_QUEUE.bytes(), DRAW_QUEUE.preparationNanos(), DRAW_QUEUE.recordingNanos());
    }

    public static VulkanRenderer.GpuFrame finishFrame() {
        ensureFrame();
        flushDraws();
        var image = renderer.finishFrame();
        frame = false;
        for (var listener : LISTENERS) listener.submitted();
        return image;
    }

    public static void present() {
        if (renderer == null) return;
        var image = finishFrame();
        if (drawable) {
            long generated = renderer.generatedFrame(image);
            if (image.generatedImage() != 0) {
                swapchain.presentGeneratedPair(generated, image.image(), image.width(), image.height());
            } else {
                swapchain.present(image.image(), image.width(), image.height());
            }
        }
    }

    public static void resize(int w, int h) {
        drawable = w > 0 && h > 0;
        if (!drawable) return;
        if (frame) finishFrame();
        width = w;
        height = h;
        if (swapchain != null) swapchain.resize(w, h);
    }

    public static void setSwapInterval(int interval) {
        if (swapchain != null) swapchain.setSwapInterval(interval);
    }

    public static void waitIdle() {
        if (device != null) device.waitIdle();
    }

    public static void close() {
        try {
            if (renderer != null) {
                if (frame) finishFrame();
                NativeRenderer.close();
                DRAW_QUEUE.close();
                for (var listener : LISTENERS) listener.closing();
                TextureStore.releaseGpuResources();
                // Presentation may still be reading an offscreen image.
                if (swapchain != null) { swapchain.close(); swapchain = null; }
                renderer.close();
            }
            if (swapchain != null) swapchain.close();
        } finally {
            if (device != null) device.close();
            renderer = null; swapchain = null; device = null; white = null; frame = false;
            rayTracingRequested = false;
            indirectLightingRequested = 0;
            temporalAaRequested = false;
            renderScaleRequested = 0;
            parallaxMappingRequested = 0;
            rayReconstructionRequested = upscalerRequested = frameGenerationRequested = 0;
            rayTracingOptionsApplied = false;
        }
    }
}
