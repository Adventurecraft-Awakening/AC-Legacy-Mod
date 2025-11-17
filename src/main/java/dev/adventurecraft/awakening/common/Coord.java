package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.util.HashCode;
import org.jetbrains.annotations.Nullable;

public final class Coord {

    public static final Coord zero = new Coord(0, 0, 0);
    public static final Coord one = new Coord(1, 1, 1);

    public final int x;
    public final int y;
    public final int z;

    public Coord(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Coord(int value) {
        this(value, value, value);
    }

    public Coord add(Coord other) {
        return new Coord(this.x + other.x, this.y + other.y, this.z + other.z);
    }

    public Coord sub(Coord other) {
        return new Coord(this.x - other.x, this.y - other.y, this.z - other.z);
    }

    public Coord min(Coord other) {
        return new Coord(Math.min(this.x, other.x), Math.min(this.y, other.y), Math.min(this.z, other.z));
    }

    public Coord max(Coord other) {
        return new Coord(Math.max(this.x, other.x), Math.max(this.y, other.y), Math.max(this.z, other.z));
    }

    public Coord negate() {
        return new Coord(-this.x, -this.y, -this.z);
    }

    public boolean equalsAny(int x, int y, int z) {
        return this.x == x || this.y == y || this.z == z;
    }

    public boolean equalsAny(int value) {
        return this.equalsAny(value, value, value);
    }

    public boolean equalsAny(Coord other) {
        return this.equalsAny(other.x, other.y, other.z);
    }

    public boolean equals(int x, int y, int z) {
        return this.x == x && this.y == y && this.z == z;
    }

    public boolean equals(int value) {
        return this.equals(value, value, value);
    }

    public @Override boolean equals(@Nullable Object obj) {
        if (obj instanceof Coord c) {
            return this.equals(c.x, c.y, c.z);
        }
        return false;
    }

    public @Override int hashCode() {
        return HashCode.combine(this.x, this.y, this.z);
    }

    public @Override String toString() {
        return "{" + "x=" + x + ", y=" + y + ", z=" + z + '}';
    }
}
