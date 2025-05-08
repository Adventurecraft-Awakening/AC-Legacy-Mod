package dev.adventurecraft.awakening.layout;

import dev.adventurecraft.awakening.util.HashCode;

public final class IntBorder implements BorderType {

    public static final IntBorder zero = new IntBorder(0);

    public final int left;
    public final int right;
    public final int top;
    public final int bot;

    public IntBorder(int left, int right, int top, int bot) {
        this.left = left;
        this.right = right;
        this.top = top;
        this.bot = bot;
    }

    public IntBorder(int horizontal, int vertical) {
        this(horizontal, horizontal, vertical, vertical);
    }

    public IntBorder(int value) {
        this(value, value, value, value);
    }

    public int width() {
        return this.left + this.right;
    }

    public int height() {
        return this.top + this.bot;
    }

    public IntPoint topLeft() {
        return new IntPoint(this.left, this.top);
    }

    public IntBorder add(IntBorder other) {
        return new IntBorder(
            this.left + other.left,
            this.right + other.right,
            this.top + other.top,
            this.bot + other.bot
        );
    }

    @Override
    public Border asFloat() {
        return new Border(this.left, this.right, this.top, this.bot);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof IntBorder border) {
            return this.left == border.left && this.right == border.right && this.top == border.top && this.bot == border.bot;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return HashCode.combine(this.left, this.right, this.top, this.bot);
    }
}
