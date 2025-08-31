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
        addOptionEnum(type, "AUTO_FAR_CLIP", "options.of.other.adjustFarClip", false, true);
        addOptionEnum(type, "GRASS_3D", "options.of.detail.grass3d", false, true);
        addOptionEnum(type, "CHAT_MESSAGE_BUFFER_LIMIT", "options.ac.chatMessageBufferLimit", true, false);
        addOptionEnum(type, "PARTICLE_LIMIT", "options.ac.detail.particleLimit", true, false);
        addOptionEnum(type, "ALLOW_JAVA_IN_SCRIPT", "options.ac.world.allowJavaInScript", false, true);
        addOptionEnum(type, "CHAT_WIDTH", "options.ac.chatWidth", true, false);
    }

    private void addOptifineOptions(String type) {
        addOptionEnum(type, "FOG_FANCY", "options.of.detail.fogType");
        addOptionEnum(type, "FOG_START", "options.of.detail.fogStart");
        addOptionEnum(type, "MIPMAP_LEVEL", "options.of.texture.mipLevel");
        addOptionEnum(type, "MIPMAP_TYPE", "options.of.texture.mipType");
        addOptionEnum(type, "LOAD_FAR", "options.of.world.loadFar");
        addOptionEnum(type, "PRELOADED_CHUNKS", "options.of.world.preloadedChunks");
        addOptionEnum(type, "SMOOTH_FPS", "options.of.other.smoothFPS");
        addOptionEnum(type, "BRIGHTNESS", "options.of.brightness", true, false);
        addOptionEnum(type, "CLOUDS", "options.of.detail.clouds");
        addOptionEnum(type, "CLOUD_HEIGHT", "options.of.detail.cloudHeight", true, false);
        addOptionEnum(type, "LEAVES", "options.of.texture.leaves");
        addOptionEnum(type, "GRASS", "options.of.detail.grass");
        addOptionEnum(type, "RAIN", "options.of.detail.rainAndSnow");
        addOptionEnum(type, "WATER", "options.of.detail.water");
        addOptionEnum(type, "ANIMATED_WATER", "options.of.texture.animated.water");
        addOptionEnum(type, "ANIMATED_LAVA", "options.of.texture.animated.lava");
        addOptionEnum(type, "ANIMATED_FIRE", "options.of.texture.animated.fire");
        addOptionEnum(type, "ANIMATED_PORTAL", "options.of.texture.animated.portal");
        addOptionEnum(type, "AO_LEVEL", "options.ao", true, false);
        addOptionEnum(type, "FAST_DEBUG_INFO", "options.of.other.fastDebugInfo");
        addOptionEnum(type, "AUTOSAVE_TICKS", "options.of.world.autosaveInterval");
        addOptionEnum(type, "CONNECTED_GRASS", "options.of.detail.connectedGrass");
        addOptionEnum(type, "ANIMATED_REDSTONE", "options.of.texture.animated.redstone");
        addOptionEnum(type, "ANIMATED_EXPLOSION", "options.of.texture.animated.explosion");
        addOptionEnum(type, "ANIMATED_FLAME", "options.of.texture.animated.flame");
        addOptionEnum(type, "ANIMATED_SMOKE", "options.of.texture.animated.smoke");
        addOptionEnum(type, "WEATHER", "options.of.world.weather");
        addOptionEnum(type, "SKY", "options.of.detail.sky");
        addOptionEnum(type, "STARS", "options.of.detail.stars");
        addOptionEnum(type, "FAR_VIEW", "options.of.world.farView");
        addOptionEnum(type, "CHUNK_UPDATES", "options.of.world.chunkUpdates");
        addOptionEnum(type, "CHUNK_UPDATES_DYNAMIC", "options.of.world.dynamicUpdates");
        addOptionEnum(type, "TIME", "options.of.world.time");
        addOptionEnum(type, "CLEAR_WATER", "options.of.detail.clearWater");
        addOptionEnum(type, "SMOOTH_INPUT", "options.of.other.smoothInput");
        addOptionEnum(type, "AA_LEVEL", "options.of.msaa");
        addOptionEnum(type, "AF_LEVEL", "options.of.texture.anisoFilter");
    }

    private static void addOptionEnum(String type, String name, String translationKey, boolean slider, boolean toggle) {
        ClassTinkerers.enumBuilder(type, String.class, boolean.class, boolean.class)
            .addEnum(name, translationKey, slider, toggle)
            .build();
    }

    private static void addOptionEnum(String type, String name, String translationKey) {
        addOptionEnum(type, name, translationKey, false, false);
    }
}
