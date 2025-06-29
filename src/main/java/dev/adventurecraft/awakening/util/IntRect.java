package dev.adventurecraft.awakening.util;

public final class IntRect {

    public final int x;
    public final int y;
    public final int w;
    public final int h;

    public IntRect(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public static IntRect fromEdges(int left, int top, int right, int bottom) {
        return new IntRect(left, top, right - left, bottom - top);
    }

    public int left() {
        return this.x;
    }

    public int right() {
        return this.x + this.w;
    }

    public int top() {
        return this.y;
    }

    public int bottom() {
        return this.y + this.h;
    }

    public int width() {
        return this.w;
    }

    public int height() {
        return this.h;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof IntRect rect) {
            return this.x == rect.x &&
                this.y == rect.y &&
                this.w == rect.w &&
                this.h == rect.h;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return HashCode.combine(this.x, this.y, this.w, this.h);
    }
}
