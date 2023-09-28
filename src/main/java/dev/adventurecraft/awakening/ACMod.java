package dev.adventurecraft.awakening;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class ACMod implements ModInitializer {

    public static final Unsafe UNSAFE;

    public static boolean chunkIsNotPopulating = true;

    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("AC");
    public static final Logger CHAT_LOGGER = LoggerFactory.getLogger("Chat");
    public static final Logger JS_LOGGER = LoggerFactory.getLogger("JS");
    public static final Logger GL_LOGGER = LoggerFactory.getLogger("GL");

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        LOGGER.info("Hello AC world!");
    }

    public static String getResourceName(String name) {
        return "/assets/adventurecraft/" + name;
    }

    static {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            UNSAFE = (Unsafe) field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Couldn't obtain reference to sun.misc.Unsafe", e);
        }
    }
}
