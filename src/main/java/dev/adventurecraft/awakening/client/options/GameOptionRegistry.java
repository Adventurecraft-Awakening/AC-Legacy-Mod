package dev.adventurecraft.awakening.client.options;

import dev.adventurecraft.awakening.util.MathF;
import dev.adventurecraft.awakening.client.render.NativeRenderingOptions;
import net.minecraft.client.Option;
import net.minecraft.locale.I18n;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static dev.adventurecraft.awakening.client.options.OptionDefinition.ChangeSource.INTERACTION;

/** One definition per persisted AdventureCraft/OptiFine option; legacy enums remain UI identities. */
public final class GameOptionRegistry {

    public static final int MIN_CHUNK_RENDER_DISTANCE = 2;
    public static final int MAX_CHUNK_RENDER_DISTANCE = 124;
    public static final int LEGACY_FAR_CHUNK_DIAMETER = 32;

    private static final List<OptionDefinition<?>> DEFINITIONS = new ArrayList<>();
    private static final OptionDefinition.Codec<Boolean> BOOLEAN = OptionDefinition.codec(Boolean::parseBoolean);
    private static final OptionDefinition.Codec<Integer> INTEGER = OptionDefinition.codec(Integer::parseInt);
    private static final OptionDefinition.Codec<Float> FLOAT = OptionDefinition.codec(Float::parseFloat);

    public static final OptionDefinition<Integer> CHUNK_RENDER_DISTANCE = define(
        integerSlider("acChunkRenderDistance", LEGACY_FAR_CHUNK_DIAMETER,
            MIN_CHUNK_RENDER_DISTANCE, MAX_CHUNK_RENDER_DISTANCE)
            .option(() -> OptionOF.CHUNK_RENDER_DISTANCE)
            .label(Object::toString)
            .cycle(GameOptionRegistry::nextRenderDistance));

