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
    }

    private void addOptifineOptions(String type) {
        addOptionEnum(type, "FOG_FANCY", "options.of.detail.fogType", false, false);
        addOptionEnum(type, "FOG_START", "options.of.detail.fogStart", false, false);
        addOptionEnum(type, "MIPMAP_LEVEL", "options.of.texture.mipLevel", false, false);
        addOptionEnum(type, "MIPMAP_TYPE", "options.of.texture.mipType", false, false);
        addOptionEnum(type, "LOAD_FAR", "options.of.world.loadFar", false, false);
        addOptionEnum(type, "PRELOADED_CHUNKS", "options.of.world.preloadedChunks", false, false);
        addOptionEnum(type, "SMOOTH_FPS", "options.of.other.smoothFPS", false, false);
        addOptionEnum(type, "BRIGHTNESS", "options.of.brightness", true, false);
        addOptionEnum(type, "CLOUDS", "options.of.detail.clouds", false, false);
        addOptionEnum(type, "CLOUD_HEIGHT", "options.of.detail.cloudHeight", true, false);
        addOptionEnum(type, "LEAVES", "options.of.texture.leaves", false, false);
        addOptionEnum(type, "GRASS", "options.of.detail.grass", false, false);
        addOptionEnum(type, "RAIN", "options.of.detail.rainAndSnow", false, false);
        addOptionEnum(type, "WATER", "options.of.detail.water", false, false);
        addOptionEnum(type, "ANIMATED_WATER", "options.of.texture.animated.water", false, false);
        addOptionEnum(type, "ANIMATED_LAVA", "options.of.texture.animated.lava", false, false);
        addOptionEnum(type, "ANIMATED_FIRE", "options.of.texture.animated.fire", false, false);
        addOptionEnum(type, "ANIMATED_PORTAL", "options.of.texture.animated.portal", false, false);
        addOptionEnum(type, "AO_LEVEL", "options.ao", true, false);
        addOptionEnum(type, "FAST_DEBUG_INFO", "options.of.other.fastDebugInfo", false, false);
        addOptionEnum(type, "AUTOSAVE_TICKS", "options.of.world.autosaveInterval", false, false);
        addOptionEnum(type, "CONNECTED_GRASS", "options.of.detail.connectedGrass", false, false);
        addOptionEnum(type, "ANIMATED_REDSTONE", "options.of.texture.animated.redstone", false, false);
        addOptionEnum(type, "ANIMATED_EXPLOSION", "options.of.texture.animated.explosion", false, false);
        addOptionEnum(type, "ANIMATED_FLAME", "options.of.texture.animated.flame", false, false);
        addOptionEnum(type, "ANIMATED_SMOKE", "options.of.texture.animated.smoke", false, false);
        addOptionEnum(type, "WEATHER", "options.of.world.weather", false, false);
        addOptionEnum(type, "SKY", "options.of.detail.sky", false, false);
        addOptionEnum(type, "STARS", "options.of.detail.stars", false, false);
        addOptionEnum(type, "FAR_VIEW", "options.of.world.farView", false, false);
        addOptionEnum(type, "CHUNK_UPDATES", "options.of.world.chunkUpdates", false, false);
        addOptionEnum(type, "CHUNK_UPDATES_DYNAMIC", "options.of.world.dynamicUpdates", false, false);
        addOptionEnum(type, "TIME", "options.of.world.time", false, false);
        addOptionEnum(type, "CLEAR_WATER", "options.of.detail.clearWater", false, false);
        addOptionEnum(type, "SMOOTH_INPUT", "options.of.other.smoothInput", false, false);
        addOptionEnum(type, "AA_LEVEL", "options.of.msaa", false, false);
        addOptionEnum(type, "AF_LEVEL", "options.of.texture.anisoFilter", false, false);
    }

    private static void addOptionEnum(String type, String name, String translationKey, boolean slider, boolean toggle) {
        ClassTinkerers.enumBuilder(type, String.class, boolean.class, boolean.class)
            .addEnum(name, translationKey, slider, toggle)
            .build();
    }
}
