package dev.adventurecraft.awakening.layout;

import dev.adventurecraft.awakening.util.HashCode;

public final class Size implements ShapeType {

    public final int w;
    public final int h;

    public Size(int w, int h) {
        this.w = w;
        this.h = h;
    }

    public int width() {
        return this.w;
    }

    public int height() {
        return this.h;
    }

    public Size flip() {
        return new Size(this.h, this.w);
    }

    @Override
    public boolean isEmpty() {
        return this.w <= 0 || this.h <= 0;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Size size) {
            return this.w == size.w && this.h == size.h;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return HashCode.combine(this.w, this.h);
    }

    @Override
    public String toString() {
        return "Size{" + this.w + ", " + this.h + '}';
    }
}
