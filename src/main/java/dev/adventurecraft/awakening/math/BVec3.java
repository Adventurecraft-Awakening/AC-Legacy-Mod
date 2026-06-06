package dev.adventurecraft.awakening.math;

import org.jetbrains.annotations.NotNull;

public final class BVec3 {

    private final byte packed;

    private BVec3(byte packed) {
        this.packed = (byte) (packed & 0b111);
    }

    private static final BVec3[] VALUES = {
        new BVec3((byte) 0b000), new BVec3((byte) 0b001), new BVec3((byte) 0b010), new BVec3((byte) 0b011),
        new BVec3((byte) 0b100), new BVec3((byte) 0b101), new BVec3((byte) 0b110), new BVec3((byte) 0b111),
    };

    public static final BVec3 ZERO = VALUES[0b000];

    public static final BVec3 X0_Y0_Z0 = VALUES[0b000];
    public static final BVec3 X1_Y0_Z0 = VALUES[0b001];
    public static final BVec3 X0_Y1_Z0 = VALUES[0b010];
    public static final BVec3 X1_Y1_Z0 = VALUES[0b011];
    public static final BVec3 X0_Y0_Z1 = VALUES[0b100];
    public static final BVec3 X1_Y0_Z1 = VALUES[0b101];
    public static final BVec3 X0_Y1_Z1 = VALUES[0b110];
    public static final BVec3 X1_Y1_Z1 = VALUES[0b111];

    public static BVec3 from(byte packed) {
        return VALUES[packed];
    }

    public static BVec3 of(boolean x, boolean y, boolean z) {
        int ix = x ? 1 : 0;
        int iy = y ? 1 : 0;
        int iz = z ? 1 : 0;
        return VALUES[ix | (iy << 1) | (iz << 2)];
    }

    public boolean x() {
        return (this.packed & 0b001) != 0;
    }

    public boolean y() {
        return (this.packed & 0b010) != 0;
    }

    public boolean z() {
        return (this.packed & 0b100) != 0;
    }

    public int ix() {
        return this.packed & 0b001;
    }

    public int iy() {
        return (this.packed & 0b010) >>> 1;
    }

    public int iz() {
        return (this.packed & 0b100) >>> 2;
    }

    public boolean get(int i) {
        return (this.packed & (1 << i)) != 0;
    }

    public BVec3 with(int i, boolean bit) {
        int cleared = this.packed & ~(1 << i);
        int filled = (bit ? 1 : 0) << i;
        return VALUES[cleared | filled];
    }

    public @Override int hashCode() {
        return this.packed;
    }

    public @Override boolean equals(Object obj) {
        return obj instanceof BVec3 other && this.equals(other);
    }

    public boolean equals(BVec3 other) {
        return this.packed == other.packed;
    }

    public @Override @NotNull String toString() {
        return '{' + "x=" + this.ix() + ", y=" + this.iy() + ", z=" + this.iz() + '}';
    }
}
