package dev.adventurecraft.awakening.layout;

import dev.adventurecraft.awakening.util.HashCode;

public final class Border implements BorderType {

    public static final Border zero = new Border(0);

    public final double left;
    public final double right;
    public final double top;
    public final double bot;

    public Border(double left, double right, double top, double bot) {
        this.left = left;
        this.right = right;
        this.top = top;
        this.bot = bot;
    }

    public Border(double horizontal, double vertical) {
        this(horizontal, horizontal, vertical, vertical);
    }

    public Border(double value) {
        this(value, value, value, value);
    }

    public double width() {
        return this.left + this.right;
    }

    public double height() {
        return this.top + this.bot;
    }

    public Border add(Border other) {
        return new Border(
            this.left + other.left,
            this.right + other.right,
            this.top + other.top,
            this.bot + other.bot
        );
    }

    public Border min(Border other) {
        return new Border(
            Math.min(this.left, other.left),
            Math.min(this.right, other.right),
            Math.min(this.top, other.top),
            Math.min(this.bot, other.bot)
        );
    }

    public Border max(Border other) {
        return new Border(
            Math.max(this.left, other.left),
            Math.max(this.right, other.right),
            Math.max(this.top, other.top),
            Math.max(this.bot, other.bot)
        );
    }

    public Border clamp(Border min, Border max) {
        return this.max(min).min(max);
    }

    @Override
    public Border asFloat() {
        return this;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Border border) {
            return this.left == border.left &&
                this.right == border.right &&
                this.top == border.top &&
                this.bot == border.bot;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return HashCode.combine(
            Double.hashCode(this.left),
            Double.hashCode(this.right),
            Double.hashCode(this.top),
            Double.hashCode(this.bot)
        );
    }
}
