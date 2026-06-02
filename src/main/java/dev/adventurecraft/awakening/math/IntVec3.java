package dev.adventurecraft.awakening.math;

import com.google.common.collect.AbstractIterator;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

public interface IntVec3 extends Comparable<IntVec3> {

    int x();

    int y();

    int z();

    default IntVec3 freeze() {
        return new Impl(this);
    }

    default Mut mut() {
        return new ImplMut(this);
    }

    default int[] toArray() {
        return new int[]{this.x(), this.y(), this.z()};
    }

    default boolean equals(IntVec3 other) {
        return this.x() == other.x() && this.y() == other.y() && this.z() == other.z();
    }

    default @Override int compareTo(IntVec3 other) {
        if (this.y() == other.y()) {
            if (this.z() == other.z()) {
                return this.x() - other.x();
            }
            return this.z() - other.z();
        }
        return this.y() - other.y();
    }

    default int compareToYXZ(IntVec3 other) {
        if (this.y() < other.y()) {
            return -1;
        }
        if (this.y() != other.y()) {
            return 1;
        }
        if (this.x() < other.x()) {
            return -1;
        }
        if (this.x() != other.x()) {
            return 1;
        }
        return Integer.compare(this.z(), other.z());
    }

    static AABB inclusiveAABB(IntVec3 a, IntVec3 b) {
        return AABB.create(
            Math.min(a.x(), b.x()),
            Math.min(a.y(), b.y()),
            Math.min(a.z(), b.z()),
            Math.max(a.x(), b.x()) + 1,
            Math.max(a.y(), b.y()) + 1,
            Math.max(a.z(), b.z()) + 1
        );
    }

    static <V extends IntVec3.Mut> Iterable<V> betweenClosed(int x0, int y0, int z0, int x1, int y1, int z1, V pos) {
        return () -> new AbstractIterator<>() {
            private final int w = x1 - x0 + 1;
            private final int h = y1 - y0 + 1;
            private final int d = z1 - z0 + 1;
            private int x;
            private int y;
            private int z;

            protected @Override V computeNext() {
                while (this.y < this.h) {
                    while (this.z < this.d) {
                        if (this.x < this.w) {
                            int x = this.x;
                            this.x = x + 1;
                            pos.set(x0 + x, y0 + this.y, z0 + this.z);
                            return pos;
                        }
                        this.x = 0;
                        this.z++;
                    }
                    this.z = 0;
                    this.y++;
                }
                return this.endOfData();
            }
        };
    }

    interface Mut extends IntVec3 {

        Mut x(int x);

        Mut y(int y);

        Mut z(int z);

        Mut set(int x, int y, int z);

        default Mut set(IntVec3 other) {
            return this.set(other.x(), other.y(), other.z());
        }
    }

    sealed class Impl implements IntVec3 {
        protected int x;
        protected int y;
        protected int z;

        public Impl() {
        }

        public Impl(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public Impl(IntVec3 other) {
            this(other.x(), other.y(), other.z());
        }

        public @Override int x() {
            return this.x;
        }

        public @Override int y() {
            return this.y;
        }

        public @Override int z() {
            return this.z;
        }

        public @Override IntVec3 freeze() {
            return this;
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
    }

    final class ImplMut extends Impl implements Mut {
        public ImplMut() {
        }

        public ImplMut(int x, int y, int z) {
            super(x, y, z);
        }

        public ImplMut(IntVec3 other) {
            super(other);
        }

        public @Override Mut x(int x) {
            this.x = x;
            return this;
        }

        public @Override Mut y(int y) {
            this.y = y;
            return this;
        }

        public @Override Mut z(int z) {
            this.z = z;
            return this;
        }

        public @Override Mut set(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
            return this;
        }
    }
}
