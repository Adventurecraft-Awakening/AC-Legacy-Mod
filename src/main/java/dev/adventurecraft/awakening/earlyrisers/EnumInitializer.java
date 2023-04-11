package dev.adventurecraft.awakening.earlyrisers;

import com.chocohead.mm.api.ClassTinkerers;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;

public class EnumInitializer implements Runnable {

    @Override
    public void run() {
        MappingResolver remapper = FabricLoader.getInstance().getMappingResolver();

        String optionType = remapper.mapClassName("intermediary", "net.minecraft.class_271");
        this.addOptifineOptions(optionType);
        this.addAdventurecraftOptions(optionType);
    }

    private void addAdventurecraftOptions(String type) {
        addOptionEnum(type, "AUTO_FAR_CLIP", "options.adjustFarClip", false, true);
        addOptionEnum(type, "GRASS_3D", "options.grass3d", false, true);
    }

    private void addOptifineOptions(String type) {
        addOptionEnum(type, "FOG_FANCY", "Fog", false, false);
        addOptionEnum(type, "FOG_START", "Fog Start", false, false);
        addOptionEnum(type, "MIPMAP_LEVEL", "Mipmap Level", false, false);
        addOptionEnum(type, "MIPMAP_TYPE", "Mipmap Type", false, false);
        addOptionEnum(type, "LOAD_FAR", "Load Far", false, false);
        addOptionEnum(type, "PRELOADED_CHUNKS", "Preloaded Chunks", false, false);
        addOptionEnum(type, "SMOOTH_FPS", "Smooth FPS", false, false);
        addOptionEnum(type, "BRIGHTNESS", "Brightness", true, false);
        addOptionEnum(type, "CLOUDS", "Clouds", false, false);
        addOptionEnum(type, "CLOUD_HEIGHT", "Cloud Height", true, false);
        addOptionEnum(type, "TREES", "Trees", false, false);
        addOptionEnum(type, "GRASS", "Grass", false, false);
        addOptionEnum(type, "RAIN", "Rain & Snow", false, false);
        addOptionEnum(type, "WATER", "Water", false, false);
        addOptionEnum(type, "ANIMATED_WATER", "Water Animated", false, false);
        addOptionEnum(type, "ANIMATED_LAVA", "Lava Animated", false, false);
        addOptionEnum(type, "ANIMATED_FIRE", "Fire Animated", false, false);
        addOptionEnum(type, "ANIMATED_PORTAL", "Portal Animated", false, false);
        addOptionEnum(type, "AO_LEVEL", "Smooth Lighting", true, false);
        addOptionEnum(type, "FAST_DEBUG_INFO", "Fast Debug Info", false, false);
        addOptionEnum(type, "AUTOSAVE_TICKS", "Autosave", false, false);
        addOptionEnum(type, "BETTER_GRASS", "Better Grass", false, false);
        addOptionEnum(type, "ANIMATED_REDSTONE", "Redstone Animated", false, false);
        addOptionEnum(type, "ANIMATED_EXPLOSION", "Explosion Animated", false, false);
        addOptionEnum(type, "ANIMATED_FLAME", "Flame Animated", false, false);
        addOptionEnum(type, "ANIMATED_SMOKE", "Smoke Animated", false, false);
        addOptionEnum(type, "WEATHER", "Weather", false, false);
        addOptionEnum(type, "SKY", "Sky", false, false);
        addOptionEnum(type, "STARS", "Stars", false, false);
        addOptionEnum(type, "FAR_VIEW", "Far View", false, false);
        addOptionEnum(type, "CHUNK_UPDATES", "Chunk Updates", false, false);
        addOptionEnum(type, "CHUNK_UPDATES_DYNAMIC", "Dynamic Updates", false, false);
        addOptionEnum(type, "TIME", "Time", false, false);
        addOptionEnum(type, "CLEAR_WATER", "Clear Water", false, false);
        addOptionEnum(type, "SMOOTH_INPUT", "Smooth Input", false, false);
        addOptionEnum(type, "AA_LEVEL", "Antialiasing", false, false);
        addOptionEnum(type, "AF_LEVEL", "Anisotropic Filtering", false, false);
    }

    private static void addOptionEnum(String type, String name, String translationKey, boolean slider, boolean toggle) {
        ClassTinkerers.enumBuilder(type, String.class, boolean.class, boolean.class)
                .addEnum(name, translationKey, slider, toggle)
                .build();
    }
}
