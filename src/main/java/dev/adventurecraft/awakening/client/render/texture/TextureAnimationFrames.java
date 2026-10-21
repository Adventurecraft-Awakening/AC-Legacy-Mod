package dev.adventurecraft.awakening.client.render.texture;

import dev.adventurecraft.awakening.image.ImageBuffer;
import dev.adventurecraft.awakening.image.ImageFormat;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

/** Immutable RGBA data for legacy square strips and explicitly sized vertical animation strips. */
public final class TextureAnimationFrames {
    public enum Layout { HORIZONTAL, VERTICAL }

    private static final int CHANNELS = 4;
    private final int sourceWidth;
    private final int sourceHeight;
    private final int frameWidth;
    private final int frameHeight;
    private final int frameCount;
    private final int frameBytes;
    private final Layout layout;
    private final ByteBuffer pixels;

    private TextureAnimationFrames(int sourceWidth, int sourceHeight, int frameWidth, int frameHeight, int frameCount,
                                   int frameBytes, Layout layout, ByteBuffer pixels) {
        this.sourceWidth = sourceWidth;
        this.sourceHeight = sourceHeight;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.frameCount = frameCount;
        this.frameBytes = frameBytes;
        this.layout = layout;
        this.pixels = pixels.asReadOnlyBuffer().order(ByteOrder.nativeOrder());
    }

    /**
     * Takes a deep byte snapshot; channel values, including RGB at alpha zero, are never converted.
     * The longer side must be an exact multiple of the shorter side. A square is one vertical frame.
     */
    public static TextureAnimationFrames from(ImageBuffer source) {
        validateSource(source);
        int width = source.getWidth(), height = source.getHeight();
        int size = Math.min(width, height);
        int longSide = Math.max(width, height);
        if (longSide % size != 0) {
            throw new IllegalArgumentException("Animation strip must contain whole square frames: " + width + "x" + height);
        }
        return snapshot(source, size, size, width > height ? Layout.HORIZONTAL : Layout.VERTICAL);
    }

    /** Explicit animations.txt frame dimensions retain the legacy vertical rectangular-strip layout. */
    public static TextureAnimationFrames from(ImageBuffer source, int frameWidth, int frameHeight) {
        validateSource(source);
        if (frameWidth <= 0 || frameHeight <= 0) {
            throw new IllegalArgumentException("Animation frame dimensions must be positive");
        }
        if (source.getWidth() != frameWidth || source.getHeight() % frameHeight != 0) {
            throw new IllegalArgumentException("Vertical animation strip must match frame width and contain whole frames");
        }
        return snapshot(source, frameWidth, frameHeight, Layout.VERTICAL);
    }

    private static void validateSource(ImageBuffer source) {
        Objects.requireNonNull(source, "source");
        if (source.getFormat() != ImageFormat.RGBA_U8) {
            throw new IllegalArgumentException("Animation strips require RGBA_U8 pixels");
        }
        int width = source.getWidth(), height = source.getHeight();
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Animation strip dimensions must be positive: " + width + "x" + height);
        }
        long rowBytes = (long) width * CHANNELS;
        // Check the row before multiplication so even hostile dimensions cannot overflow a long.
        if (rowBytes > Integer.MAX_VALUE || rowBytes * height > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Animation strip exceeds the supported byte size: " + width + "x" + height);
        }
        int sourceStride = source.getByteStride();
        long storedBytes = (long) sourceStride * height;
        if (sourceStride < rowBytes || storedBytes > source.getBuffer().limit()) {
            throw new IllegalArgumentException("Animation strip pixels do not fit their declared row stride and dimensions");
        }
    }

    private static TextureAnimationFrames snapshot(ImageBuffer source, int frameWidth, int frameHeight, Layout layout) {
        int count = layout == Layout.HORIZONTAL ? source.getWidth() / frameWidth : source.getHeight() / frameHeight;
        int frameBytes = Math.toIntExact((long) frameWidth * frameHeight * CHANNELS);
        int frameRowBytes = Math.multiplyExact(frameWidth, CHANNELS);
        int totalBytes = Math.multiplyExact(frameBytes, count);
        ByteBuffer snapshot = ByteBuffer.allocateDirect(totalBytes).order(ByteOrder.nativeOrder());
        // Store frame-major data. Horizontal strips require a separate row slice for every frame.
        for (int frame = 0; frame < count; frame++) {
            int sourceX = layout == Layout.HORIZONTAL ? frame * frameRowBytes : 0;
            int sourceY = layout == Layout.VERTICAL ? frame * frameHeight : 0;
            for (int y = 0; y < frameHeight; y++) {
                snapshot.put(source.getRowSlice(sourceY + y).slice(sourceX, frameRowBytes));
            }
        }
        snapshot.flip();
        return new TextureAnimationFrames(source.getWidth(), source.getHeight(), frameWidth, frameHeight,
            count, frameBytes, layout, snapshot);
    }

    public int sourceWidth() { return sourceWidth; }
    public int sourceHeight() { return sourceHeight; }
    public int frameWidth() { return frameWidth; }
    public int frameHeight() { return frameHeight; }
    /** Convenience for square legacy strips; explicitly rectangular animations have two dimensions. */
    public int frameSize() {
        if (frameWidth != frameHeight) throw new IllegalStateException("Animation frames are rectangular");
        return frameWidth;
    }
    public int frameCount() { return frameCount; }
    public Layout layout() { return layout; }
    public boolean horizontal() { return layout == Layout.HORIZONTAL; }

    /** Companion strips must match source dimensions, frame count, and orientation exactly. */
    public boolean matchesLayout(TextureAnimationFrames other) {
        return other != null && sourceWidth == other.sourceWidth && sourceHeight == other.sourceHeight
            && frameWidth == other.frameWidth && frameHeight == other.frameHeight
            && frameCount == other.frameCount && layout == other.layout;
    }

    /** Returns an isolated read-only frame view; frameCount wraps to zero and negative indices wrap too. */
    public ImageBuffer frame(int index) {
        int wrapped = Math.floorMod(index, frameCount);
        ByteBuffer view = pixels.slice(wrapped * frameBytes, frameBytes)
            .asReadOnlyBuffer().order(ByteOrder.nativeOrder());
        return ImageBuffer.wrap(view, frameWidth, frameHeight, ImageFormat.RGBA_U8);
    }

    /** Inserts a companion suffix before the filename extension without changing resource folders. */
    public static String siblingPath(String path, String suffix) {
        Objects.requireNonNull(path, "path");
        if (!"_n".equals(suffix) && !"_s".equals(suffix)) {
            throw new IllegalArgumentException("Companion suffix must be _n or _s");
        }
        int separator = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        if (path.length() <= separator + 1) {
            throw new IllegalArgumentException("Animation resource path must contain a filename");
        }
        int extension = path.lastIndexOf('.');
        if (extension <= separator + 1) return path + suffix;
        return path.substring(0, extension) + suffix + path.substring(extension);
    }
}
