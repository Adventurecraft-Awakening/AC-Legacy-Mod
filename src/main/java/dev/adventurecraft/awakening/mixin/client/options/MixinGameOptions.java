package dev.adventurecraft.awakening.mixin.client.options;

import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.client.render.resource.MipmapMode;
import dev.adventurecraft.awakening.client.render.RenderSystem;
import dev.adventurecraft.awakening.client.options.Config;
import dev.adventurecraft.awakening.client.options.ConnectedGrassOption;
import dev.adventurecraft.awakening.client.options.GameOptionRegistry;
import dev.adventurecraft.awakening.client.options.OptionBindings;
import dev.adventurecraft.awakening.client.options.OptionEffects;
import dev.adventurecraft.awakening.extension.client.options.ExGameOptions;
import dev.adventurecraft.awakening.extension.client.render.ExWorldEventRenderer;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.locale.I18n;
import net.minecraft.util.Language;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.tile.Tile;
import dev.adventurecraft.awakening.client.platform.window.GameWindow;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mixin(Options.class)
public abstract class MixinGameOptions implements ExGameOptions, OptionEffects {

    @Unique private static final int MAX_CHUNK_SIMULATION_DISTANCE = 9;

    @Final @Shadow private static String[] DIFFICULTIES;
    @Final @Shadow private static String[] GUI_SCALES;
    @Final @Shadow private static String[] PERFORMANCE_OPTIONS;

    @Shadow public int viewDistance;
    @Shadow public boolean advancedOpengl;
    @Shadow public int fpsLimit;
    @Shadow public boolean fancyGraphics;
    @Shadow public boolean ao;
    @Shadow public KeyMapping keyUp;
    @Shadow public KeyMapping keyLeft;
    @Shadow public KeyMapping keyDown;
    @Shadow public KeyMapping keyRight;
    @Shadow public KeyMapping keyJump;
    @Shadow public KeyMapping keyInventory;
    @Shadow public KeyMapping keyDrop;
    @Shadow public KeyMapping keyChat;
    @Shadow public KeyMapping keyFog;
    @Shadow public KeyMapping keySneak;
    @Shadow protected Minecraft minecraft;
    @Shadow public int difficulty;
    @Shadow public int guiScale;

