package dev.adventurecraft.awakening.client.render.state;

/** An ordered clear, optionally restricted to a rectangle and selected color channels. */
public record ClearRequest(boolean clearColor, boolean clearDepth, Color4 color, float depth, ColorMask colorMask, Rect region) {
    public ClearRequest { java.util.Objects.requireNonNull(color); java.util.Objects.requireNonNull(colorMask); }
    public static ClearRequest all(Color4 color, float depth) { return new ClearRequest(true, true, color, depth, ColorMask.RGBA, null); }
}
