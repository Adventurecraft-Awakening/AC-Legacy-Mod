package dev.adventurecraft.awakening.layout;

import dev.adventurecraft.awakening.common.Coord;

public final class IntBox {

    public static final IntBox zero = new IntBox(0, 0, 0, 0, 0, 0);

    public final int x0;
    public final int y0;
    public final int z0;

    public final int x1;
    public final int y1;
    public final int z1;

    public IntBox(int x0, int y0, int z0, int x1, int y1, int z1) {
        this.x0 = x0;
        this.y0 = y0;
        this.z0 = z0;

        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
    }

    public static IntBox fromCorners(Coord p0, Coord p1) {
        return new IntBox(p0.x, p0.y, p0.z, p1.x, p1.y, p1.z);
    }

    public static IntBox fromBox(Coord origin, Coord size) {
        return fromCorners(origin, origin.add(size));
    }

    public IntBox shiftRight(int amount) {
        return new IntBox(
            this.x0 >> amount,
            this.y0 >> amount,
            this.z0 >> amount,
            this.x1 >> amount,
            this.y1 >> amount,
            this.z1 >> amount
        );
    }

    public IntBox shiftLeft(int amount) {
        return new IntBox(
            this.x0 << amount,
            this.y0 << amount,
            this.z0 << amount,
            this.x1 << amount,
            this.y1 << amount,
            this.z1 << amount
        );
    }

    public boolean containsX(int x) {
        return x >= this.x0 && x < this.x1;
    }

    public boolean containsY(int y) {
        return y >= this.y0 && y < this.y1;
    }

    public boolean containsZ(int z) {
        return z >= this.z0 && z < this.z1;
    }

    public boolean contains(int x, int y, int z) {
        return this.containsX(x) && this.containsY(y) && this.containsZ(z);
    }
}
