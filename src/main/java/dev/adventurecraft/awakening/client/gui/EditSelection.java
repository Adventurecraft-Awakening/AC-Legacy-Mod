package dev.adventurecraft.awakening.client.gui;

public record EditSelection(int start, int end) {

    public EditSelection(int start) {
        this(start, start);
    }

    public boolean isEmpty() {
        return (this.end - this.start) == 0;
    }

    public int sign() {
        return ((this.end - this.start) < 0) ? -1 : 1;
    }

    public int length() {
        return Math.abs(this.end - this.start);
    }

    public int absStart() {
        return Math.min(this.start, this.end);
    }

    public int absEnd() {
        return Math.max(this.start, this.end);
    }
}
