package dev.adventurecraft.awakening.layout;

import dev.adventurecraft.awakening.util.HashCode;

public final class Rect implements RectType {

    public static final Rect zero = new Rect(0, 0, 0, 0);

    public final double x;
    public final double y;
    public final double w;
    public final double h;

    public Rect(double x, double y, double w, double h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public Rect(double value) {
        this(value, value, value, value);
    }

    public static Rect fromEdges(double left, double top, double right, double bot) {
        double x0 = Math.min(left, right);
        double y0 = Math.min(top, bot);
        double x1 = Math.max(left, right);
        double y1 = Math.max(top, bot);
        return new Rect(x0, y0, x1 - x0, y1 - y0);
    }

    public static Rect fromEdges(Rect horizontal, Rect vertical) {
        return fromEdges(horizontal.left(), vertical.top(), horizontal.right(), vertical.bot());
    }

    public double left() {
        return this.x;
    }

    public double right() {
        return this.x + this.w;
    }

    public double top() {
        return this.y;
    }

    public double bot() {
        return this.y + this.h;
    }

    public double width() {
        return this.w;
    }

    public double height() {
        return this.h;
    }

    public Point location() {
        return new Point(this.x, this.y);
    }

    public Rect alongLeft(double width) {
        return fromEdges(this.left() + width, this.top(), this.left(), this.bot());
    }

    public Rect alongRight(double width) {
        return fromEdges(this.right(), this.top(), this.right() - width, this.bot());
    }

    public Rect alongTop(double height) {
        return fromEdges(this.left(), this.top() + height, this.right(), this.top());
    }

    public Rect alongBot(double height) {
        return fromEdges(this.left(), this.bot(), this.right(), this.bot() - height);
    }

    public Rect shrink(Border border) {
        return new Rect(
            this.x + border.left,
            this.y + border.top,
            this.w - border.right - border.left,
            this.h - border.bot - border.top
        );
    }

    public Rect expand(Border border) {
        return new Rect(
            this.x - border.left,
            this.y - border.top,
            this.w + border.right + border.left,
            this.h + border.bot + border.top
        );
    }

    public Rect offset(Point point) {
        return this.offset(point.x, point.y);
    }

    public Rect offset(double x, double y) {
        return new Rect(this.x + x, this.y + y, this.w, this.h);
    }

    public Rect abs() {
        return new Rect(Math.abs(this.x), Math.abs(this.y), Math.abs(this.w), Math.abs(this.h));
    }

    public Rect round() {
        return new Rect(Math.round(this.x), Math.round(this.y), Math.round(this.w), Math.round(this.h));
    }

    public Rect multiply(Rect other) {
        return new Rect(this.x * other.x, this.y * other.y, this.w * other.w, this.h * other.h);
    }

    public Rect divide(Rect other) {
        return new Rect(this.x / other.x, this.y / other.y, this.w / other.w, this.h / other.h);
    }

    public Rect divide(double other) {
        return new Rect(this.x / other, this.y / other, this.w / other, this.h / other);
    }

    public IntRect asInt() {
        return new IntRect((int) this.x, (int) this.y, (int) this.w, (int) this.h);
    }

    @Override
    public boolean isEmpty() {
        return this.w <= 0 || this.h <= 0;
    }

    @Override
    public Rect asFloat() {
        return this;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Rect rect) {
            return this.x == rect.x && this.y == rect.y && this.w == rect.w && this.h == rect.h;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return HashCode.combine(
            Double.hashCode(this.x),
            Double.hashCode(this.y),
            Double.hashCode(this.w),
            Double.hashCode(this.h)
        );
    }

    @Override
    public String toString() {
        return "Rect{" + x + ", " + y + ", " + w + ", " + h + '}';
    }
}