    @Unique public boolean ofFogFancy = GameOptionRegistry.FOG_FANCY.defaultValue();
    @Unique public float ofFogStart = GameOptionRegistry.FOG_START.defaultValue();
    @Unique public int ofMipmapLevel = GameOptionRegistry.MIPMAP_LEVEL.defaultValue();
    @Unique public boolean ofMipmapLinear = GameOptionRegistry.MIPMAP_LINEAR.defaultValue();
    @Unique public boolean ofLoadFar = GameOptionRegistry.LOAD_FAR.defaultValue();
    @Unique public int ofPreloadedChunks = GameOptionRegistry.PRELOADED_CHUNKS.defaultValue();
    @Unique public float ofBrightness = GameOptionRegistry.BRIGHTNESS.defaultValue();
    @Unique public float ofAoLevel = GameOptionRegistry.AO_LEVEL.defaultValue();
    @Unique public int ofAaLevel = GameOptionRegistry.AA_LEVEL.defaultValue();
    @Unique public int ofAfLevel = GameOptionRegistry.AF_LEVEL.defaultValue();
    @Unique public int ofClouds = GameOptionRegistry.CLOUDS.defaultValue();
    @Unique public float ofCloudsHeight = GameOptionRegistry.CLOUD_HEIGHT.defaultValue();
    @Unique public int ofTrees = GameOptionRegistry.LEAVES.defaultValue();
    @Unique public int ofGrass = GameOptionRegistry.GRASS.defaultValue();
    @Unique public int ofRain = GameOptionRegistry.RAIN.defaultValue();
    @Unique public int ofWater = GameOptionRegistry.WATER.defaultValue();
    @Unique public ConnectedGrassOption ofConnectedGrass = GameOptionRegistry.CONNECTED_GRASS.defaultValue();
    @Unique public int ofAutoSaveTicks = GameOptionRegistry.AUTOSAVE_TICKS.defaultValue();
    @Unique public boolean ofFastDebugInfo = GameOptionRegistry.FAST_DEBUG_INFO.defaultValue();
    @Unique public boolean ofWeather = GameOptionRegistry.WEATHER.defaultValue();
    @Unique public boolean ofSky = GameOptionRegistry.SKY.defaultValue();
    @Unique public boolean ofStars = GameOptionRegistry.STARS.defaultValue();
    @Unique public boolean ofChunkUpdatesDynamic = GameOptionRegistry.CHUNK_UPDATES_DYNAMIC.defaultValue();
    @Unique public int ofTime = GameOptionRegistry.TIME.defaultValue();
    @Unique public boolean ofClearWater = GameOptionRegistry.CLEAR_WATER.defaultValue();
    @Unique public int ofAnimatedWater = GameOptionRegistry.ANIMATED_WATER.defaultValue();
    @Unique public int ofAnimatedLava = GameOptionRegistry.ANIMATED_LAVA.defaultValue();
    @Unique public boolean ofAnimatedFire = GameOptionRegistry.ANIMATED_FIRE.defaultValue();
    @Unique public boolean ofAnimatedPortal = GameOptionRegistry.ANIMATED_PORTAL.defaultValue();
    @Unique public boolean ofAnimatedRedstone = GameOptionRegistry.ANIMATED_REDSTONE.defaultValue();
    @Unique public boolean ofAnimatedExplosion = GameOptionRegistry.ANIMATED_EXPLOSION.defaultValue();
    @Unique public boolean ofAnimatedFlame = GameOptionRegistry.ANIMATED_FLAME.defaultValue();
    @Unique public boolean ofAnimatedSmoke = GameOptionRegistry.ANIMATED_SMOKE.defaultValue();
    @Unique public KeyMapping ofKeyBindZoom;
    @Unique public List<KeyMapping> keyBindings;
    @Unique public boolean grass3d = GameOptionRegistry.GRASS_3D.defaultValue();
    @Unique private boolean ac$rayTracing = GameOptionRegistry.RAY_TRACING.defaultValue();
    @Unique private int ac$indirectLighting = GameOptionRegistry.INDIRECT_LIGHTING.defaultValue();
    @Unique private boolean ac$temporalAa = GameOptionRegistry.TEMPORAL_AA.defaultValue();
    @Unique private int ac$renderScale = GameOptionRegistry.RENDER_SCALE.defaultValue();
    @Unique private int ac$parallaxMapping = GameOptionRegistry.PARALLAX_MAPPING.defaultValue();
    @Unique private int ac$rayReconstruction = GameOptionRegistry.RAY_RECONSTRUCTION.defaultValue();
    @Unique private int ac$upscaler = GameOptionRegistry.UPSCALER.defaultValue();
    @Unique private int ac$frameGeneration = GameOptionRegistry.FRAME_GENERATION.defaultValue();
    @Unique public int chatMessageBufferLimit = GameOptionRegistry.CHAT_MESSAGE_BUFFER_LIMIT.defaultValue();
    @Unique public int particleLimit = GameOptionRegistry.PARTICLE_LIMIT.defaultValue();
    @Unique public boolean allowJavaInScript = GameOptionRegistry.ALLOW_JAVA_IN_SCRIPT.defaultValue();
    @Unique public float chatWidth = GameOptionRegistry.CHAT_WIDTH.defaultValue();
    @Unique private boolean ac$hasStoredChunkRenderDistance;
    @Unique private int ac$storedChunkRenderDistance;
    @Unique private boolean ac$renderDistanceResolved;

    @Unique private OptionBindings ac$optionBindings;

