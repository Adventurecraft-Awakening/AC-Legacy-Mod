package dev.adventurecraft.awakening.image;

import dev.adventurecraft.awakening.util.MathF;

public final class Rgba {

    public static final int WHITE = 0xffffffff;
    public static final int BLACK = 0xff000000;

    public static int fromBgra(int bgra) {
        return fromRgba8(bgra >>> 16, bgra >>> 8, bgra, bgra >>> 24);
    }

    public static int fromRgb8(byte r, byte g, byte b) {
        return fromRgba8(r, g, b, 0xff);
    }

    public static int fromRgb8(int r, int g, int b) {
        return fromRgba8(r, g, b, 0xff);
    }

    public static int fromRgba8(byte r, byte g, byte b, byte a) {
        return fromRgba8(r & 0xff, g & 0xff, b & 0xff, a & 0xff);
    }

    public static int fromRgba8(int r, int g, int b, int a) {
        return (r & 0xff) | ((g & 0xff) << 8) | ((b & 0xff) << 16) | ((a & 0xff) << 24);
    }

    public static int saturateFromRgba8(int r, int g, int b, int a) {
        return fromRgba8(
            MathF.clamp(r, 0, 255),
            MathF.clamp(g, 0, 255),
            MathF.clamp(b, 0, 255),
            MathF.clamp(a, 0, 255)
        );
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

    public static int red(int rgba) {
        return rgba & 0xff;
    }

    public static int green(int rgba) {
        return (rgba >>> 8) & 0xff;
    }

    public static int blue(int rgba) {
        return (rgba >>> 16) & 0xff;
    }

    public static int crispBlend(int L, int R) {
        return RgbaF.crispBlend(RgbaF.fromRgba(L), RgbaF.fromRgba(R)).toRgba();
    }

    public static int weightedAverageColor(int tL, int tR, int bR, int bL) {
        RgbaF t = RgbaF.crispBlend(RgbaF.fromRgba(tL), RgbaF.fromRgba(tR));
        RgbaF b = RgbaF.crispBlend(RgbaF.fromRgba(bR), RgbaF.fromRgba(bL));
        return RgbaF.crispBlend(t, b).toRgba();
    }

    public static int toByte(float value) {
        return (int) Math.floor(value * 255.0F);
    }

    public static int toByte(double value) {
        return (int) Math.floor(value * 255.0F);
    }

    public static int saturateToByte(float value) {
        return toByte(MathF.clamp(value, 0, 255));
    }

    public static int saturateToByte(double value) {
        return toByte(MathF.clamp(value, 0, 255));
    }

    public static int scale(int color, float rgb) {
        return scale(color, rgb, rgb, rgb);
    }

    public static int scale(int color, float r, float g, float b) {
        return fromRgba8(
            saturateToByte(red(color) * r),
            saturateToByte(green(color) * g),
            saturateToByte(blue(color) * b),
            color
        );
    }

}
