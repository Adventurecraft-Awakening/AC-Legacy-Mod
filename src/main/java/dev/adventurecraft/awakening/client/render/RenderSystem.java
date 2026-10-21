package dev.adventurecraft.awakening.client.render;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.client.render.texture.TextureStore;
import dev.adventurecraft.awakening.client.render.state.AttachmentIntent;
import dev.adventurecraft.awakening.client.render.state.ClearRequest;
import dev.adventurecraft.awakening.client.render.vulkan.VulkanDevice;
import dev.adventurecraft.awakening.client.render.vulkan.VulkanRenderer;
import dev.adventurecraft.awakening.client.render.vulkan.VulkanSwapchain;
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
    public static VulkanRenderer renderer() {
        if (renderer == null) throw new IllegalStateException("Vulkan display has not been created");
        return renderer;
    }

    public static VulkanRenderer.Texture whiteTexture() { return white; }

    /** Explicit native frame entry; inherited entry points can join an already open frame. */
    public static void beginFrame(AttachmentIntent intent) {
        if (frame) return;
        ++frameSequence;
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
            swapchain.present(image.image(), image.width(), image.height());
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
        }
    }
}
