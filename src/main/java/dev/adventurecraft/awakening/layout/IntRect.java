package dev.adventurecraft.awakening.layout;

import dev.adventurecraft.awakening.util.HashCode;

public final class IntRect implements RectType {

    public static final IntRect zero = new IntRect(0, 0, 0, 0);

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

    public static IntRect fromEdges(int left, int top, int right, int bot) {
        int x0 = Math.min(left, right);
        int y0 = Math.min(top, bot);
        int x1 = Math.max(left, right);
        int y1 = Math.max(top, bot);
        return new IntRect(x0, y0, x1 - x0, y1 - y0);
    }

    public static IntRect fromEdges(IntRect horizontal, IntRect vertical) {
        return fromEdges(horizontal.left(), vertical.top(), horizontal.right(), vertical.bot());
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

    public int bot() {
        return this.y + this.h;
    }

    public int width() {
        return this.w;
    }

    public int height() {
        return this.h;
    }

    public Size size() {
        return new Size(this.w, this.h);
    }

    public IntPoint location() {
        return new IntPoint(this.x, this.y);
    }

    public IntPoint topLeft() {
        return new IntPoint(this.x, this.y);
    }

    public IntPoint topRight() {
        return new IntPoint(this.x + this.w, this.y);
    }

    public IntPoint botLeft() {
        return new IntPoint(this.x, this.y + this.h);
    }

    public IntPoint botRight() {
        return new IntPoint(this.x + this.w, this.y + this.h);
    }

    public IntRect alongLeft(int width) {
        return fromEdges(this.left() + width, this.top(), this.left(), this.bot());
    }

    public IntRect alongRight(int width) {
        return fromEdges(this.right(), this.top(), this.right() - width, this.bot());
    }

    public IntRect alongTop(int height) {
        return fromEdges(this.left(), this.top() + height, this.right(), this.top());
    }

    public IntRect alongBot(int height) {
        return fromEdges(this.left(), this.bot(), this.right(), this.bot() - height);
    }

    public IntRect shrink(IntBorder border) {
        return new IntRect(
            this.x + border.left,
            this.y + border.top,
            this.w - border.right - border.left,
            this.h - border.bot - border.top
        );
    }

    public IntRect expand(IntBorder border) {
        return new IntRect(
            this.x - border.left,
            this.y - border.top,
            this.w + border.right + border.left,
            this.h + border.bot + border.top
        );
    }

    public IntRect union(IntRect other) {
        int x0 = Math.min(this.x, other.x);
        int y0 = Math.min(this.y, other.y);
        int x1 = Math.max(this.right(), other.right());
        int y1 = Math.max(this.bot(), other.bot());
        return new IntRect(x0, y0, x1 - x0, y1 - y0);
    }

    public IntRect intersect(IntRect other) {
        int x0 = Math.max(this.x, other.x);
        int y0 = Math.max(this.y, other.y);
        int x1 = Math.min(this.right(), other.right());
        int y1 = Math.min(this.bot(), other.bot());
        return new IntRect(x0, y0, x1 - x0, y1 - y0);
    }

    public boolean contains(IntPoint point) {
        return this.containsX(point.x) && this.containsY(point.y);
    }

    public boolean containsX(int x) {
        return x >= this.left() && x <= this.right();
    }

    public boolean containsY(int y) {
        return y >= this.top() && y <= this.bot();
    }

    @Override
    public boolean isEmpty() {
        return this.w <= 0 || this.h <= 0;
    }

    @Override
    public Rect asFloat() {
        return new Rect(this.x, this.y, this.w, this.h);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof IntRect rect) {
            return this.x == rect.x && this.y == rect.y && this.w == rect.w && this.h == rect.h;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return HashCode.combine(this.x, this.y, this.w, this.h);
    }

    @Override
    public String toString() {
        return "IntRect{x=" + x + ", y=" + y + ", w=" + w + ", h=" + h + '}';
    }
}