    @Unique
    private OptionBindings ac$options() {
        if (this.ac$optionBindings == null) {
            // Direct bindings retain the public legacy fields as the sole source of state.
            this.ac$optionBindings = new OptionBindings(this)
                .bind(GameOptionRegistry.CHUNK_RENDER_DISTANCE, () -> this.viewDistance, value -> this.viewDistance = value)
                .bind(GameOptionRegistry.FOG_FANCY, () -> this.ofFogFancy, value -> this.ofFogFancy = value)
                .bind(GameOptionRegistry.FOG_START, () -> this.ofFogStart, value -> this.ofFogStart = value)
                .bind(GameOptionRegistry.MIPMAP_LEVEL, () -> this.ofMipmapLevel, value -> this.ofMipmapLevel = value)
                .bind(GameOptionRegistry.MIPMAP_LINEAR, () -> this.ofMipmapLinear, value -> this.ofMipmapLinear = value)
                .bind(GameOptionRegistry.LOAD_FAR, () -> this.ofLoadFar, value -> this.ofLoadFar = value)
                .bind(GameOptionRegistry.PRELOADED_CHUNKS, () -> this.ofPreloadedChunks, value -> this.ofPreloadedChunks = value)
                .bind(GameOptionRegistry.BRIGHTNESS, () -> this.ofBrightness, value -> this.ofBrightness = value)
                .bind(GameOptionRegistry.AO_LEVEL, () -> this.ofAoLevel, value -> this.ofAoLevel = value)
                .bind(GameOptionRegistry.CLOUDS, () -> this.ofClouds, value -> this.ofClouds = value)
                .bind(GameOptionRegistry.CLOUD_HEIGHT, () -> this.ofCloudsHeight, value -> this.ofCloudsHeight = value)
                .bind(GameOptionRegistry.LEAVES, () -> this.ofTrees, value -> this.ofTrees = value)
                .bind(GameOptionRegistry.GRASS, () -> this.ofGrass, value -> this.ofGrass = value)
                .bind(GameOptionRegistry.RAIN, () -> this.ofRain, value -> this.ofRain = value)
                .bind(GameOptionRegistry.WATER, () -> this.ofWater, value -> this.ofWater = value)
                .bind(GameOptionRegistry.ANIMATED_WATER, () -> this.ofAnimatedWater, value -> this.ofAnimatedWater = value)
                .bind(GameOptionRegistry.ANIMATED_LAVA, () -> this.ofAnimatedLava, value -> this.ofAnimatedLava = value)
                .bind(GameOptionRegistry.ANIMATED_FIRE, () -> this.ofAnimatedFire, value -> this.ofAnimatedFire = value)
                .bind(GameOptionRegistry.ANIMATED_PORTAL, () -> this.ofAnimatedPortal, value -> this.ofAnimatedPortal = value)
                .bind(GameOptionRegistry.ANIMATED_REDSTONE, () -> this.ofAnimatedRedstone, value -> this.ofAnimatedRedstone = value)
                .bind(GameOptionRegistry.ANIMATED_EXPLOSION, () -> this.ofAnimatedExplosion, value -> this.ofAnimatedExplosion = value)
                .bind(GameOptionRegistry.ANIMATED_FLAME, () -> this.ofAnimatedFlame, value -> this.ofAnimatedFlame = value)
                .bind(GameOptionRegistry.ANIMATED_SMOKE, () -> this.ofAnimatedSmoke, value -> this.ofAnimatedSmoke = value)
                .bind(GameOptionRegistry.FAST_DEBUG_INFO, () -> this.ofFastDebugInfo, value -> this.ofFastDebugInfo = value)
                .bind(GameOptionRegistry.AUTOSAVE_TICKS, () -> this.ofAutoSaveTicks, value -> this.ofAutoSaveTicks = value)
                .bind(GameOptionRegistry.CONNECTED_GRASS, () -> this.ofConnectedGrass, value -> this.ofConnectedGrass = value)
                .bind(GameOptionRegistry.WEATHER, () -> this.ofWeather, value -> this.ofWeather = value)
                .bind(GameOptionRegistry.SKY, () -> this.ofSky, value -> this.ofSky = value)
                .bind(GameOptionRegistry.STARS, () -> this.ofStars, value -> this.ofStars = value)
                .bind(GameOptionRegistry.CHUNK_UPDATES_DYNAMIC, () -> this.ofChunkUpdatesDynamic, value -> this.ofChunkUpdatesDynamic = value)
                .bind(GameOptionRegistry.TIME, () -> this.ofTime, value -> this.ofTime = value)
                .bind(GameOptionRegistry.CLEAR_WATER, () -> this.ofClearWater, value -> this.ofClearWater = value)
                .bind(GameOptionRegistry.AA_LEVEL, () -> this.ofAaLevel, value -> this.ofAaLevel = value)
                .bind(GameOptionRegistry.AF_LEVEL, () -> this.ofAfLevel, value -> this.ofAfLevel = value)
                .bind(GameOptionRegistry.GRASS_3D, () -> this.grass3d, value -> this.grass3d = value)
                .bind(GameOptionRegistry.RAY_TRACING, this::rayTracing, value -> this.ac$rayTracing = value)
                .bind(GameOptionRegistry.INDIRECT_LIGHTING, this::indirectLighting, value -> this.ac$indirectLighting = value)
                .bind(GameOptionRegistry.TEMPORAL_AA, this::temporalAa, value -> this.ac$temporalAa = value)
                .bind(GameOptionRegistry.RENDER_SCALE, this::renderScale, value -> this.ac$renderScale = value)
                .bind(GameOptionRegistry.PARALLAX_MAPPING, this::parallaxMapping, value -> this.ac$parallaxMapping = value)
                .bind(GameOptionRegistry.RAY_RECONSTRUCTION, this::rayReconstruction, value -> this.ac$rayReconstruction = value)
                .bind(GameOptionRegistry.UPSCALER, this::upscaler, value -> this.ac$upscaler = value)
                .bind(GameOptionRegistry.FRAME_GENERATION, this::frameGeneration, value -> this.ac$frameGeneration = value)
                .bind(GameOptionRegistry.CHAT_MESSAGE_BUFFER_LIMIT, () -> this.chatMessageBufferLimit, value -> this.chatMessageBufferLimit = value)
                .bind(GameOptionRegistry.PARTICLE_LIMIT, () -> this.particleLimit, value -> this.particleLimit = value)
                .bind(GameOptionRegistry.ALLOW_JAVA_IN_SCRIPT, () -> this.allowJavaInScript, value -> this.allowJavaInScript = value)
                .bind(GameOptionRegistry.CHAT_WIDTH, () -> this.chatWidth, value -> this.chatWidth = value)
                .complete();
        }
        return this.ac$optionBindings;
    }

