package dev.adventurecraft.awakening.client.render;

/** Runtime availability and effective selections for optional reconstruction backends. */
public final class NativeRenderingOptions {
    public enum Group { RECONSTRUCTION, UPSCALER, FRAME_GENERATION }
    public record Status(boolean available, String reason) {
        public Status { reason = reason == null ? "" : reason; }
        public static Status ready() { return new Status(true, ""); }
        public static Status unavailable(String reason) { return new Status(false, reason); }
    }
    private NativeRenderingOptions() { }
    public static Status status(Group group, int mode) {
        if (mode == 0) return Status.ready();
        if (mode < 0 || mode > 2) return Status.unavailable("Unknown rendering mode");
        return RenderSystem.isInitialized() ? RenderSystem.renderer().nativeRenderingStatus(group, mode)
            : Status.unavailable("Renderer is not initialized");
    }
    public static int effective(Group group) {
        return RenderSystem.isInitialized() ? RenderSystem.renderer().nativeRenderingEffective(group) : 0;
    }
}
