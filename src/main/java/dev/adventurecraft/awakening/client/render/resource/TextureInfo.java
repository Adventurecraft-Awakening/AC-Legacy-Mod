package dev.adventurecraft.awakening.client.render.resource;

import dev.adventurecraft.awakening.client.render.state.SamplerState;
import dev.adventurecraft.awakening.layout.Size;

/** Texture metadata and sampling snapshot, independent of legacy names or bindings. */
public final class TextureInfo {
    public final String name;
    public final RenderTexture texture;
    public final Size size;
    public final int baseLevel;
    public final int maxLevel;
    public final MipmapMode mipMode;
    public final TextureFilter minFilter;
    public final TextureFilter magFilter;
    private final SamplerState sampler;

    public TextureInfo(String name, RenderTexture texture) {
        this.name = name;
        this.texture = texture;
        this.size = texture.size(0);
        this.baseLevel = texture.baseLevel();
        this.maxLevel = texture.maxLevel();
        this.sampler = texture.sampler();
        this.mipMode = MipmapMode.fromNative(this.sampler.mipmap());
        this.minFilter = TextureFilter.fromNative(this.sampler.min());
        this.magFilter = TextureFilter.fromNative(this.sampler.mag());
    }

    public void restoreParameters() {
        this.texture.setSampler(this.sampler);
        this.texture.setMipRange(this.baseLevel, this.maxLevel);
    }

    @Override public String toString() {
        return "TextureInfo{name='%s', size=%s, format=RGBA8, baseLevel=%d, maxLevel=%d, sampler=%s}"
            .formatted(this.name, this.size, this.baseLevel, this.maxLevel, this.sampler);
    }
}
