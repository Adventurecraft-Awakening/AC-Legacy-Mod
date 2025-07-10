package dev.adventurecraft.awakening;

import dev.adventurecraft.awakening.util.FabricUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.include.com.google.gson.*;
import sun.misc.Unsafe;

import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.HashMap;

public class ACMod implements ModInitializer {

    public static final String MOD_ID = "adventurecraft";

    public static final Unsafe UNSAFE;

    public static boolean chunkIsNotPopulating = true;

    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("AC");
    public static final Logger CHAT_LOGGER = LoggerFactory.getLogger("Chat");
    public static final Logger JS_LOGGER = LoggerFactory.getLogger("JS");
    public static final Logger GL_LOGGER = LoggerFactory.getLogger("GL");

    public static @Nullable ModContainer MOD_CONTAINER;
    public static @Nullable GitMetadata GIT_META;

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.
        MOD_CONTAINER = FabricLoader.getInstance().getModContainer(MOD_ID).orElse(null);

        this.setupGitMetadata();
    }

    private void setupGitMetadata() {
        if (MOD_CONTAINER == null) {
            LOGGER.error("Missing mod container for ID {}.", MOD_ID);
            return;
        }

        var customValues = new HashMap<String, JsonElement>();
        for (var entry : MOD_CONTAINER.getMetadata().getCustomValues().entrySet()) {
            customValues.put(entry.getKey(), FabricUtil.toJson(entry.getValue()));
        }
        LOGGER.info("Mod container custom metadata: {}", customValues);

        try {
            String gitElement = String.valueOf(customValues.get("git"));
            GIT_META = new Gson().fromJson(new StringReader(gitElement), GitMetadata.class);
        }
        catch (Exception e) {
            LOGGER.error("Failed to parse Git metadata.", e);
        }
    }

    public static String getResourceName(String name) {
        return "/assets/adventurecraft/" + name;
    }

    static {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            UNSAFE = (Unsafe) field.get(null);
        }
        catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Couldn't obtain reference to sun.misc.Unsafe", e);
        }
    }
}

