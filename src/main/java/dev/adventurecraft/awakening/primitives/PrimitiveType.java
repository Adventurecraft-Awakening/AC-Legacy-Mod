package dev.adventurecraft.awakening.primitives;

import java.util.Objects;

public abstract class PrimitiveType {

    private final int bits;
    private final boolean signed;

    public PrimitiveType(int bits, boolean signed) {
        this.bits = bits;
        this.signed = signed;
    }

    public final int bits() {
        return this.bits;
    }

    public final boolean signed() {
        return this.signed;
    }

    public String toShortPrefix() {
        return this.signed() ? "S" : "U";
    }

    public String toShortName() {
        return this.toShortPrefix() + this.bits();
    }

    public String toLongPrefix() {
        return this.signed() ? "Signed" : "Unsigned";
    }

    public String toLongName() {
        return this.toLongPrefix() + this.bits();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PrimitiveType that)) {
            return false;
        }
        return bits() == that.bits() && signed() == that.signed();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.bits(), this.signed());
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + '{' + this.toShortName() + '}';
    }
}

