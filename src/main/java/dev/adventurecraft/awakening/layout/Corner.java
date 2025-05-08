package dev.adventurecraft.awakening.layout;

import dev.adventurecraft.awakening.util.HashCode;

public final class Corner implements CornerType {

    public final double tl;
    public final double tr;
    public final double bl;
    public final double br;

    public Corner(double tl, double tr, double bl, double br) {
        this.tl = tl;
        this.tr = tr;
        this.bl = bl;
        this.br = br;
    }

    public Corner(double value) {
        this(value, value, value, value);
    }

    @Override
    public Corner asFloat() {
        return new Corner(this.tl, this.tr, this.bl, this.br);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Corner rect) {
            return this.tl == rect.tl &&
                this.tr == rect.tr &&
                this.bl == rect.bl &&
                this.br == rect.br;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return HashCode.combine(
            Double.hashCode(this.tl),
            Double.hashCode(this.tr),
            Double.hashCode(this.bl),
            Double.hashCode(this.br)
        );
    }
}
