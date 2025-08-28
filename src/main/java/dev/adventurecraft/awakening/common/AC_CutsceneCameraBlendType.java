package dev.adventurecraft.awakening.common;

public enum AC_CutsceneCameraBlendType {

    NONE(0),
    LINEAR(1),
    QUADRATIC(2);

    public static final int MAX = 3;

    public final int value;

    AC_CutsceneCameraBlendType(int value) {
        this.value = value;
    }

    public static AC_CutsceneCameraBlendType cycle(AC_CutsceneCameraBlendType type, int direction) {
        return get(Integer.remainderUnsigned(type.value + direction, MAX));
    }

    public static AC_CutsceneCameraBlendType get(int value) {
        return switch (value) {
            case 0 -> NONE;
            case 1 -> LINEAR;
            case 2 -> QUADRATIC;
            default -> throw new IllegalStateException("Unexpected value: " + value);
        };
    }
}
