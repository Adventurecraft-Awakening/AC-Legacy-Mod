package dev.adventurecraft.awakening.layout;

import dev.adventurecraft.awakening.util.HashCode;
import dev.adventurecraft.awakening.util.MathF;

public final class Point implements PointType {

    public static final Point zero = new Point(0);
    public static final Point one = new Point(1);

    public final double x;
    public final double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Point(double value) {
        this(value, value);
    }

    public Point add(Point other) {
        return this.add(other.x, other.y);
    }

    public Point add(double x, double y) {
        return new Point(this.x + x, this.y + y);
    }

    public Point sub(Point other) {
        return this.sub(other.x, other.y);
    }

    public Point sub(double x, double y) {
        return new Point(this.x - x, this.y - y);
    }

    public Point min(Point other) {
        return new Point(Math.min(this.x, other.x), Math.min(this.y, other.y));
    }

    public Point max(Point other) {
        return new Point(Math.max(this.x, other.x), Math.max(this.y, other.y));
    }

    public Point clamp(Point min, Point max) {
        return this.max(min).min(max);
    }

    public Point lerp(Point target, Point amount) {
        return new Point(MathF.lerp(amount.x, this.x, target.x), MathF.lerp(amount.y, this.y, target.y));
    }

    public Point lerp(Point target, double amount) {
        return this.lerp(target, new Point(amount));
    }

    public Point round() {
        return new Point(Math.round(this.x), Math.round(this.y));
    }

    public Point floor() {
        return new Point(Math.floor(this.x), Math.floor(this.y));
    }

    public IntPoint asInt() {
        return new IntPoint((int) this.x, (int) this.y);
    }

    @Override
    public Point asFloat() {
        return this;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Point rect) {
            return this.x == rect.x && this.y == rect.y;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return HashCode.combine(Double.hashCode(this.x), Double.hashCode(this.y));
    }

    @Override
    public String toString() {
        return "Point{" + x + ", " + y + '}';
    }
}
