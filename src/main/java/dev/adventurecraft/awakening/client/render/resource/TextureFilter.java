package dev.adventurecraft.awakening.client.render.resource;

import dev.adventurecraft.awakening.client.render.state.SamplerState;

public enum TextureFilter {
    NEAREST(SamplerState.Filter.NEAREST), LINEAR(SamplerState.Filter.LINEAR);
    private final SamplerState.Filter value;
    TextureFilter(SamplerState.Filter value) { this.value = value; }
    public SamplerState.Filter nativeValue() { return this.value; }
    public static TextureFilter fromNative(SamplerState.Filter value) {
        return value == SamplerState.Filter.LINEAR ? LINEAR : NEAREST;
    }
}
