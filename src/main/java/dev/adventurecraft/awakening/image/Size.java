package dev.adventurecraft.awakening.image;

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
}
