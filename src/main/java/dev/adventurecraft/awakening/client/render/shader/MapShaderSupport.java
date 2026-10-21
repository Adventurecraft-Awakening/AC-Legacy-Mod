package dev.adventurecraft.awakening.client.render.shader;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.client.render.shader.sources.MapShaderpackResolver;
import dev.adventurecraft.awakening.client.render.shader.sources.NullShaderpackSource;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.world.level.Level;

import java.io.File;
import java.io.IOException;

/** Reports map shader compatibility without creating a second rendering backend. */
public final class MapShaderSupport {
    private static String currentPackName;

    private MapShaderSupport() { }

    /** Inspect only this map's selected pack, and release archive handles immediately. */
    public static void loadMap(Level level) {
        unloadMap();
        if (level == null) return;
        File directory = ((ExWorld) level).getLevelDir();
        if (directory == null) return;
        try (var source = MapShaderpackResolver.open(directory)) {
            if (source instanceof NullShaderpackSource) return;
            currentPackName = source.getName();
            ACMod.LOGGER.warn(
                "Map shader pack {} uses legacy OpenGL GLSL, which the native Vulkan renderer does not support. " +
                    "The map will render with AdventureCraft's built-in Vulkan shaders.", currentPackName);
        } catch (IOException | RuntimeException error) {
            currentPackName = null;
            ACMod.LOGGER.warn("Cannot inspect shader pack for map {}. Using built-in Vulkan shaders.", directory, error);
        }
    }

    public static void unloadMap() { currentPackName = null; }

    /** Name of the map's unsupported pack, or null when no pack is selected. */
    public static String currentPackName() { return currentPackName; }

    public static boolean hasUnsupportedPack() { return currentPackName != null; }
}
