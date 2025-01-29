package dev.adventurecraft.awakening.image;

public final class Rgba {

    public static int fromBgra(int bgra) {
        return fromRgba8(bgra >>> 16, bgra >>> 8, bgra, bgra >>> 24);
    }

    public static int fromRgb8(byte r, byte g, byte b) {
        return fromRgba8(r, g, b, (byte) 255);
    }

    public static int fromRgb8(int r, int g, int b) {
        return fromRgba8(r, g, b, 0xff);
    }

    public static int fromRgba8(byte r, byte g, byte b, byte a) {
        return fromRgba8((int) r, g, b, a);
    }

    public static int fromRgba8(int r, int g, int b, int a) {
        return (r & 0xff) |
            ((g & 0xff) << 8) |
            ((b & 0xff) << 16) |
            ((a & 0xff) << 24);
    }
}
