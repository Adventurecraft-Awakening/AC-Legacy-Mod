package dev.adventurecraft.awakening.common;

public class AoHelper {

    public static float lightLevel0 = 0;
    public static float lightLevel1 = 0;

    public static void setLightLevels(float min, float max) {
        lightLevel0 = min;
        lightLevel1 = max;
    }

    public static float fixAoLight(float min, float max, float a, float b, float factor) {
        if (a > min) {
            return a;
        } else if (b <= max) {
            return a;
        } else {
            return a + (b - a) * factor;
        }
    }
}