    @Shadow
    public abstract float getProgressValue(Option option);

    @Shadow
    public abstract boolean getBooleanValue(Option option);

    @Inject(
        method = "<init>(Lnet/minecraft/client/Minecraft;Ljava/io/File;)V",
        at = @At("TAIL")
    )
    private void init(Minecraft file, File par2, CallbackInfo ci) {
        this.sharedInit();
    }

    @Inject(
        method = "<init>()V",
        at = @At("TAIL")
    )
    private void init(CallbackInfo ci) {
        this.sharedInit();
    }

    private void sharedInit() {
        this.advancedOpengl = false;
        this.ac$resolveRenderDistance();
        this.keyBindings = new ArrayList<>();
        this.ofKeyBindZoom = new KeyMapping("key.ofZoom", 46);

        this.keyBindings.add(this.keyUp);
        this.keyBindings.add(this.keyLeft);
        this.keyBindings.add(this.keyDown);
        this.keyBindings.add(this.keyRight);
        this.keyBindings.add(this.keyJump);
        this.keyBindings.add(this.keySneak);
        this.keyBindings.add(this.keyDrop);
        this.keyBindings.add(this.keyInventory);
        this.keyBindings.add(this.keyChat);
        this.keyBindings.add(this.keyFog);
        this.keyBindings.add(this.ofKeyBindZoom);
    }

    @Inject(method = "load", at = @At("HEAD"))
    private void ac$beginLoadingRenderDistance(CallbackInfo ci) {
        this.ac$hasStoredChunkRenderDistance = false;
        this.ac$renderDistanceResolved = false;
    }

    @Inject(method = "load", at = @At("RETURN"))
    private void ac$finishLoadingRenderDistance(CallbackInfo ci) {
        this.advancedOpengl = false;
        this.ac$resolveRenderDistance();
    }

    @Unique
    private void ac$resolveRenderDistance() {
        if (this.ac$renderDistanceResolved) {
            return;
        }
        int distance = this.ac$hasStoredChunkRenderDistance ? this.ac$storedChunkRenderDistance : this.viewDistance;
        // Resolve after every line has loaded, so the native key wins regardless of file order.
        this.viewDistance = GameOptionRegistry.resolveRenderDistance(distance, this.ac$hasStoredChunkRenderDistance);
        this.ac$renderDistanceResolved = true;
    }

