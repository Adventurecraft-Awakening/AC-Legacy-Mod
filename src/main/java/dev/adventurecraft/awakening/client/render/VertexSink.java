package dev.adventurecraft.awakening.client.render;

import dev.adventurecraft.awakening.image.Rgba;

/** API-independent vertex attributes consumed by mesh builders and inherited tessellation adapters. */
public interface VertexSink {
    void ac$vertex(float x, float y, float z);
    void ac$tex(float u, float v);
    void ac$color8(int rgba);
    void ac$color32(float r, float g, float b, float a);
    void ac$normal8(int xyz);
    void ac$normal32(float x, float y, float z);
    /** Optional unlit tint/emission metadata; legacy geometry keeps the zero default. */
    default void ac$material(int material) { }
    /** Raw sky-light nibble for terrain; a negative value restores legacy geometry. */
    default void ac$skyLight(int skyLight) { }
    /** Water and captured rain exposure, independent of atlas, tint, and opacity. */
    default void ac$surfaceFlags(int flags) { }
    default void ac$vertexUV(float x, float y, float z, float u, float v) { ac$tex(u, v); ac$vertex(x, y, z); }
    default void ac$color8(byte r, byte g, byte b, byte a) { ac$color8(Rgba.fromRgba8(r, g, b, a)); }
    default void ac$color32(float r, float g, float b) { ac$color32(r, g, b, 1); }
    default void ac$splatColor8(byte luma) { ac$color8(luma, luma, luma, (byte) 255); }
    default void ac$splatColor32(float luma) { ac$color32(luma, luma, luma, 1); }
    default double getX() { return 0; }
    default double getY() { return 0; }
    default double getZ() { return 0; }
    default void vertex(double x, double y, double z) { ac$vertex((float)x,(float)y,(float)z); }
    default void vertexUV(double x, double y, double z, double u, double v) { ac$vertexUV((float)x,(float)y,(float)z,(float)u,(float)v); }
    default void tex(double u, double v) { ac$tex((float)u,(float)v); }
    default void color(float r, float g, float b) { ac$color32(r,g,b,1); }
    default void color(float r, float g, float b, float a) { ac$color32(r,g,b,a); }
    default void color(int r, int g, int b, int a) { ac$color8(Rgba.saturateFromRgba8(r,g,b,a)); }
    default void normal(float x, float y, float z) { ac$normal32(x,y,z); }
    default void addOffset(float x, float y, float z) { throw new UnsupportedOperationException("This vertex sink has no local origin"); }
}
