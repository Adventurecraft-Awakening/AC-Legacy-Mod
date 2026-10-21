package dev.adventurecraft.awakening.client.render.state;

/** Explicit initial attachment contents for a native frame. DISCARD makes prior contents unspecified. */
public record AttachmentIntent(LoadOp color, Color4 clearColor, LoadOp depth, float clearDepth) {
    public enum LoadOp { LOAD, CLEAR, DISCARD }
    public static final AttachmentIntent PRESERVE = new AttachmentIntent(LoadOp.LOAD, Color4.BLACK, LoadOp.LOAD, 1);
    public AttachmentIntent { java.util.Objects.requireNonNull(color); java.util.Objects.requireNonNull(clearColor); java.util.Objects.requireNonNull(depth); }
    public static AttachmentIntent clear(Color4 color, float depth) { return new AttachmentIntent(LoadOp.CLEAR, color, LoadOp.CLEAR, depth); }
}
