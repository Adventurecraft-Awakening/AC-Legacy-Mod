package dev.adventurecraft.awakening.common;

public record TextRect(int charCount, int width) {

    public static final TextRect EMPTY = new TextRect(0, 0);
}
