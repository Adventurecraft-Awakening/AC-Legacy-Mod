package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.GitMetadata;

public final class AC_Version {

    public static String version = "Adventurecraft - Minecraft Beta 1.7.3";
    public static String shortVersion = "AC - MC b1.7.3";

    private static void getVersion() {
        GitMetadata gitMeta = ACMod.GIT_META;
        if (gitMeta != null) {
            version = String.format("Adventurecraft %s", gitMeta.version);
            shortVersion = String.format("AC %s - MC b1.7.3", gitMeta.version);
        }
    }

    static {
        getVersion();
    }
}
