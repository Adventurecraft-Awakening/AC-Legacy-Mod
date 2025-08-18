package dev.adventurecraft.awakening.extension.client.render;

import org.lwjgl.util.vector.Vector3f;

public interface ExTesselator {

    void ac$vertex(float x, float y, float z);

    void ac$tex(float u, float v);

    void ac$color8(int rgba);

    void ac$color32(float r, float g, float b, float a);

    void ac$normal8(int xyz);

    void ac$normal32(float x, float y, float z);

    default void ac$normal32(Vector3f dir) {
        this.ac$normal32(dir.x, dir.y, dir.z);
    }

    default void ac$vertexUV(float x, float y, float z, float u, float v) {
        this.ac$tex(u, v);
        this.ac$vertex(x, y, z);
    }

    default void ac$vertexUV(Vector3f pos, float u, float v) {
        this.ac$vertexUV(pos.x, pos.y, pos.z, u, v);
    }

    default void ac$color8(byte r, byte g, byte b, byte a) {
        this.ac$color8(((a & 0xff) << 24) | ((b & 0xff) << 16) | ((g & 0xff) << 8) | (r & 0xff));
    }

    default void ac$color32(float r, float g, float b) {
        this.ac$color32(r, g, b, 1.0f);
    }

    default void ac$splatColor8(byte luma) {
        this.ac$color8(luma, luma, luma, (byte) 0xff);
    }

    default void ac$splatColor32(float luma) {
        this.ac$color32(luma, luma, luma, 1.0f);
    }

    default double getX() {
        return 0;
    }

    default double getY() {
        return 0;
    }

    default double getZ() {
        return 0;
    }
}
