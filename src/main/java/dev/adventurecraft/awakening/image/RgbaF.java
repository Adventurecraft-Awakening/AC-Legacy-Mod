package dev.adventurecraft.awakening.image;

public record RgbaF(float r, float g, float b, float a) {

    public static RgbaF fromRgba(int rgba) {
        return new RgbaF(rgba & 0xff, (rgba >>> 8) & 0xff, (rgba >>> 16) & 0xff, rgba >>> 24);
    }

    public int toRgba() {
        int R = (int) r;
        int G = (int) g;
        int B = (int) b;
        int A = (int) a;
        return R | (G << 8) | (B << 16) | (A << 24);
    }

    public static RgbaF crispBlend(RgbaF L, RgbaF R) {
        float aL = L.a;
        float aR = R.a;
        float a = (aL + aR) * 0.5f;

        if (aL == 0) {
            L = R;
            a *= 0.5f;
        }
        else if (aR == 0) {
            R = L;
            a *= 0.5f;
        }

        float div = 1.0f / (aL + aR);
        float b = (L.b * aL + R.b * aR) * div;
        float g = (L.g * aL + R.g * aR) * div;
        float r = (L.r * aL + R.r * aR) * div;

        return new RgbaF(r, g, b, a);
    }
}