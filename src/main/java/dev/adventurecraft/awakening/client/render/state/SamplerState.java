package dev.adventurecraft.awakening.client.render.state;

public record SamplerState(Filter min, Filter mag, MipmapMode mipmap, AddressMode wrapU, AddressMode wrapV) {
    public enum Filter { NEAREST, LINEAR }
    public enum MipmapMode { NONE, NEAREST, LINEAR }
    public enum AddressMode { REPEAT, MIRRORED_REPEAT, CLAMP_TO_EDGE, CLAMP_TO_BORDER }
    public static final SamplerState NEAREST_REPEAT = new SamplerState(Filter.NEAREST, Filter.NEAREST, MipmapMode.NONE, AddressMode.REPEAT, AddressMode.REPEAT);
    public SamplerState { java.util.Objects.requireNonNull(min); java.util.Objects.requireNonNull(mag); java.util.Objects.requireNonNull(mipmap); java.util.Objects.requireNonNull(wrapU); java.util.Objects.requireNonNull(wrapV); }
}
