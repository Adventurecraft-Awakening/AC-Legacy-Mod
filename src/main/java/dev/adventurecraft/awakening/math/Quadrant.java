package dev.adventurecraft.awakening.math;

public enum Quadrant {

    R0(0),
    R90(1),
    R180(2),
    R270(3);

    public final int shift;

    Quadrant(int shift) {
        this.shift = shift;
    }

    public static Quadrant fromShift(int shift) {
        return switch (Math.floorMod(shift, 360)) {
            case 0 -> R0;
            case 90 -> R90;
            case 180 -> R180;
            case 270 -> R270;
            default -> throw new IllegalArgumentException("Invalid shift: " + shift);
        };
    }
}

