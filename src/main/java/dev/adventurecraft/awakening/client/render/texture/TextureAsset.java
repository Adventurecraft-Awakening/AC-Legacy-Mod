package dev.adventurecraft.awakening.client.render.texture;

import java.util.Objects;

/** A resource path and its sampling policy, independent from compatibility integer names. */
public record TextureAsset(String path, TextureLoadOptions options) {
    public TextureAsset(String path) { this(path, TextureLoadOptions.DEFAULT); }
    public TextureAsset { Objects.requireNonNull(path, "path"); Objects.requireNonNull(options, "options"); }
}
