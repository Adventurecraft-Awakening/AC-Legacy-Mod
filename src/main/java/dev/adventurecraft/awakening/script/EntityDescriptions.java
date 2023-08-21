package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.ACMod;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;

public class EntityDescriptions {

    static final HashMap<String, ScriptEntityDescription> descriptions = new HashMap<>();

    public static ScriptEntityDescription getDescription(String name) {
        return descriptions.get(name);
    }

    static void addDescription(String name, ScriptEntityDescription desc) {
        descriptions.put(name, desc);
    }

    public static void clearDescriptions() {
        descriptions.clear();
    }

    public static void loadDescriptions(File directory) {
        clearDescriptions();
        if (directory != null && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".txt")) {
                        loadDescription(file);
                    }
                }
            }
        }
    }

    private static void loadDescription(File file) {
        var bag = new Properties();
        try {
            bag.load(new FileInputStream(file));
            var desc = new ScriptEntityDescription(file.getName().split("\\.")[0]);

            try {
                desc.health = Integer.parseInt(bag.getProperty("health", "10"));
            } catch (NumberFormatException ex) {
                logException(file, "health", ex);
            }

            try {
                desc.width = Float.parseFloat(bag.getProperty("width", "0.6"));
            } catch (NumberFormatException ex) {
                logException(file, "width", ex);
            }

            try {
                desc.height = Float.parseFloat(bag.getProperty("height", "1.8"));
            } catch (NumberFormatException ex) {
                logException(file, "height", ex);
            }

            try {
                desc.moveSpeed = Float.parseFloat(bag.getProperty("moveSpeed", "0.7"));
            } catch (NumberFormatException ex) {
                logException(file, "moveSpeed", ex);
            }

            desc.texture = bag.getProperty("texture", "/mob/char.png");
            desc.onCreated = bag.getProperty("onCreated", "");
            desc.onUpdate = bag.getProperty("onUpdate", "");
            desc.onDeath = bag.getProperty("onDeath", "");
            desc.onPathReached = bag.getProperty("onPathReached", "");
            desc.onAttacked = bag.getProperty("onAttacked", "");
            desc.onInteraction = bag.getProperty("onInteraction", "");
        } catch (IOException ex) {
            logException(file, ex);
        }
    }

    private static void logException(File file, String property, NumberFormatException ex) {
        ACMod.LOGGER.error("Failed to parse property \"{}\" of entity description \"{}\".", property, file.getPath(), ex);
    }

    private static void logException(File file, Throwable ex) {
        ACMod.LOGGER.error("Failed to load entity description \"{}\".", file.getPath(), ex);
    }

    public static Set<String> getDescriptions() {
        return descriptions.keySet();
    }
}
