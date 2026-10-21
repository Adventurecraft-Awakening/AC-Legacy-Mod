package dev.adventurecraft.awakening.client.render.resource;

import dev.adventurecraft.awakening.client.render.state.SamplerState;

public enum MipmapMode {
    NONE(SamplerState.MipmapMode.NONE), NEAREST(SamplerState.MipmapMode.NEAREST), LINEAR(SamplerState.MipmapMode.LINEAR);
    private final SamplerState.MipmapMode value;
    MipmapMode(SamplerState.MipmapMode value) { this.value = value; }
    public SamplerState.MipmapMode nativeValue() { return this.value; }
    public static MipmapMode fromNative(SamplerState.MipmapMode value) { return valueOf(value.name()); }
}
