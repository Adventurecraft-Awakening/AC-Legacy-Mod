package dev.adventurecraft.awakening.util;

public final class PosUtil {

    public static int blockToSectionCoord(int blockCoord) {
        return blockCoord >> 4;
    }

    public static int sectionToBlockCoord(int sectionCoord) {
        return sectionCoord << 4;
    }
}
