package dev.adventurecraft.awakening.image;

public final class Rgba {

    public static int fromBgra(int bgra) {
        return fromRgba8(bgra >>> 16, bgra >>> 8, bgra, bgra >>> 24);
    }

    public static int fromRgb8(byte r, byte g, byte b) {
        return fromRgba8(r, g, b, -1);
    }

    public static int fromRgb8(int r, int g, int b) {
        return fromRgba8(r, g, b, -1);
    }

    public static int fromRgba8(byte r, byte g, byte b, byte a) {
        return fromRgba8(r & 0xff, g & 0xff, b & 0xff, a & 0xff);
    }

    public static int fromRgba8(int r, int g, int b, int a) {
        return (r & 0xff) |
            ((g & 0xff) << 8) |
            ((b & 0xff) << 16) |
            ((a & 0xff) << 24);
    }

    public static int withRgb(int rgba, int rgb) {
        return (rgba & 0xff000000) | (rgb & 0xffffff);
    }

    public static int withAlpha(int rgba, int alpha) {
        int a = (alpha & 0xff) << 24;
        return (rgba & 0xffffff) | a;
    }

    public static int alphaOrOpaque(int rgba) {
        if ((rgba >>> 24) == 0) {
            rgba |= 0xff000000;
        }
        return rgba;
    }

    public static int getAlpha(int rgba) {
        return rgba >>> 24;
    }
}
