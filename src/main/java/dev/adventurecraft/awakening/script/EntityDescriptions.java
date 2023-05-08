package dev.adventurecraft.awakening.script;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;

@SuppressWarnings("unused")
public class EntityDescriptions {

    static final HashMap<String, ScriptEntityDescription> descriptions = new HashMap();

    public static ScriptEntityDescription getDescription(String var0) {
        return descriptions.get(var0);
    }

    static void addDescription(String var0, ScriptEntityDescription var1) {
        descriptions.put(var0, var1);
    }

    public static void clearDescriptions() {
        descriptions.clear();
    }

    public static void loadDescriptions(File var0) {
        clearDescriptions();
        if (var0 != null && var0.exists() && var0.isDirectory()) {
            File[] var1 = var0.listFiles();
            for (File var4 : var1) {
                if (var4.isFile() && var4.getName().endsWith(".txt")) {
                    loadDescription(var4);
                }
            }
        }
    }

    private static void loadDescription(File var0) {
        Properties var1 = new Properties();

        try {
            var1.load(new FileInputStream(var0));
            ScriptEntityDescription var2 = new ScriptEntityDescription(var0.getName().split("\\.")[0]);

            try {
                var2.health = Integer.parseInt(var1.getProperty("health", "10"));
            } catch (NumberFormatException var7) {
            }

            try {
                var2.width = Float.parseFloat(var1.getProperty("width", "0.6"));
            } catch (NumberFormatException var6) {
            }

            try {
                var2.height = Float.parseFloat(var1.getProperty("height", "1.8"));
            } catch (NumberFormatException var5) {
            }

            try {
                var2.moveSpeed = Float.parseFloat(var1.getProperty("moveSpeed", "0.7"));
            } catch (NumberFormatException var4) {
            }

            var2.texture = var1.getProperty("texture", "/mob/char.png");
            var2.onCreated = var1.getProperty("onCreated", "");
            var2.onUpdate = var1.getProperty("onUpdate", "");
            var2.onDeath = var1.getProperty("onDeath", "");
            var2.onPathReached = var1.getProperty("onPathReached", "");
            var2.onAttacked = var1.getProperty("onAttacked", "");
            var2.onInteraction = var1.getProperty("onInteraction", "");
        } catch (FileNotFoundException var8) {
            var8.printStackTrace();
        } catch (IOException var9) {
            var9.printStackTrace();
        }
    }

    public static Set<String> getDescriptions() {
        return descriptions.keySet();
    }
}
