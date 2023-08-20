package dev.adventurecraft.awakening.extension.util;

public interface ExProgressListener {

    void notifyProgress(String stage, double percentage, boolean forceDraw);
}
