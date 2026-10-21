package dev.adventurecraft.awakening.client.render.texture;

/** Immutable per-asset policy. Global mip and anaglyph preferences are captured when loading. */
public record TextureLoadOptions(boolean blur, boolean clamp, DataType dataType) {
    public enum DataType { COLOR, NORMAL, SPECULAR }

    public TextureLoadOptions(boolean blur, boolean clamp) { this(blur, clamp, DataType.COLOR); }

    public TextureLoadOptions { java.util.Objects.requireNonNull(dataType); }

    public static final TextureLoadOptions DEFAULT = new TextureLoadOptions(false, false);
}
