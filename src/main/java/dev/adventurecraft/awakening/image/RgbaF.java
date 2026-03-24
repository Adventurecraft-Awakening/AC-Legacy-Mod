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
        float a = aL + aR;
        aL /= a;
        aR /= a;

        a *= 0.5f;
        if (aL == 0) {
            L = R;
            a *= 0.5f;
        }
        else if (aR == 0) {
            R = L;
            a *= 0.5f;
        }

        float b = L.b * aL + R.b * aR;
        float g = L.g * aL + R.g * aR;
        float r = L.r * aL + R.r * aR;

        return new RgbaF(r, g, b, a);
    }

    public static float fromByte(int value) {
        return (value & 0xff) / 255.0F;
    }
}