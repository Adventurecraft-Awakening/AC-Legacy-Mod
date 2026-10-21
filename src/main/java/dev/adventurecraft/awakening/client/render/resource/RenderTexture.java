package dev.adventurecraft.awakening.client.render.resource;

import dev.adventurecraft.awakening.client.render.state.SamplerState;
import dev.adventurecraft.awakening.client.render.texture.TextureStore;
import dev.adventurecraft.awakening.layout.Size;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/** Native texture ownership. Borrowed views never dispose their owner's storage. */
public final class RenderTexture implements AutoCloseable {
    private final RenderTexture owner;
    private final java.util.function.Supplier<RenderTexture> assetSource;
    private final SamplerState viewSampler;
    private final int viewBase, viewMax;
    private boolean closed;

    private RenderTexture(RenderTexture owner) { this(owner, null, 0, 0); }
    private RenderTexture(RenderTexture owner, SamplerState sampler, int baseLevel, int maxLevel) {
        this.owner = owner; this.assetSource = null; this.viewSampler = sampler; this.viewBase = baseLevel; this.viewMax = maxLevel;
    }
    private RenderTexture(java.util.function.Supplier<RenderTexture> source) {
        this.owner = null; this.assetSource = java.util.Objects.requireNonNull(source);
        this.viewSampler = null; this.viewBase = this.viewMax = 0;
    }
    /** A catalog-owned logical asset keeps resolving its current immutable image after reload. */
    public static RenderTexture assetReference(java.util.function.Supplier<RenderTexture> source) { return new RenderTexture(source); }

    public static RenderTexture create(TextureDesc descriptor) {
        RenderTexture texture = new RenderTexture((RenderTexture) null);
        TextureStore.register(texture, descriptor);
        return texture;
    }

    public static RenderTexture create(int width, int height, int mipLevels) {
        return create(new TextureDesc(width, height, mipLevels));
    }

    /** Storage identity for the native store; it carries no legacy or Vulkan numeric name. */
    public RenderTexture storageOwner() {
        if (this.closed) throw new IllegalStateException("Texture reference is closed");
        if (this.assetSource != null) return this.assetSource.get().storageOwner();
        return this.owner == null ? this : this.owner.storageOwner();
    }

    private RenderTexture referenceOwner() {
        if (this.closed) throw new IllegalStateException("Texture reference is closed");
        return this.owner == null ? this : this.owner.referenceOwner();
    }
    public RenderTexture borrow() { return new RenderTexture(this.referenceOwner(), this.viewSampler, this.viewBase, this.viewMax); }
    public RenderTexture sampled(int baseLevel, int maxLevel, SamplerState sampler) {
        if (baseLevel < 0 || maxLevel < baseLevel) throw new IllegalArgumentException("Invalid sampled mip range");
        return new RenderTexture(this.referenceOwner(), java.util.Objects.requireNonNull(sampler), baseLevel, maxLevel);
    }
    private void requireMutableSampling() {
        this.storageOwner();
        if (this.viewSampler != null) throw new IllegalStateException("Sampled texture views are immutable");
    }
    public boolean isClosed() { return this.closed || this.owner != null && this.owner.isClosed()
        || this.assetSource != null && this.assetSource.get().isClosed(); }
    public TextureDesc descriptor() { return TextureStore.descriptor(this); }
    public Size size(int level) { return TextureStore.size(this, level); }
    public long sizeInBytes() { return TextureStore.sizeInBytes(this); }
    public ByteBuffer readLevel(int level) { return TextureStore.readLevel(this, level); }
    public void upload(int level, int x, int y, int width, int height, ByteBuffer rgba) {
        TextureStore.upload(this, level, x, y, width, height, rgba);
    }
    public void upload(int level, int x, int y, int width, int height, IntBuffer rgba) {
        TextureStore.upload(this, level, x, y, width, height, rgba);
    }
    public SamplerState sampler() { this.storageOwner(); return this.viewSampler == null ? TextureStore.sampler(this) : this.viewSampler; }
    public void setSampler(SamplerState sampler) { this.requireMutableSampling(); TextureStore.setSampler(this, sampler); }
    public int baseLevel() { this.storageOwner(); return this.viewSampler == null ? TextureStore.baseLevel(this) : this.viewBase; }
    public int maxLevel() { this.storageOwner(); return this.viewSampler == null ? TextureStore.maxLevel(this) : this.viewMax; }
    public void setMipRange(int baseLevel, int maxLevel) { this.requireMutableSampling(); TextureStore.setMipRange(this, baseLevel, maxLevel); }
    public void setFilter(TextureFilter min, TextureFilter mag, MipmapMode mipmap) {
        SamplerState old = this.sampler();
        this.setSampler(new SamplerState(min.nativeValue(), mag.nativeValue(), mipmap.nativeValue(), old.wrapU(), old.wrapV()));
    }
    public void setClamp(boolean clamp) {
        SamplerState old = this.sampler();
        var address = clamp ? SamplerState.AddressMode.CLAMP_TO_EDGE : SamplerState.AddressMode.REPEAT;
        this.setSampler(new SamplerState(old.min(), old.mag(), old.mipmap(), address, address));
    }
    @Override public void close() {
        if (this.closed) return;
        if (this.owner == null && this.assetSource == null) TextureStore.delete(this);
        this.closed = true;
    }
}