    public static final OptionDefinition<Boolean> FOG_FANCY = define(
        toggle("ofFogFancy", Config.DEF_FOG_FANCY, () -> OptionOF.FOG_FANCY)
            .label(value -> value ? "Fancy" : "Fast"));
    public static final OptionDefinition<Float> FOG_START = define(
        OptionDefinition.builder("ofFogStart", 0.8F, FLOAT)
            .option(() -> OptionOF.FOG_START)
            .normalize(value -> value < 0.2F ? 0.2F : value > 0.81F ? 0.8F : value)
            .cycle(value -> value + 0.2F > 0.81F ? 0.2F : value + 0.2F)
            .label(Object::toString));
    public static final OptionDefinition<Integer> MIPMAP_LEVEL = define(
        integer("ofMipmapLevel", 0, 0, 4).option(() -> OptionOF.MIPMAP_LEVEL)
            .cycle(value -> value >= 4 ? 0 : value + 1)
            .label(Object::toString).effect(textureEffect()));
    public static final OptionDefinition<Boolean> MIPMAP_LINEAR = define(
        toggle("ofMipmapLinear", false, () -> OptionOF.MIPMAP_TYPE)
            .label(value -> value ? "Linear" : "Nearest").effect(textureEffect()));
    public static final OptionDefinition<Boolean> LOAD_FAR = define(
        toggle("ofLoadFar", false, () -> OptionOF.LOAD_FAR).effect(terrainEffect()));
    public static final OptionDefinition<Integer> PRELOADED_CHUNKS = define(
        integer("ofPreloadedChunks", 0, 0, 8).option(() -> OptionOF.PRELOADED_CHUNKS)
            .cycle(value -> value + 2 > 8 ? 0 : value + 2)
            .label(value -> value == 0 ? "OFF" : value.toString()).effect(terrainEffect()));
    public static final OptionDefinition<Float> BRIGHTNESS = define(
        percentage("ofBrightness", 0.0F, () -> OptionOF.BRIGHTNESS, true)
            .effect((effects, value, source) -> effects.updateOptionBrightness()));
    public static final OptionDefinition<Float> AO_LEVEL = define(
        percentage("ofAoLevel", 0.0F, () -> OptionOF.AO_LEVEL, true)
            .effect((effects, value, source) -> effects.updateOptionAmbientOcclusion(value > 0.0F, source == INTERACTION)));
    public static final OptionDefinition<Integer> CLOUDS = define(
        quality("ofClouds", () -> OptionOF.CLOUDS, 3, 3));
    public static final OptionDefinition<Float> CLOUD_HEIGHT = define(
        percentage("ofCloudsHeight", 0.0F, () -> OptionOF.CLOUD_HEIGHT, true));
    public static final OptionDefinition<Integer> LEAVES = define(
        quality("ofTrees", () -> OptionOF.LEAVES, 2, 2).effect(terrainEffect()));
    public static final OptionDefinition<Integer> GRASS = define(
        quality("ofGrass", () -> OptionOF.GRASS, 2, 2).effect(terrainEffect()));
    public static final OptionDefinition<Integer> RAIN = define(
        quality("ofRain", () -> OptionOF.RAIN, 3, 3));
    // Legacy files accept Water=OFF (3); its button cycles only Default/Fast/Fancy.
    public static final OptionDefinition<Integer> WATER = define(
        quality("ofWater", () -> OptionOF.WATER, 3, 2));
    public static final OptionDefinition<Integer> ANIMATED_WATER = define(
        animation("ofAnimatedWater", () -> OptionOF.ANIMATED_WATER));
    public static final OptionDefinition<Integer> ANIMATED_LAVA = define(
        animation("ofAnimatedLava", () -> OptionOF.ANIMATED_LAVA));
    public static final OptionDefinition<Boolean> ANIMATED_FIRE = define(
        toggle("ofAnimatedFire", true, () -> OptionOF.ANIMATED_FIRE).effect(textureEffect()));
    public static final OptionDefinition<Boolean> ANIMATED_PORTAL = define(
        toggle("ofAnimatedPortal", true, () -> OptionOF.ANIMATED_PORTAL).effect(textureEffect()));
    public static final OptionDefinition<Boolean> ANIMATED_REDSTONE = define(
        toggle("ofAnimatedRedstone", true, () -> OptionOF.ANIMATED_REDSTONE));
    public static final OptionDefinition<Boolean> ANIMATED_EXPLOSION = define(
        toggle("ofAnimatedExplosion", true, () -> OptionOF.ANIMATED_EXPLOSION));
    public static final OptionDefinition<Boolean> ANIMATED_FLAME = define(
        toggle("ofAnimatedFlame", true, () -> OptionOF.ANIMATED_FLAME));
    public static final OptionDefinition<Boolean> ANIMATED_SMOKE = define(
        toggle("ofAnimatedSmoke", true, () -> OptionOF.ANIMATED_SMOKE));
    public static final OptionDefinition<Boolean> FAST_DEBUG_INFO = define(
        toggle("ofFastDebugInfo", false, () -> OptionOF.FAST_DEBUG_INFO));
    public static final OptionDefinition<Integer> AUTOSAVE_TICKS = define(
        integer("ofAutoSaveTicks", 4000, 40, 40000).option(() -> OptionOF.AUTOSAVE_TICKS)
            .cycle(value -> value * 10 > 40000 ? 40 : value * 10)
            .label(value -> value <= 40 ? "Default (2s)" : value <= 400 ? "20s" : value <= 4000 ? "3min" : "30min"));
    public static final OptionDefinition<ConnectedGrassOption> CONNECTED_GRASS = define(
        OptionDefinition.builder("ofConnectedGrass", ConnectedGrassOption.OFF,
                OptionDefinition.codec(GameOptionRegistry::decodeConnectedGrass))
            .option(() -> OptionOF.CONNECTED_GRASS)
            .cycle(value -> switch (value) {
                case OFF -> ConnectedGrassOption.FAST;
                case FAST -> ConnectedGrassOption.FANCY;
                case FANCY -> ConnectedGrassOption.OFF;
            })
            .label(value -> switch (value) {
                case OFF -> "OFF";
                case FAST -> "Fast";
                case FANCY -> "Fancy";
            }).effect(terrainEffect()));
    public static final OptionDefinition<Boolean> WEATHER = define(
        toggle("ofWeather", true, () -> OptionOF.WEATHER));
    public static final OptionDefinition<Boolean> SKY = define(
        toggle("ofSky", true, () -> OptionOF.SKY));
    public static final OptionDefinition<Boolean> STARS = define(
        toggle("ofStars", true, () -> OptionOF.STARS));
    public static final OptionDefinition<Boolean> CHUNK_UPDATES_DYNAMIC = define(
        toggle("ofChunkUpdatesDynamic", true, () -> OptionOF.CHUNK_UPDATES_DYNAMIC));
    public static final OptionDefinition<Integer> TIME = define(
        integer("ofTime", 0, 0, 2).option(() -> OptionOF.TIME)
            .cycle(value -> value >= 2 ? 0 : value + 1)
            .label(value -> value == 1 ? "Day Only" : value == 2 ? "Night Only" : "Default"));
    public static final OptionDefinition<Boolean> CLEAR_WATER = define(
        toggle("ofClearWater", false, () -> OptionOF.CLEAR_WATER)
            .effect((effects, value, source) -> effects.updateOptionWaterOpacity()));
    // Preserve old configuration values without exposing unsupported Vulkan controls.
    public static final OptionDefinition<Integer> AA_LEVEL = define(integer("ofAaLevel", 0, 0, 16));
    public static final OptionDefinition<Integer> AF_LEVEL = define(integer("ofAfLevel", 1, 1, 16));
    public static final OptionDefinition<Boolean> GRASS_3D = define(
        toggle("grass3d", true, () -> OptionOF.GRASS_3D)
            .label(GameOptionRegistry::localizedBoolean).effect(terrainEffect()));
    public static final OptionDefinition<Boolean> RAY_TRACING = define(
        toggle("acRayTracing", false, () -> OptionOF.RAY_TRACING)
            .label(GameOptionRegistry::localizedBoolean)
            .effect((effects, value, source) -> {
                if (source == INTERACTION) effects.updateOptionRayTracing(value);
            }));
    public static final OptionDefinition<Integer> INDIRECT_LIGHTING = define(
        integer("acIndirectLighting", 0, 0, 3).option(() -> OptionOF.INDIRECT_LIGHTING)
            .cycle(value -> (value + 1) % 4)
            .label(value -> value == 0 ? "Vertex cache" : "SHARC " + (64 << (value - 1)) + "MiB")
            .effect((effects, value, source) -> {
                if (source == INTERACTION) effects.updateOptionIndirectLighting(value);
            }));
    public static final OptionDefinition<Boolean> TEMPORAL_AA = define(
        toggle("acTemporalAa", false, () -> OptionOF.TEMPORAL_AA)
            .label(GameOptionRegistry::localizedBoolean)
            .effect((effects, value, source) -> {
                if (source == INTERACTION) effects.updateOptionTemporalAa(value);
            }));
    public static final OptionDefinition<Integer> RENDER_SCALE = define(
        integer("acRenderScale", 0, 0, 4).option(() -> OptionOF.RENDER_SCALE)
            .cycle(value -> (value + 1) % 5)
            .label((value, translations) -> switch (value) {
                case 1 -> "85%";
                case 2 -> "75%";
                case 3 -> "67%";
                case 4 -> "50%";
                default -> translations.get("options.ac.renderScale.native");
            })
            .effect((effects, value, source) -> {
                if (source == INTERACTION) effects.updateOptionRenderScale(value);
            }));
    public static final OptionDefinition<Integer> PARALLAX_MAPPING = define(
        integer("acParallaxMapping", 0, 0, 2).option(() -> OptionOF.PARALLAX_MAPPING)
            .cycle(value -> (value + 1) % 3)
            .label((value, translations) -> translations.get(switch (value) {
                case 1 -> "options.ac.parallaxMapping.fast";
                case 2 -> "options.ac.parallaxMapping.fancy";
                default -> "options.ac.parallaxMapping.off";
            }))
            .effect((effects, value, source) -> {
                if (source == INTERACTION) effects.updateOptionParallaxMapping(value);
            }));
    public static final OptionDefinition<Integer> RAY_RECONSTRUCTION = define(
        integer("acRayReconstruction", 0, 0, 2).option(() -> OptionOF.RAY_RECONSTRUCTION)
            .cycle((value, direction) -> nextNativeMode(NativeRenderingOptions.Group.RECONSTRUCTION, value, direction))
            .label((value, translations) -> translations.get(switch (value) {
                case 1 -> "options.ac.rayReconstruction.dlss";
                case 2 -> "options.ac.rayReconstruction.nrd";
                default -> "options.ac.rayReconstruction.builtin";
            }))
            .effect((effects, value, source) -> {
                if (source == INTERACTION) effects.updateOptionRayReconstruction(value);
            }));
    public static final OptionDefinition<Integer> UPSCALER = define(
        integer("acUpscaler", 0, 0, 2).option(() -> OptionOF.UPSCALER)
            .cycle((value, direction) -> nextNativeMode(NativeRenderingOptions.Group.UPSCALER, value, direction))
            .label((value, translations) -> translations.get(switch (value) {
                case 1 -> "options.ac.upscaler.dlss";
                case 2 -> "options.ac.upscaler.fsr";
                default -> "options.ac.upscaler.cubic";
            }))
            .effect((effects, value, source) -> {
                if (source == INTERACTION) effects.updateOptionUpscaler(value);
            }));
    public static final OptionDefinition<Integer> FRAME_GENERATION = define(
        integer("acFrameGeneration", 0, 0, 2).option(() -> OptionOF.FRAME_GENERATION)
            .cycle((value, direction) -> nextNativeMode(NativeRenderingOptions.Group.FRAME_GENERATION, value, direction))
            .label((value, translations) -> translations.get(switch (value) {
                case 1 -> "options.ac.frameGeneration.dlss";
                case 2 -> "options.ac.frameGeneration.fsr";
                default -> "options.ac.frameGeneration.off";
            }))
            .effect((effects, value, source) -> {
                if (source == INTERACTION) effects.updateOptionFrameGeneration(value);
            }));
    public static final OptionDefinition<Integer> CHAT_MESSAGE_BUFFER_LIMIT = define(
        integerSlider("chatMessageBufferLimit", 100, 1, 10000)
            .option(() -> OptionOF.CHAT_MESSAGE_BUFFER_LIMIT));
    public static final OptionDefinition<Integer> PARTICLE_LIMIT = define(
        integerSlider("particleLimit", 1024 * 4, 0, 1024 * 32, true)
            .option(() -> OptionOF.PARTICLE_LIMIT));
    public static final OptionDefinition<Boolean> ALLOW_JAVA_IN_SCRIPT = define(
        toggle("allowJavaInScript", false, () -> OptionOF.ALLOW_JAVA_IN_SCRIPT)
            .label(GameOptionRegistry::localizedBoolean));
    public static final OptionDefinition<Float> CHAT_WIDTH = define(
        // Historical files permit values outside the slider range; preserve them on load.
        percentage("chatWidth", 0.5F, () -> OptionOF.CHAT_WIDTH, false));

