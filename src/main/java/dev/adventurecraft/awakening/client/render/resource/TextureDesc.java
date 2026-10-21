package dev.adventurecraft.awakening.client.render.resource;

/** Immutable native image allocation. Pixel updates never change its dimensions or mip count. */
public record TextureDesc(int width, int height, int mipLevels, Format format) {
    public enum Format { RGBA8 }
    public TextureDesc(int width, int height, int mipLevels) { this(width, height, mipLevels, Format.RGBA8); }
    public TextureDesc {
        if (width < 1 || height < 1 || mipLevels < 1
            || mipLevels > 32 - Integer.numberOfLeadingZeros(Math.max(width, height))) {
            throw new IllegalArgumentException("Invalid texture allocation");
        }
        java.util.Objects.requireNonNull(format, "format");
        Math.multiplyExact(Math.multiplyExact(width, height), Integer.BYTES);
    }
    public int width(int level) { checkLevel(level); return Math.max(1, width >> level); }
    public int height(int level) { checkLevel(level); return Math.max(1, height >> level); }
    private void checkLevel(int level) {
        if (level < 0 || level >= mipLevels) throw new IllegalArgumentException("Mip level outside texture allocation");
    }
}
