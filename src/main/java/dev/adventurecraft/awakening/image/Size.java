package dev.adventurecraft.awakening.image;

import java.util.Objects;

public final class Size {
    
    public final int w;
    public final int h;

    public Size(int w, int h) {
        this.w = w;
        this.h = h;
    }

    public Size flip() {
        return new Size(this.h, this.w);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Size size))
            return false;
        return w == size.w && h == size.h;
    }

    @Override
    public int hashCode() {
        return Objects.hash(w, h);
    }

    @Override
    public String toString() {
        return "Size{" +w + ", " + h + '}';
    }
}
