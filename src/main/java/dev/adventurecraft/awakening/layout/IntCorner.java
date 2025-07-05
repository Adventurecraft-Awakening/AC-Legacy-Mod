package dev.adventurecraft.awakening.layout;

import dev.adventurecraft.awakening.util.HashCode;

public final class IntCorner implements CornerType {

    private final int tl;
    private final int tr;
    private final int bl;
    private final int br;

    public IntCorner(int topLeft, int topRight, int botLeft, int botRight) {
        this.tl = topLeft;
        this.tr = topRight;
        this.bl = botLeft;
        this.br = botRight;
    }

    public IntCorner(int value) {
        this(value, value, value, value);
    }

    public static IntCorner vertical(int top, int bot) {
        return new IntCorner(top, top, bot, bot);
    }

    public static IntCorner horizontal(int left, int right) {
        return new IntCorner(left, right, left, right);
    }

    public int topLeft() {
        return this.tl;
    }

    public int topRight() {
        return this.tr;
    }

    public int botLeft() {
        return this.bl;
    }

    public int botRight() {
        return this.br;
    }

    @Override
    public Corner asFloat() {
        return new Corner(this.tl, this.tr, this.bl, this.br);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof IntCorner rect) {
            return this.tl == rect.tl &&
                this.tr == rect.tr &&
                this.bl == rect.bl &&
                this.br == rect.br;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return HashCode.combine(this.tl, this.tr, this.bl, this.br);
    }
}
