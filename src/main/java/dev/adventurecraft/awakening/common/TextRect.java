package dev.adventurecraft.awakening.common;

public record TextRect(int charCount, int width) {

    public static final TextRect empty = new TextRect(0, 0);
}