    public static final List<OptionDefinition<?>> VALUES = List.copyOf(DEFINITIONS);

    private GameOptionRegistry() {
    }

    private static int nextNativeMode(NativeRenderingOptions.Group group, int current, int direction) {
        int step = direction < 0 ? -1 : 1;
        for (int offset = 1; offset <= 3; offset++) {
            int candidate = Math.floorMod(current + step * offset, 3);
            if (candidate == 0 || NativeRenderingOptions.status(group, candidate).available()) return candidate;
        }
        return current;
    }

    private static <T> OptionDefinition<T> define(OptionDefinition.Builder<T> builder) {
        OptionDefinition<T> definition = builder.build();
        DEFINITIONS.add(definition);
        return definition;
    }

    private static OptionDefinition.Builder<Boolean> toggle(String key, boolean value, Supplier<Option> option) {
        return OptionDefinition.builder(key, value, BOOLEAN).option(option)
            .cycle(current -> !current).booleanValue(current -> current)
            .label(current -> current ? "ON" : "OFF");
    }

    private static OptionDefinition.Builder<Integer> integer(String key, int value, int min, int max) {
        return OptionDefinition.builder(key, value, INTEGER).normalize(current -> Config.limit(current, min, max));
    }

    private static OptionDefinition.Builder<Integer> integerSlider(String key, int value, int min, int max) {
        return integerSlider(key, value, min, max, false);
    }

