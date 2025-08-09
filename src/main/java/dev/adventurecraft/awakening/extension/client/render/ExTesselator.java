package dev.adventurecraft.awakening.extension.client.render;

import dev.adventurecraft.awakening.util.MathF;

public interface ExTesselator {

    void ac$vertex(float x, float y, float z);

    void ac$tex(float u, float v);

    void ac$color(int rgba);

    default void ac$vertexUV(float x, float y, float z, float u, float v) {
        this.ac$tex(u, v);
        this.ac$vertex(x, y, z);
    }

    default void ac$color(int r, int g, int b, int a) {
        this.ac$color(a << 24 | b << 16 | g << 8 | r);
    }

    default void ac$splatColor(float luma) {
        int l = MathF.clamp((int) (luma * 255.0F), 0, 255);
        this.ac$color(l, l, l, 255);
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
