package dev.adventurecraft.awakening.world;

import dev.adventurecraft.awakening.common.Coord;
import dev.adventurecraft.awakening.math.Direction;
import dev.adventurecraft.awakening.math.Direction.Axis;
import dev.adventurecraft.awakening.math.IntVec3;
import dev.adventurecraft.awakening.math.Rotation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class BlockPos implements IntVec3 {

    public static final BlockPos ZERO = new BlockPos();

    public static final BlockPos ONE = new BlockPos(1);

    protected int x;
    protected int y;
    protected int z;

    public BlockPos() {
    }

    public BlockPos(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public BlockPos(int value) {
        this(value, value, value);
    }

    public BlockPos(IntVec3 other) {
        this(other.x(), other.y(), other.z());
    }

    public final @Override int x() {
        return this.x;
    }

    public final @Override int y() {
        return this.y;
    }

    public final @Override int z() {
        return this.z;
    }

    public BlockPos add(int x, int y, int z) {
        if (x == 0 && y == 0 && z == 0) {
            return this.freeze();
        }
        return new BlockPos(this.x + x, this.y + y, this.z + z);
    }

    public BlockPos add(IntVec3 other) {
        return this.add(other.x(), other.y(), other.z());
    }

    public BlockPos sub(int x, int y, int z) {
        if (x == 0 && y == 0 && z == 0) {
            return this.freeze();
        }
        return new BlockPos(this.x - x, this.y - y, this.z - z);
    }

    public BlockPos sub(IntVec3 other) {
        return this.sub(other.x(), other.y(), other.z());
    }

    public BlockPos mul(int scale) {
        if (scale == 1) {
            return this.freeze();
        }
        if (scale == 0) {
            return ZERO;
        }
        return new BlockPos(this.x * scale, this.y * scale, this.z * scale);
    }

    public BlockPos negate() {
        return new BlockPos(-this.x, -this.y, -this.z);
    }

    public BlockPos min(IntVec3 other) {
        return new BlockPos(Math.min(this.x, other.x()), Math.min(this.y, other.y()), Math.min(this.z, other.z()));
    }

    public BlockPos max(IntVec3 other) {
        return new BlockPos(Math.max(this.x, other.x()), Math.max(this.y, other.y()), Math.max(this.z, other.z()));
    }

    public boolean lessAny(int x, int y, int z) {
        return this.x < x || this.y < y || this.z < z;
    }

    public boolean equals(int x, int y, int z) {
        return this.x == x || this.y == y || this.z == z;
    }

    public boolean equalsAll(int value) {
        return this.equals(value, value, value);
    }

    public BlockPos relative(Direction direction) {
        Coord n = direction.normal();
        return new BlockPos(this.x + n.x, this.y + n.y, this.z + n.z);
    }

    public BlockPos relative(Direction direction, int steps) {
        if (steps == 0) {
            return this.freeze();
        }
        Coord n = direction.normal();
        return new BlockPos(this.x + n.x * steps, this.y + n.y * steps, this.z + n.z * steps);
    }

    public BlockPos relative(Axis axis, int steps) {
        if (steps == 0) {
            return this.freeze();
        }
        int xStep = axis == Axis.X ? steps : 0;
        int yStep = axis == Axis.Y ? steps : 0;
        int zStep = axis == Axis.Z ? steps : 0;
        return new BlockPos(this.x + xStep, this.y + yStep, this.z + zStep);
    }

    public BlockPos rotate(Rotation rotation) {
        return switch (rotation) {
            case RIGHT_90 -> new BlockPos(-this.z, this.y, this.x);
            case RIGHT_180 -> new BlockPos(-this.x, this.y, -this.z);
            case LEFT_90 -> new BlockPos(this.z, this.y, -this.x);
            case NONE -> this.freeze();
        };
    }

    public @Override BlockPos freeze() {
        return this;
    }

    public @Override Mut mut() {
        return new Mut(this);
    }

    public @Override Mut mutCopy() {
        return new Mut(this);
    }

    public @Override boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof IntVec3 other && this.equals(other);
    }

    public @Override int hashCode() {
        return (this.y + this.z * 31) * 31 + this.x;
    }

    public @Override @NotNull String toString() {
        return '{' + "x=" + this.x + ", y=" + this.y + ", z=" + this.z + '}';
    }

    public static BlockPos.Mut mutZero() {
        return new BlockPos.Mut();
    }

    public static BlockPos floor(Vec3 vec) {
        return new BlockPos((int) Math.floor(vec.x), (int) Math.floor(vec.y), (int) Math.floor(vec.z));
    }

    public static BlockPos sub(IntVec3 a, IntVec3 b) {
        return new BlockPos(a.x() - b.x(), a.y() - b.y(), a.z() - b.z());
    }

    public static boolean contains(int x, int y, int z, IntVec3 min, IntVec3 max) {
        return IntVec3.contains(x, y, z, min, max);
    }

    public static AABB inclusiveAABB(IntVec3 a, IntVec3 b) {
        return IntVec3.inclusiveAABB(a, b);
    }

    public static Iterable<BlockPos.Mut> betweenClosed(int x0, int y0, int z0, int x1, int y1, int z1) {
        return IntVec3.betweenClosed(x0, y0, z0, x1, y1, z1, new BlockPos.Mut());
    }

    public static Iterable<BlockPos.Mut> betweenClosed(BlockPos a, BlockPos b) {
        return betweenClosed(
            Math.min(a.x, b.x),
            Math.min(a.y, b.y),
            Math.min(a.z, b.z),
            Math.max(a.x, b.x),
            Math.max(a.y, b.y),
            Math.max(a.z, b.z)
        );
    }

    public static final class Mut extends BlockPos implements IntVec3.Mut {

        public Mut() {
        }

        public Mut(int x, int y, int z) {
            super(x, y, z);
        }

        public Mut(int value) {
            super(value);
        }

        public Mut(IntVec3 other) {
            super(other);
        }

        public @Override BlockPos.Mut x(int x) {
            this.x = x;
            return this;
        }

        public @Override BlockPos.Mut y(int y) {
            this.y = y;
            return this;
        }

        public @Override BlockPos.Mut z(int z) {
            this.z = z;
            return this;
        }

        public @Override BlockPos.Mut set(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
            return this;
        }

        public @Override BlockPos.Mut set(IntVec3 other) {
            this.x = other.x();
            this.y = other.y();
            this.z = other.z();
            return this;
        }

        public @Override BlockPos.Mut splat(int value) {
            this.x = value;
            this.y = value;
            this.z = value;
            return this;
        }

        public @Override BlockPos freeze() {
            return new BlockPos(this);
        }

        public @Override BlockPos.Mut mut() {
            return this;
        }
    }
}