    private static OptionDefinition.Builder<Integer> integerSlider(
        String key, int value, int min, int max, boolean displayPercentage) {
        OptionDefinition.Slider<Integer> slider = new OptionDefinition.Slider<>() {
            @Override
            public Integer value(float progress) {
                return (int) MathF.lerp(progress, min, max);
            }

            @Override
            public float progress(Integer current) {
                return (float) MathF.inverseLerp(current, min, max);
            }
        };
        return integer(key, value, min, max).slider(slider).label((current, translations) ->
            displayPercentage ? percentageLabel(slider.progress(current), translations) :
                slider.value(slider.progress(current)).toString());
    }

    private static OptionDefinition.Builder<Float> percentage(
        String key, float value, Supplier<Option> option, boolean constrainLoad) {
        return OptionDefinition.builder(key, value, FLOAT).option(option)
            .normalize(current -> constrainLoad ? Config.limit(current, 0.0F, 1.0F) : current)
            .slider(new OptionDefinition.Slider<>() {
                @Override
                public Float value(float progress) {
                    return progress;
                }

                @Override
                public float progress(Float current) {
                    return current;
                }
            }).label(GameOptionRegistry::percentageLabel);
    }

    private static String percentageLabel(float value, I18n translations) {
        return value == 0.0F ? translations.get("options.off") : (int) (value * 100.0F) + "%";
    }