    @Redirect(
        method = "toggle",
        at = @At(
            value = "FIELD",
            opcode = Opcodes.PUTFIELD,
            target = "Lnet/minecraft/client/Options;viewDistance:I"
        )
    )
    private void ac$cycleChunkRenderDistance(Options options, int legacyValue, @Local(argsOnly = true) int step) {
        // The vanilla assignment masks to 0..3; keep native chunk-grid diameters.
        this.viewDistance = GameOptionRegistry.nextRenderDistance(this.viewDistance, step);
    }

    @Inject(
        method = "set",
        at = @At("TAIL")
    )
    private void setFloatOptionOF(Option option, float value, CallbackInfo ci) {
        this.ac$options().setProgress(option, value);
    }

    @Override
    public void reloadOptionTextures() {
        this.minecraft.textures.reloadAll();
    }

    @Override
    public void rebuildOptionTerrain() {
        this.minecraft.levelRenderer.allChanged();
    }

    @Override
    public void updateOptionAmbientOcclusion(boolean enabled, boolean rebuildTerrain) {
        this.ao = enabled;
        if (rebuildTerrain) {
            this.rebuildOptionTerrain();
        }
    }

    @Override
    public void updateOptionBrightness() {
        if (this.minecraft.level != null) {
            ((ExWorld) this.minecraft.level).loadBrightness();
            if (!RenderSystem.renderer().rayTracingSupported()) {
                ((ExWorldEventRenderer) this.minecraft.levelRenderer).updateAllTheRenderers();
            }
        }
    }

    @Override
    public void updateOptionRayTracing(boolean enabled) {
        if (!RenderSystem.isInitialized()) return;
        this.ac$rayTracing = enabled && RenderSystem.rayTracingAvailable();
        boolean changed = RenderSystem.rayTracingEnabled() != this.ac$rayTracing;
        RenderSystem.setRayTracingEnabled(this.ac$rayTracing);
        if (changed && this.minecraft != null && this.minecraft.level != null) {
            this.rebuildOptionTerrain();
        }
    }

    @Override
    public boolean rayTracing() {
        return this.ac$rayTracing && (!RenderSystem.isInitialized() || RenderSystem.rayTracingAvailable());
    }

    @Override
    public void updateOptionIndirectLighting(int mode) {
        this.ac$indirectLighting = GameOptionRegistry.INDIRECT_LIGHTING.normalize(mode);
        RenderSystem.setIndirectLighting(this.ac$indirectLighting);
    }

    @Override
    public int indirectLighting() { return this.ac$indirectLighting; }

    @Override
    public void updateOptionTemporalAa(boolean enabled) {
        this.ac$temporalAa = enabled;
        RenderSystem.setTemporalAaEnabled(enabled);
    }

    @Override
    public boolean temporalAa() { return this.ac$temporalAa; }

    @Override
    public void updateOptionRenderScale(int mode) {
        this.ac$renderScale = GameOptionRegistry.RENDER_SCALE.normalize(mode);
        RenderSystem.setRenderScale(this.ac$renderScale);
    }

    @Override
    public int renderScale() { return this.ac$renderScale; }

    @Override
    public void updateOptionParallaxMapping(int mode) {
        this.ac$parallaxMapping = GameOptionRegistry.PARALLAX_MAPPING.normalize(mode);
        RenderSystem.setParallaxMapping(this.ac$parallaxMapping);
    }

    @Override
    public int parallaxMapping() { return this.ac$parallaxMapping; }

    @Override
    public void updateOptionRayReconstruction(int mode) {
        this.ac$rayReconstruction = GameOptionRegistry.RAY_RECONSTRUCTION.normalize(mode);
        RenderSystem.setRayReconstruction(this.ac$rayReconstruction);
    }

    @Override
    public int rayReconstruction() { return this.ac$rayReconstruction; }

    @Override
    public void updateOptionUpscaler(int mode) {
        this.ac$upscaler = GameOptionRegistry.UPSCALER.normalize(mode);
        RenderSystem.setUpscaler(this.ac$upscaler);
    }

    @Override
    public int upscaler() { return this.ac$upscaler; }

