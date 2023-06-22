package dev.adventurecraft.awakening.common;

public class LightHelper {

    public static float getLightBaseAtIndex(int index) {
        return 1.0F - (float) index / 15.0F;
    }

    public static float calculateLight(float value, float factor, float baseValue) {
        return (1.0F - value) / (value * factor + 1.0F) * (1.0F - baseValue) + baseValue;
    }

    public static float solveLightValue(float value, float factor, float baseValue) {
        // s1: value = (1.0F - original) / (original * factor + 1.0F) * (1.0F - baseValue) + baseValue;
        // s2: (value - baseValue) = (1.0F - original) / (original * factor + 1.0F) * (1.0F - baseValue);
        // s3: (value - baseValue) / (1.0F - baseValue) = (1.0F - original) / (original * factor + 1.0F);
        float s3Left = (value - baseValue) / (1.0F - baseValue);
        float s3Right = (1.0F - s3Left) / (s3Left * factor + 1.0F);
        return s3Right;
    }

    public static float getDefaultLightAtIndex(int index) {
        return calculateLight(getLightBaseAtIndex(index), 3.0F, 0.05F);
    }
}
