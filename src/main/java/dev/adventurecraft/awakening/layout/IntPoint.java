package dev.adventurecraft.awakening.layout;

import dev.adventurecraft.awakening.util.HashCode;

public final class IntPoint implements PointType {

    public static final IntPoint zero = new IntPoint(0);
    public static final IntPoint one = new IntPoint(1);

    public final int x;
    public final int y;

    public IntPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public IntPoint(int value) {
        this(value, value);
    }

    public IntPoint add(IntPoint other) {
        return new IntPoint(this.x + other.x, this.y + other.y);
    }

    public IntPoint sub(IntPoint other) {
        return new IntPoint(this.x - other.x, this.y - other.y);
    }

    public IntPoint abs() {
        return new IntPoint(Math.abs(this.x), Math.abs(this.y));
    }

    public @Override Point asFloat() {
        return new Point(this.x, this.y);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof IntPoint rect) {
            return this.x == rect.x && this.y == rect.y;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return HashCode.combine(this.x, this.y);
    }

    @Override
    public String toString() {
        return "IntPoint{x=" + x + ", y=" + y + '}';
    }
}
