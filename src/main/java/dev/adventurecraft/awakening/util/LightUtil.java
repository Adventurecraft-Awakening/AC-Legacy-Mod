package dev.adventurecraft.awakening.util;

public final class LightUtil {

    public static float remapValue(float[] ramp, float value) {
        int low = (int) Math.floor(value);
        int high = (int) Math.ceil(value);
        float delta = value - low;
        return MathF.lerp(delta, ramp[low], ramp[high]);
    }
}