    private static String localizedBoolean(boolean value, I18n translations) {
        return translations.get(value ? "options.on" : "options.off");
    }

    private static OptionDefinition.Builder<Integer> quality(String key, Supplier<Option> option, int max, int cycleMax) {
        return integer(key, 0, 0, max).option(option).cycle(value -> value >= cycleMax ? 0 : value + 1)
            .label(value -> switch (value) {
                case 1 -> "Fast";
                case 2 -> "Fancy";
                case 3 -> max == 3 ? "OFF" : "Default";
                default -> "Default";
            });
    }

    private static OptionDefinition.Builder<Integer> animation(String key, Supplier<Option> option) {
        return integer(key, 0, 0, 2).option(option).cycle(value -> value >= 2 ? 0 : value + 1)
            .label(value -> switch (value) {
                case 1 -> "Dynamic";
                case 2 -> "OFF";
                default -> "ON";
            }).effect(textureEffect());
    }

    private static <T> OptionDefinition.Effect<T> textureEffect() {
        return (effects, value, source) -> {
            if (source == INTERACTION) {
                effects.reloadOptionTextures();
            }
        };
    }

    private static <T> OptionDefinition.Effect<T> terrainEffect() {
        return (effects, value, source) -> {
            if (source == INTERACTION) {
                effects.rebuildOptionTerrain();
            }
        };
    }

    private static ConnectedGrassOption decodeConnectedGrass(String value) {
        try {
            return ConnectedGrassOption.valueOf(value);
        }
        catch (IllegalArgumentException ignored) {
            return ConnectedGrassOption.OFF;
        }
    }

    public static int resolveRenderDistance(int value, boolean nativeKeyPresent) {
        if (!nativeKeyPresent && value >= 0 && value <= 3) {
            value = LEGACY_FAR_CHUNK_DIAMETER >> value;
        }
        return CHUNK_RENDER_DISTANCE.normalize(value);
    }

    public static int nextRenderDistance(int current, int direction) {
        if (direction == 0) {
            return current;
        }
        int[] presets = {LEGACY_FAR_CHUNK_DIAMETER, LEGACY_FAR_CHUNK_DIAMETER >> 1,
            LEGACY_FAR_CHUNK_DIAMETER >> 2, LEGACY_FAR_CHUNK_DIAMETER >> 3};
        for (int i = 0; i < presets.length; i++) {
            int candidate = direction > 0 ? presets[i] : presets[presets.length - i - 1];
            if (direction > 0 ? candidate < current : candidate > current) {
                return candidate;
            }
        }
        return direction > 0 ? presets[0] : presets[presets.length - 1];
    }
}
