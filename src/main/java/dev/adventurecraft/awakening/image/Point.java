package dev.adventurecraft.awakening.image;

public final class Point {

    public static final Point zero = new Point(0);
    public static final Point one = new Point(1);
    
    public final int x;
    public final int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Point(int v) {
        this(v, v);
    }
}
