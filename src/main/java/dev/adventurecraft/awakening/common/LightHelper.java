package dev.adventurecraft.awakening.common;

public class LightHelper {

    public static float getLightBaseAtIndex(int index) {
        return 1.0F - (float) index / 15.0F;
    }

    public static float calculateLight(float value, float factor, float baseValue) {
        return (1.0F - value) / (value * factor + 1.0F) * (1.0F - baseValue) + baseValue;
    }

    public static float solveLightValue(float value, float factor, float baseValue) {
        float s0 = (1.0F - value) / (value * factor + 1.0F) * (1.0F - baseValue) + baseValue;
        float s1 = s0 - baseValue; // = (1.0F - value) / (value * factor + 1.0F) * (1.0F - baseValue)
        float s2 = s1 / (1.0F - baseValue); // = (1.0F - value) / (value * factor + 1.0F)
        float s3 = s2 * (value * factor + 1.0F); // = (1.0F - value)
        return 1.0F - s3;
    }

    public static float getDefaultLightAtIndex(int index) {
        return calculateLight(getLightBaseAtIndex(index), 3.0F, 0.05F);
    }
}
