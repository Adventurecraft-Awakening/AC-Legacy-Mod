package dev.adventurecraft.awakening.client.render;

/** Pure packing for the native vertex format's signed normalized eight-bit normal components. */
public final class VertexPacking {
    private VertexPacking() { }
    public static int packByteNormal(float x, float y, float z) {
        return component(x) | (component(y) << 8) | (component(z) << 16);
    }
    private static int component(float value) {
        return ((int) (Math.max(-1f,Math.min(value,1f))*127f)) & 255;
    }
}
