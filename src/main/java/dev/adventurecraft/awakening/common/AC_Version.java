package dev.adventurecraft.awakening.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class AC_Version {
    public static String version = "AdventureCraft - Minecraft Beta 1.7.3";
    public static String shortVersion = "AdventureCraft - Minecraft Beta 1.7.3";

    private static void getVersion() {
        try {
            File var0 = new File("version.txt");
            if (var0.exists()) {
                BufferedReader var1 = new BufferedReader(new FileReader(var0));
                String var2 = var1.readLine();
                if (var2 != null) {
                    version = String.format("AdventureCraft %s", var2);
                    shortVersion = String.format("AC %s - (MC 1.7.3)", var2);
                }
            }
        } catch (Exception var3) {
            var3.printStackTrace();
        }
    }

    static {
        getVersion();
    }
}
