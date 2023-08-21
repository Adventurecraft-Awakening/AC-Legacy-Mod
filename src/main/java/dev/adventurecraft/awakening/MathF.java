package dev.adventurecraft.awakening;

public final class MathF {

    public static float cubicInterpolation(float mu, float y0, float y1, float y2, float y3) {
        float mu2 = mu * mu;
        float a0 = -0.5F * y0 + 1.5F * y1 - 1.5F * y2 + 0.5F * y3;
        float a1 = y0 - 2.5F * y1 + 2.0F * y2 - 0.5F * y3;
        float a2 = -0.5F * y0 + 0.5F * y2;
        return a0 * mu * mu2 + a1 * mu2 + a2 * mu + y1;
    }

    public static float lerp(float amount, float start, float end) {
        return (1.0F - amount) * start + amount * end;
    }

    public static double lerp(double amount, double start, double end) {
        return (1.0 - amount) * start + amount * end;
    }

    public static double round(double value, int decimals) {
        return Math.round(value * decimals) / (double) decimals;
    }
}