    @Override
    public void updateOptionFrameGeneration(int mode) {
        this.ac$frameGeneration = GameOptionRegistry.FRAME_GENERATION.normalize(mode);
        RenderSystem.setFrameGeneration(this.ac$frameGeneration);
    }

    @Override
    public int frameGeneration() { return this.ac$frameGeneration; }

    @Override
    public void updateOptionWaterOpacity() {
        int waterOpacity = 3;
        if (this.ofClearWater) {
            waterOpacity = 1;
        }

        Tile.WATER.setLightBlock(waterOpacity);
        Tile.FLOWING_WATER.setLightBlock(waterOpacity);

        if (this.minecraft.level == null) {
            return;
        }
        ChunkSource source = this.minecraft.level.chunkSource;
        if (source == null) {
            return;
        }
        for (int x = -512; x < 512; ++x) {
            for (int z = -512; z < 512; ++z) {
                if (source.hasChunk(x, z)) {
                    LevelChunk chunk = source.getChunk(x, z);
                    if (chunk != null) {
                        byte[] var6 = chunk.skyLight.data;

                        Arrays.fill(var6, (byte) 0);

                        chunk.recalcHeightmap();
                    }
                }
            }
        }

        this.minecraft.levelRenderer.allChanged();
    }

    @Redirect(
        method = "toggle",
        at = @At(
            value = "FIELD",
            opcode = Opcodes.PUTFIELD,
            target = "Lnet/minecraft/client/Options;advancedOpengl:Z",
            ordinal = 0
        )
    )
    private void setIntOptionOF_ADVANCED_OPENGL(Options instance, boolean value) {
        this.advancedOpengl = false;

        ((ExWorldEventRenderer) this.minecraft.levelRenderer).setAllRenderersVisible();
    }

    @Redirect(
        method = "toggle",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/LevelRenderer;allChanged()V",
            ordinal = 0
        )
    )
    private void disableChangeEventForAdvancedOpenGL(LevelRenderer instance) {
    }

    @Redirect(
        method = "toggle",
        at = @At(
            value = "FIELD",
            opcode = Opcodes.PUTFIELD,
            target = "Lnet/minecraft/client/Options;fpsLimit:I",
            ordinal = 0
        )
    )
    private void setIntOptionPutFpsLimit(Options instance, int value, @Local(argsOnly = true) int i) {
        this.fpsLimit = (this.fpsLimit + i) % 4;
        GameWindow.setVSyncEnabled(this.fpsLimit == 3);
    }

    @Inject(
        method = "toggle",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Options;save()V",
            shift = At.Shift.BEFORE
        )
    )
    private void setIntOptionOF(Option option, int value, CallbackInfo ci) {
        this.ac$options().cycle(option, value);
    }

    @Inject(
        method = "getProgressValue",
        at = @At("HEAD"),
        cancellable = true
    )
    private void getFloatValueOF(Option option, CallbackInfoReturnable<Float> cir) {
        Float value = this.ac$options().progress(option);
        if (value != null) {
            cir.setReturnValue(value);
        }
    }

    @Inject(
        method = "getBooleanValue",
        at = @At("HEAD"),
        cancellable = true
    )
    private void getBooleanValueOF(Option option, CallbackInfoReturnable<Boolean> cir) {
        Boolean value = this.ac$options().booleanValue(option);
        if (value != null) {
            cir.setReturnValue(value);
        }
    }

    @Overwrite
    public String getMessage(Option option) {
        I18n ts = I18n.getInstance();
        String name = ts.get(option.getCaptionId());
        if (name == null) {
            name = option.getCaptionId();
        }
        String prefix = name + ": ";
        String customValue = this.ac$options().label(option, ts);
        if (customValue != null) {
            return prefix + customValue;
        }
        if (option == Option.RENDER_DISTANCE) {
            return prefix + this.viewDistance;
        }
        if (option.isProgress()) {
            float value = this.getProgressValue(option);
            if (option == Option.SENSITIVITY) {
                if (value == 0.0F) {
                    return prefix + ts.get("options.sensitivity.min");
                }
                if (value == 1.0F) {
                    return prefix + ts.get("options.sensitivity.max");
                }
                return prefix + (int) (value * 200.0F) + "%";
            }
            return prefix + (value == 0.0F ? ts.get("options.off") : (int) (value * 100.0F) + "%");
        }
        if (option.isBoolean()) {
            return prefix + ts.get(this.getBooleanValue(option) ? "options.on" : "options.off");
        }
        if (option == Option.DIFFICULTY) {
            return prefix + ts.get(DIFFICULTIES[this.difficulty]);
        }
        if (option == Option.GUI_SCALE) {
            return prefix + ts.get(GUI_SCALES[this.guiScale]);
        }
        if (option == Option.FRAMERATE_LIMIT) {
            return prefix + (this.fpsLimit == 3 ? "VSync" : Language.getOrDefault(PERFORMANCE_OPTIONS[this.fpsLimit]));
        }
        if (option == Option.GRAPHICS) {
            return prefix + ts.get(this.fancyGraphics ? "options.graphics.fancy" : "options.graphics.fast");
        }
        return prefix;
    }

    @Inject(
        method = "load",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Options;fpsLimit:I",
            shift = At.Shift.AFTER
        )
    )
    private void load_fpsLimit(CallbackInfo ci) {
        GameWindow.setVSyncEnabled(this.fpsLimit == 3);
    }

    @Inject(
        method = "load",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Options;ao:Z",
            shift = At.Shift.AFTER
        )
    )
    private void load_ao(CallbackInfo ci) {
        if (this.ao) {
            this.ofAoLevel = 1.0F;
        }
        else {
            this.ofAoLevel = 0.0F;
        }
    }

    @Inject(
        method = "load",
        at = @At(
            value = "INVOKE",
            target = "Ljava/lang/String;equals(Ljava/lang/Object;)Z",
            shift = At.Shift.BEFORE,
            ordinal = 0
        )
    )
    private void loadOF(CallbackInfo ci, @Local String[] parts) {
        if (parts.length < 2) {
            return;
        }
        if (GameOptionRegistry.CHUNK_RENDER_DISTANCE.key().equals(parts[0])) {
            this.ac$storedChunkRenderDistance = GameOptionRegistry.CHUNK_RENDER_DISTANCE.decode(parts[1]);
            this.ac$hasStoredChunkRenderDistance = true;
        }
        else {
            this.ac$options().load(parts[0], parts[1]);
        }
    }

    @Inject(
        method = "save",
        at = @At(
            value = "INVOKE",
            target = "Ljava/io/PrintWriter;close()V",
            shift = At.Shift.BEFORE
        )
    )
    private void saveOptionsOF(CallbackInfo ci, @Local PrintWriter writer) {
        this.ac$options().save(writer);
    }

    @Override
    public boolean ofFogFancy() {
        return ofFogFancy;
    }

    @Override
    public float ofFogStart() {
        return ofFogStart;
    }

    @Override
    public int ofMipmapLevel() {
        return ofMipmapLevel;
    }

    @Override
    public MipmapMode ofMipmapMode() {
        return ofMipmapLinear ? MipmapMode.LINEAR : MipmapMode.NEAREST;
    }

    @Override
    public boolean ofLoadFar() {
        return ofLoadFar;
    }

    @Override
    public int ofPreloadedChunks() {
        return ofPreloadedChunks;
    }

    @Override
    public int ofChunkRenderDistance() {
        return Config.limit(this.viewDistance,
            GameOptionRegistry.MIN_CHUNK_RENDER_DISTANCE, GameOptionRegistry.MAX_CHUNK_RENDER_DISTANCE)
            + this.ofPreloadedChunks();
    }

    @Override
    public int ofChunkSimulationDistance() {
        if (this.ofLoadFar()) {
            return MAX_CHUNK_SIMULATION_DISTANCE;
        }
        return Math.min(this.ofChunkRenderDistance(), MAX_CHUNK_SIMULATION_DISTANCE);
    }

    @Override
    public int ofChunkLoadDistance() {
        int render = this.ofChunkRenderDistance();
        int sim = this.ofChunkSimulationDistance() + this.ofPreloadedChunks();
        return Math.max(render, sim) + CHUNK_DISTANCE_BORDER;
    }

    @Override
    public boolean ofOcclusionFancy() {
        return false;
    }

    @Override
    public boolean isOcclusionEnabled() {
        return false;
    }

    @Override
    public boolean isOcclusionFancy() {
        return false;
    }

    @Override
    public float ofBrightness() {
        return ofBrightness;
    }

    @Override
    public float ofAoLevel() {
        return ofAoLevel;
    }

    @Override
    public int ofAaLevel() {
        return ofAaLevel;
    }

    @Override
    public int ofAfLevel() {
        return ofAfLevel;
    }

    @Override
    public int ofClouds() {
        return ofClouds;
    }

    @Override
    public boolean isCloudsOff() {
        return ofClouds() == 3;
    }

    @Override
    public boolean isCloudsFancy() {
        if (ofClouds() == 0) {
            return fancyGraphics;
        }
        return ofClouds() == 2;
    }

    @Override
    public float ofCloudsHeight() {
        return ofCloudsHeight;
    }

    @Override
    public int ofLeaves() {
        return ofTrees;
    }

    @Override
    public boolean isLeavesFancy() {
        if (ofLeaves() == 0) {
            return fancyGraphics;
        }
        return ofLeaves() == 2;
    }

    @Override
    public int ofGrass() {
        return ofGrass;
    }

    @Override
    public boolean isGrassFancy() {
        if (ofGrass() == 0) {
            return fancyGraphics;
        }
        return ofGrass() == 2;
    }

    @Override
    public int ofRain() {
        return ofRain;
    }

    @Override
    public boolean isRainOff() {
        return ofRain() == 3;
    }

    @Override
    public boolean isRainFancy() {
        if (ofRain() == 0) {
            return fancyGraphics;
        }
        return ofRain() == 2;
    }

    @Override
    public int ofWater() {
        return ofWater;
    }

    @Override
    public boolean isWaterFancy() {
        if (ofWater() == 0) {
            return fancyGraphics;
        }
        return ofWater() == 2;
    }

    @Override
    public ConnectedGrassOption ofConnectedGrass() {
        return ofConnectedGrass;
    }

    @Override
    public int ofAutoSaveTicks() {
        return ofAutoSaveTicks;
    }

    @Override
    public boolean ofFastDebugInfo() {
        return ofFastDebugInfo;
    }

    @Override
    public boolean ofWeather() {
        return ofWeather;
    }

    @Override
    public boolean ofSky() {
        return ofSky;
    }

    @Override
    public boolean ofStars() {
        return ofStars;
    }

    @Override
    public boolean ofChunkUpdatesDynamic() {
        return ofChunkUpdatesDynamic;
    }

    @Override
    public int ofTime() {
        return ofTime;
    }

    @Override
    public boolean isTimeDayOnly() {
        return ofTime() == 1;
    }

    @Override
    public boolean isTimeNightOnly() {
        return ofTime() == 2;
    }

    @Override
    public boolean ofClearWater() {
        return ofClearWater;
    }

    @Override
    public int ofAnimatedWater() {
        return ofAnimatedWater;
    }

    @Override
    public int ofAnimatedLava() {
        return ofAnimatedLava;
    }

    @Override
    public boolean ofAnimatedFire() {
        return ofAnimatedFire;
    }

    @Override
    public boolean ofAnimatedPortal() {
        return ofAnimatedPortal;
    }

    @Override
    public boolean ofAnimatedRedstone() {
        return ofAnimatedRedstone;
    }

    @Override
    public boolean ofAnimatedExplosion() {
        return ofAnimatedExplosion;
    }

    @Override
    public boolean ofAnimatedFlame() {
        return ofAnimatedFlame;
    }

    @Override
    public boolean ofAnimatedSmoke() {
        return ofAnimatedSmoke;
    }

    @Override
    public KeyMapping ofKeyBindZoom() {
        return ofKeyBindZoom;
    }

    @Override
    public boolean isGrass3d() {
        return this.grass3d;
    }

    @Override
    public int getChatMessageBufferLimit() {
        return this.chatMessageBufferLimit;
    }

    @Override
    public int getParticleLimit() {
        return this.particleLimit;
    }

    @Override
    public boolean getAllowJavaInScript() {
        return this.allowJavaInScript;
    }

    @Override
    public float getChatWidth() {
        return this.chatWidth;
    }
}
