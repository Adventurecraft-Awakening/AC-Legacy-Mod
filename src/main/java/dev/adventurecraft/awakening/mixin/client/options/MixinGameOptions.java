package dev.adventurecraft.awakening.mixin.client.options;

import dev.adventurecraft.awakening.client.options.OptionOF;
import dev.adventurecraft.awakening.client.options.Config;
import dev.adventurecraft.awakening.extension.client.options.ExGameOptions;
import dev.adventurecraft.awakening.extension.client.render.ExGameRenderer;
import dev.adventurecraft.awakening.extension.client.render.ExWorldEventRenderer;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.options.Option;
import net.minecraft.client.render.block.BlockRenderer;
import net.minecraft.client.resource.language.Internationalization;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.source.WorldSource;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.BufferedReader;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

@Mixin(GameOptions.class)
public abstract class MixinGameOptions implements ExGameOptions {

    private static final int DEFAULT = 0;
    private static final int FAST = 1;
    private static final int FANCY = 2;
    private static final int OFF = 3;
    private static final int ANIM_ON = 0;
    private static final int ANIM_GENERATED = 1;
    private static final int ANIM_OFF = 2;

    @Final
    @Shadow
    private static String[] renderDistanceTranslationKeys;
    @Final
    @Shadow
    private static String[] difficultyTranslationKeys;
    @Final
    @Shadow
    private static String[] guiScaleTranslationKeys;
    @Final
    @Shadow
    private static String[] performanceTranslationKeys;

    @Shadow
    public int viewDistance;
    @Shadow
    public boolean advancedOpengl;
    @Shadow
    public int fpsLimit;
    @Shadow
    public boolean fancyGraphics;
    @Shadow
    public boolean ao;
    @Shadow
    public KeyBinding forwardKey;
    @Shadow
    public KeyBinding leftKey;
    @Shadow
    public KeyBinding backKey;
    @Shadow
    public KeyBinding rightKey;
    @Shadow
    public KeyBinding jumpKey;
    @Shadow
    public KeyBinding inventoryKey;
    @Shadow
    public KeyBinding dropKey;
    @Shadow
    public KeyBinding chatKey;
    @Shadow
    public KeyBinding fogKey;
    @Shadow
    public KeyBinding sneakKey;
    @Shadow
    protected Minecraft client;

    public boolean ofFogFancy = false;
    public float ofFogStart = 0.8F;
    public int ofMipmapLevel = 0;
    public boolean ofMipmapLinear = false;
    public boolean ofLoadFar = false;
    public int ofPreloadedChunks = 0;
    public boolean ofOcclusionFancy = false;
    public boolean ofSmoothFps = false;
    public boolean ofSmoothInput = false;
    public float ofBrightness = 0.0F;
    public float ofAoLevel = 0.0F;
    public int ofAaLevel = 0;
    public int ofAfLevel = 1;
    public int ofClouds = 0;
    public float ofCloudsHeight = 0.0F;
    public int ofTrees = 0;
    public int ofGrass = 0;
    public int ofRain = 0;
    public int ofWater = 0;
    public int ofBetterGrass = 3;
    public int ofAutoSaveTicks = 4000;
    public boolean ofFastDebugInfo = false;
    public boolean ofWeather = true;
    public boolean ofSky = true;
    public boolean ofStars = true;
    public int ofChunkUpdates = 1;
    public boolean ofChunkUpdatesDynamic = true;
    public boolean ofFarView = false;
    public int ofTime = 0;
    public boolean ofClearWater = false;
    public int ofAnimatedWater = 0;
    public int ofAnimatedLava = 0;
    public boolean ofAnimatedFire = true;
    public boolean ofAnimatedPortal = true;
    public boolean ofAnimatedRedstone = true;
    public boolean ofAnimatedExplosion = true;
    public boolean ofAnimatedFlame = true;
    public boolean ofAnimatedSmoke = true;
    public KeyBinding ofKeyBindZoom;
    public List<KeyBinding> keyBindings;
    public int difficulty;
    public boolean hideHud;
    public boolean thirdPerson;
    public boolean debugHud;
    public String lastServer;
    public boolean field_1445;
    public boolean cinematicMode;
    public boolean alwaysFalse;
    public float field_1448;
    public float field_1449;
    public int guiScale;

    @Shadow
    public abstract float getFloatValue(Option var1);

    @Shadow
    public abstract boolean getBooleanValue(Option var1);

    @Inject(method = "<init>(Lnet/minecraft/client/Minecraft;Ljava/io/File;)V", at = @At("TAIL"))
    private void init(Minecraft file, File par2, CallbackInfo ci) {
        this.ofKeyBindZoom = new KeyBinding("Zoom", 46);
        this.keyBindings = new ArrayList<>();
        sharedInit();
        this.keyBindings.add(this.fogKey);
        this.keyBindings.add(this.ofKeyBindZoom);

        Config.setGameSettings((GameOptions) (Object) this);
    }

    @Inject(method = "<init>()V", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        this.keyBindings = new ArrayList<>();
        sharedInit();
    }

    private void sharedInit() {
        this.keyBindings.add(this.forwardKey);
        this.keyBindings.add(this.leftKey);
        this.keyBindings.add(this.backKey);
        this.keyBindings.add(this.rightKey);
        this.keyBindings.add(this.jumpKey);
        this.keyBindings.add(this.sneakKey);
        this.keyBindings.add(this.dropKey);
        this.keyBindings.add(this.inventoryKey);
        this.keyBindings.add(this.chatKey);
        this.keyBindings.add(this.fogKey);
        this.difficulty = 2;
        this.hideHud = false;
        this.thirdPerson = false;
        this.debugHud = false;
        this.lastServer = "";
        this.field_1445 = false;
        this.cinematicMode = false;
        this.alwaysFalse = false;
        this.field_1448 = 1.0F;
        this.field_1449 = 1.0F;
        this.guiScale = 0;
    }

    @Inject(method = "setFloatOption", at = @At("TAIL"))
    private void setFloatOptionOF(Option var1, float var2, CallbackInfo ci) {

        if (var1 == OptionOF.BRIGHTNESS) {
            this.ofBrightness = var2;
            this.updateWorldLightLevels();
        }

        if (var1 == OptionOF.CLOUD_HEIGHT) {
            this.ofCloudsHeight = var2;
        }

        if (var1 == OptionOF.AO_LEVEL) {
            this.ofAoLevel = var2;
            this.ao = this.ofAoLevel > 0.0F;
            this.client.worldRenderer.method_1537();
        }
    }

    private void updateWorldLightLevels() {
        if (this.client.gameRenderer != null) {
            ((ExGameRenderer)this.client.gameRenderer).updateWorldLightLevels();
        }

        if (this.client.worldRenderer != null) {
            this.client.worldRenderer.method_1537();
        }
    }

    private void updateWaterOpacity() {
        byte var1 = 3;
        if (this.ofClearWater) {
            var1 = 1;
        }

        Block.STILL_WATER.setLightOpacity(var1);
        Block.FLOWING_WATER.setLightOpacity(var1);
        if (this.client.world != null) {
            WorldSource var2 = this.client.world.worldSource;
            if (var2 != null) {
                for (int var3 = -512; var3 < 512; ++var3) {
                    for (int var4 = -512; var4 < 512; ++var4) {
                        if (var2.isChunkLoaded(var3, var4)) {
                            Chunk var5 = var2.getChunk(var3, var4);
                            if (var5 != null) {
                                byte[] var6 = var5.field_958.field_2103;

                                for (int var7 = 0; var7 < var6.length; ++var7) {
                                    var6[var7] = 0;
                                }

                                var5.generateHeightmap();
                            }
                        }
                    }
                }

                this.client.worldRenderer.method_1537();
            }
        }
    }

    @Inject(method = "setIntOption", at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/options/GameOptions;advancedOpengl:Z",
            ordinal = 0,
            shift = At.Shift.BEFORE),
            cancellable = true)
    private void setIntOptionOF_ADVANCED_OPENGL(Option var1, int var2, CallbackInfo ci) {
        if (!Config.isOcclusionAvailable()) {
            this.ofOcclusionFancy = false;
            this.advancedOpengl = false;
        } else if (!this.advancedOpengl) {
            this.advancedOpengl = true;
            this.ofOcclusionFancy = false;
        } else if (!this.ofOcclusionFancy) {
            this.ofOcclusionFancy = true;
        } else {
            this.ofOcclusionFancy = false;
            this.advancedOpengl = false;
        }

        ((ExWorldEventRenderer)this.client.worldRenderer).setAllRenderersVisible();

        ci.cancel();
    }

    @Inject(method = "setIntOption", at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/options/GameOptions;fpsLimit:I",
            ordinal = 0,
            shift = At.Shift.BEFORE),
            cancellable = true)
    private void setIntOptionOF_LIMIT_FRAMERATE(Option var1, int var2, CallbackInfo ci) {
        this.fpsLimit = (this.fpsLimit + var2) % 4;
        Display.setVSyncEnabled(this.fpsLimit == 3);

        ci.cancel();
    }

    @Inject(method = "setIntOption", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/options/GameOptions;saveOptions()V",
            shift = At.Shift.BEFORE))
    private void setIntOptionOF(Option var1, int var2, CallbackInfo ci) {
        if (var1 == OptionOF.FOG_FANCY) {
            if (!Config.isFancyFogAvailable()) {
                this.ofFogFancy = false;
            } else {
                this.ofFogFancy = !this.ofFogFancy;
            }
        }

        if (var1 == OptionOF.FOG_START) {
            this.ofFogStart += 0.2F;
            if (this.ofFogStart > 0.81F) {
                this.ofFogStart = 0.2F;
            }
        }

        if (var1 == OptionOF.MIPMAP_LEVEL) {
            ++this.ofMipmapLevel;
            if (this.ofMipmapLevel > 4) {
                this.ofMipmapLevel = 0;
            }

            this.client.textureManager.reloadTexturesFromTexturePack();
        }

        if (var1 == OptionOF.MIPMAP_TYPE) {
            this.ofMipmapLinear = !this.ofMipmapLinear;
            this.client.textureManager.reloadTexturesFromTexturePack();
        }

        if (var1 == OptionOF.LOAD_FAR) {
            this.ofLoadFar = !this.ofLoadFar;
            this.client.worldRenderer.method_1537();
        }

        if (var1 == OptionOF.PRELOADED_CHUNKS) {
            this.ofPreloadedChunks += 2;
            if (this.ofPreloadedChunks > 8) {
                this.ofPreloadedChunks = 0;
            }

            this.client.worldRenderer.method_1537();
        }

        if (var1 == OptionOF.SMOOTH_FPS) {
            this.ofSmoothFps = !this.ofSmoothFps;
        }

        if (var1 == OptionOF.SMOOTH_INPUT) {
            this.ofSmoothInput = !this.ofSmoothInput;
        }

        if (var1 == OptionOF.CLOUDS) {
            ++this.ofClouds;
            if (this.ofClouds > 3) {
                this.ofClouds = 0;
            }
        }

        if (var1 == OptionOF.TREES) {
            ++this.ofTrees;
            if (this.ofTrees > 2) {
                this.ofTrees = 0;
            }

            this.client.worldRenderer.method_1537();
        }

        if (var1 == OptionOF.GRASS) {
            ++this.ofGrass;
            if (this.ofGrass > 2) {
                this.ofGrass = 0;
            }

            BlockRenderer.field_67 = Config.isGrassFancy();
            this.client.worldRenderer.method_1537();
        }

        if (var1 == OptionOF.RAIN) {
            ++this.ofRain;
            if (this.ofRain > 3) {
                this.ofRain = 0;
            }
        }

        if (var1 == OptionOF.WATER) {
            ++this.ofWater;
            if (this.ofWater > 2) {
                this.ofWater = 0;
            }
        }

        if (var1 == OptionOF.ANIMATED_WATER) {
            ++this.ofAnimatedWater;
            if (this.ofAnimatedWater > 2) {
                this.ofAnimatedWater = 0;
            }

            this.client.textureManager.reloadTexturesFromTexturePack();
        }

        if (var1 == OptionOF.ANIMATED_LAVA) {
            ++this.ofAnimatedLava;
            if (this.ofAnimatedLava > 2) {
                this.ofAnimatedLava = 0;
            }

            this.client.textureManager.reloadTexturesFromTexturePack();
        }

        if (var1 == OptionOF.ANIMATED_FIRE) {
            this.ofAnimatedFire = !this.ofAnimatedFire;
            this.client.textureManager.reloadTexturesFromTexturePack();
        }

        if (var1 == OptionOF.ANIMATED_PORTAL) {
            this.ofAnimatedPortal = !this.ofAnimatedPortal;
            this.client.textureManager.reloadTexturesFromTexturePack();
        }

        if (var1 == OptionOF.ANIMATED_REDSTONE) {
            this.ofAnimatedRedstone = !this.ofAnimatedRedstone;
        }

        if (var1 == OptionOF.ANIMATED_EXPLOSION) {
            this.ofAnimatedExplosion = !this.ofAnimatedExplosion;
        }

        if (var1 == OptionOF.ANIMATED_FLAME) {
            this.ofAnimatedFlame = !this.ofAnimatedFlame;
        }

        if (var1 == OptionOF.ANIMATED_SMOKE) {
            this.ofAnimatedSmoke = !this.ofAnimatedSmoke;
        }

        if (var1 == OptionOF.FAST_DEBUG_INFO) {
            this.ofFastDebugInfo = !this.ofFastDebugInfo;
        }

        if (var1 == OptionOF.AUTOSAVE_TICKS) {
            this.ofAutoSaveTicks *= 10;
            if (this.ofAutoSaveTicks > 40000) {
                this.ofAutoSaveTicks = 40;
            }
        }

        if (var1 == OptionOF.BETTER_GRASS) {
            ++this.ofBetterGrass;
            if (this.ofBetterGrass > 3) {
                this.ofBetterGrass = 1;
            }

            this.client.worldRenderer.method_1537();
        }

        if (var1 == OptionOF.WEATHER) {
            this.ofWeather = !this.ofWeather;
        }

        if (var1 == OptionOF.SKY) {
            this.ofSky = !this.ofSky;
        }

        if (var1 == OptionOF.STARS) {
            this.ofStars = !this.ofStars;
        }

        if (var1 == OptionOF.CHUNK_UPDATES) {
            ++this.ofChunkUpdates;
            if (this.ofChunkUpdates > 5) {
                this.ofChunkUpdates = 1;
            }
        }

        if (var1 == OptionOF.CHUNK_UPDATES_DYNAMIC) {
            this.ofChunkUpdatesDynamic = !this.ofChunkUpdatesDynamic;
        }

        if (var1 == OptionOF.FAR_VIEW) {
            this.ofFarView = !this.ofFarView;
            this.client.worldRenderer.method_1537();
        }

        if (var1 == OptionOF.TIME) {
            ++this.ofTime;
            if (this.ofTime > 2) {
                this.ofTime = 0;
            }
        }

        if (var1 == OptionOF.CLEAR_WATER) {
            this.ofClearWater = !this.ofClearWater;
            this.updateWaterOpacity();
        }

        if (var1 == OptionOF.AA_LEVEL) {
            int[] var3 = new int[]{0, 2, 4, 6, 8, 12, 16};
            boolean var4 = false;

            for (int var5 = 0; var5 < var3.length - 1; ++var5) {
                if (this.ofAaLevel == var3[var5]) {
                    this.ofAaLevel = var3[var5 + 1];
                    var4 = true;
                    break;
                }
            }

            if (!var4) {
                this.ofAaLevel = 0;
            }
        }

        if (var1 == OptionOF.AF_LEVEL) {
            this.ofAfLevel *= 2;
            if (this.ofAfLevel > 16) {
                this.ofAfLevel = 1;
            }

            this.ofAfLevel = Config.limit(this.ofAfLevel, 1, 16);
            this.client.textureManager.reloadTexturesFromTexturePack();
        }
    }

    @Inject(method = "getFloatValue", at = @At("HEAD"),
            cancellable = true)
    private void getFloatValueOF(Option var1, CallbackInfoReturnable<Float> cir) {
        if (var1 == OptionOF.BRIGHTNESS) {
            cir.setReturnValue(this.ofBrightness);
            cir.cancel();
        } else if (var1 == OptionOF.CLOUD_HEIGHT) {
            cir.setReturnValue(this.ofCloudsHeight);
            cir.cancel();
        } else if (var1 == OptionOF.AO_LEVEL) {
            cir.setReturnValue(this.ofAoLevel);
            cir.cancel();
        }
    }

    @Overwrite
    public String getTranslatedValue(Option var1) {
        TranslationStorage var2 = TranslationStorage.getInstance();
        String var3 = var2.translate(var1.getTranslationKey());
        if (var3 == null) {
            var3 = var1.getTranslationKey();
        }

        String var4 = var3 + ": ";
        if (var1.isSlider()) {
            float var6 = this.getFloatValue(var1);
            if (var1 == Option.SENSITIVITY) {
                if (var6 == 0.0F)
                    return var4 + var2.translate("options.sensitivity.min");
                if (var6 == 1.0F)
                    return var4 + var2.translate("options.sensitivity.max");
                return var4 + (int) (var6 * 200.0F) + "%";
            }
            if (var6 == 0.0F)
                return var4 + var2.translate("options.off");
            return var4 + (int) (var6 * 100.0F) + "%";
        } else if (var1 == Option.ADVANCED_OPENGL) {
            if (!this.advancedOpengl)
                return var4 + "OFF";
            return this.ofOcclusionFancy ? var4 + "Fancy" : var4 + "Fast";
        } else if (var1.isToggle()) {
            boolean var5 = this.getBooleanValue(var1);
            if (var5)
                return var4 + var2.translate("options.on");
            return var4 + var2.translate("options.off");
        } else if (var1 == Option.RENDER_DISTANCE) {
            return var4 + var2.translate(renderDistanceTranslationKeys[this.viewDistance]);
        } else if (var1 == Option.GLOBAL_DIFFICULTY) {
            return var4 + var2.translate(difficultyTranslationKeys[this.difficulty]);
        } else if (var1 == Option.GUI_SCALE) {
            return var4 + var2.translate(guiScaleTranslationKeys[this.guiScale]);
        } else if (var1 == Option.LIMIT_FRAMERATE) {
            if (this.fpsLimit == 3)
                return var4 + "VSync";
            return var4 + Internationalization.translate(performanceTranslationKeys[this.fpsLimit]);
        } else if (var1 == OptionOF.FOG_FANCY) {
            return this.ofFogFancy ? var4 + "Fancy" : var4 + "Fast";
        } else if (var1 == OptionOF.FOG_START) {
            return var4 + this.ofFogStart;
        } else if (var1 == OptionOF.MIPMAP_LEVEL) {
            return var4 + this.ofMipmapLevel;
        } else if (var1 == OptionOF.MIPMAP_TYPE) {
            return this.ofMipmapLinear ? var4 + "Linear" : var4 + "Nearest";
        } else if (var1 == OptionOF.LOAD_FAR) {
            return this.ofLoadFar ? var4 + "ON" : var4 + "OFF";
        } else if (var1 == OptionOF.PRELOADED_CHUNKS) {
            if (this.ofPreloadedChunks == 0)
                return var4 + "OFF";
            return var4 + this.ofPreloadedChunks;
        } else if (var1 == OptionOF.SMOOTH_FPS) {
            return this.ofSmoothFps ? var4 + "ON" : var4 + "OFF";
        } else if (var1 == OptionOF.SMOOTH_INPUT) {
            return this.ofSmoothInput ? var4 + "ON" : var4 + "OFF";
        } else if (var1 == OptionOF.CLOUDS) {
            switch (this.ofClouds) {
                case 1:
                    return var4 + "Fast";
                case 2:
                    return var4 + "Fancy";
                case 3:
                    return var4 + "OFF";
                default:
                    return var4 + "Default";
            }
        } else if (var1 == OptionOF.TREES) {
            switch (this.ofTrees) {
                case 1:
                    return var4 + "Fast";
                case 2:
                    return var4 + "Fancy";
                default:
                    return var4 + "Default";
            }
        } else if (var1 == OptionOF.GRASS) {
            switch (this.ofGrass) {
                case 1:
                    return var4 + "Fast";
                case 2:
                    return var4 + "Fancy";
                default:
                    return var4 + "Default";
            }
        } else if (var1 == OptionOF.RAIN) {
            switch (this.ofRain) {
                case 1:
                    return var4 + "Fast";
                case 2:
                    return var4 + "Fancy";
                case 3:
                    return var4 + "OFF";
                default:
                    return var4 + "Default";
            }
        } else if (var1 == OptionOF.WATER) {
            switch (this.ofWater) {
                case 1:
                    return var4 + "Fast";
                case 2:
                    return var4 + "Fancy";
                case 3:
                    return var4 + "OFF";
                default:
                    return var4 + "Default";
            }
        } else if (var1 == OptionOF.ANIMATED_WATER) {
            switch (this.ofAnimatedWater) {
                case 1:
                    return var4 + "Dynamic";
                case 2:
                    return var4 + "OFF";
                default:
                    return var4 + "ON";
            }
        } else if (var1 == OptionOF.ANIMATED_LAVA) {
            switch (this.ofAnimatedLava) {
                case 1:
                    return var4 + "Dynamic";
                case 2:
                    return var4 + "OFF";
                default:
                    return var4 + "ON";
            }
        } else if (var1 == OptionOF.ANIMATED_FIRE) {
            return this.ofAnimatedFire ? var4 + "ON" : var4 + "OFF";
        } else if (var1 == OptionOF.ANIMATED_PORTAL) {
            return this.ofAnimatedPortal ? var4 + "ON" : var4 + "OFF";
        } else if (var1 == OptionOF.ANIMATED_REDSTONE) {
            return this.ofAnimatedRedstone ? var4 + "ON" : var4 + "OFF";
        } else if (var1 == OptionOF.ANIMATED_EXPLOSION) {
            return this.ofAnimatedExplosion ? var4 + "ON" : var4 + "OFF";
        } else if (var1 == OptionOF.ANIMATED_FLAME) {
            return this.ofAnimatedFlame ? var4 + "ON" : var4 + "OFF";
        } else if (var1 == OptionOF.ANIMATED_SMOKE) {
            return this.ofAnimatedSmoke ? var4 + "ON" : var4 + "OFF";
        } else if (var1 == OptionOF.FAST_DEBUG_INFO) {
            return this.ofFastDebugInfo ? var4 + "ON" : var4 + "OFF";
        } else if (var1 == OptionOF.AUTOSAVE_TICKS) {
            if (this.ofAutoSaveTicks <= 40)
                return var4 + "Default (2s)";
            if (this.ofAutoSaveTicks <= 400)
                return var4 + "20s";
            if (this.ofAutoSaveTicks <= 4000)
                return var4 + "3min";
            return var4 + "30min";
        } else if (var1 == OptionOF.BETTER_GRASS) {
            switch (this.ofBetterGrass) {
                case 1:
                    return var4 + "Fast";
                case 2:
                    return var4 + "Fancy";
                default:
                    return var4 + "OFF";
            }
        } else {
            if (var1 == OptionOF.WEATHER)
                return this.ofWeather ? var4 + "ON" : var4 + "OFF";
            if (var1 == OptionOF.SKY)
                return this.ofSky ? var4 + "ON" : var4 + "OFF";
            if (var1 == OptionOF.STARS)
                return this.ofStars ? var4 + "ON" : var4 + "OFF";
            if (var1 == OptionOF.CHUNK_UPDATES)
                return var4 + this.ofChunkUpdates;
            if (var1 == OptionOF.CHUNK_UPDATES_DYNAMIC)
                return this.ofChunkUpdatesDynamic ? var4 + "ON" : var4 + "OFF";
            if (var1 == OptionOF.FAR_VIEW)
                return this.ofFarView ? var4 + "ON" : var4 + "OFF";
            if (var1 == OptionOF.TIME) {
                if (this.ofTime == 1)
                    return var4 + "Day Only";
                if (this.ofTime == 2)
                    return var4 + "Night Only";
                return var4 + "Default";
            }
            if (var1 == OptionOF.CLEAR_WATER)
                return this.ofClearWater ? var4 + "ON" : var4 + "OFF";
            if (var1 == OptionOF.AA_LEVEL) {
                if (this.ofAaLevel == 0)
                    return var4 + "OFF";
                return var4 + this.ofAaLevel;
            }
            if (var1 == OptionOF.AF_LEVEL) {
                if (this.ofAfLevel == 1)
                    return var4 + "OFF";
                return var4 + this.ofAfLevel;
            }
            if (var1 == Option.GRAPHICS_QUALITY) {
                if (this.fancyGraphics)
                    return var4 + var2.translate("options.graphics.fancy");
                return var4 + var2.translate("options.graphics.fast");
            }
            return var4;
        }
    }

    @Inject(method = "load", at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/options/GameOptions;fpsLimit:I",
            shift = At.Shift.AFTER))
    private void load_fpsLimit(CallbackInfo ci) {
        Display.setVSyncEnabled(this.fpsLimit == 3);
    }

    @Inject(method = "load", at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/options/GameOptions;ao:Z",
            shift = At.Shift.AFTER))
    private void load_ao(CallbackInfo ci) {
        if (this.ao) {
            this.ofAoLevel = 1.0F;
        } else {
            this.ofAoLevel = 0.0F;
        }
    }

    @Inject(method = "load", at = @At(
            value = "INVOKE",
            target = "Ljava/lang/String;equals(Ljava/lang/Object;)Z",
            shift = At.Shift.BEFORE,
            ordinal = 0),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void loadOF(CallbackInfo ci, BufferedReader var1, String var2, String[] var3) {
        if (var3[0].equals("ofFogFancy") && var3.length >= 2) {
            this.ofFogFancy = var3[1].equals("true");
        }

        if (var3[0].equals("ofFogStart") && var3.length >= 2) {
            this.ofFogStart = Float.parseFloat(var3[1]);
            if (this.ofFogStart < 0.2F) {
                this.ofFogStart = 0.2F;
            }

            if (this.ofFogStart > 0.81F) {
                this.ofFogStart = 0.8F;
            }
        }

        if (var3[0].equals("ofMipmapLevel") && var3.length >= 2) {
            this.ofMipmapLevel = Integer.parseInt(var3[1]);
            if (this.ofMipmapLevel < 0) {
                this.ofMipmapLevel = 0;
            }

            if (this.ofMipmapLevel > 4) {
                this.ofMipmapLevel = 4;
            }
        }

        if (var3[0].equals("ofMipmapLinear") && var3.length >= 2) {
            this.ofMipmapLinear = Boolean.parseBoolean(var3[1]);
        }

        if (var3[0].equals("ofLoadFar") && var3.length >= 2) {
            this.ofLoadFar = Boolean.parseBoolean(var3[1]);
        }

        if (var3[0].equals("ofPreloadedChunks") && var3.length >= 2) {
            this.ofPreloadedChunks = Integer.parseInt(var3[1]);
            if (this.ofPreloadedChunks < 0) {
                this.ofPreloadedChunks = 0;
            }

            if (this.ofPreloadedChunks > 8) {
                this.ofPreloadedChunks = 8;
            }
        }

        if (var3[0].equals("ofOcclusionFancy") && var3.length >= 2) {
            this.ofOcclusionFancy = Boolean.parseBoolean(var3[1]);
        }

        if (var3[0].equals("ofSmoothFps") && var3.length >= 2) {
            this.ofSmoothFps = Boolean.parseBoolean(var3[1]);
        }

        if (var3[0].equals("ofSmoothInput") && var3.length >= 2) {
            this.ofSmoothInput = Boolean.parseBoolean(var3[1]);
        }

        if (var3[0].equals("ofBrightness") && var3.length >= 2) {
            this.ofBrightness = Float.parseFloat(var3[1]);
            this.ofBrightness = Config.limit(this.ofBrightness, 0.0F, 1.0F);
            this.updateWorldLightLevels();
        }

        if (var3[0].equals("ofAoLevel") && var3.length >= 2) {
            this.ofAoLevel = Float.parseFloat(var3[1]);
            this.ofAoLevel = Config.limit(this.ofAoLevel, 0.0F, 1.0F);
            this.ao = this.ofAoLevel > 0.0F;
        }

        if (var3[0].equals("ofClouds") && var3.length >= 2) {
            this.ofClouds = Integer.parseInt(var3[1]);
            this.ofClouds = Config.limit(this.ofClouds, 0, 3);
        }

        if (var3[0].equals("ofCloudsHeight") && var3.length >= 2) {
            this.ofCloudsHeight = Float.parseFloat(var3[1]);
            this.ofCloudsHeight = Config.limit(this.ofCloudsHeight, 0.0F, 1.0F);
        }

        if (var3[0].equals("ofTrees") && var3.length >= 2) {
            this.ofTrees = Integer.parseInt(var3[1]);
            this.ofTrees = Config.limit(this.ofTrees, 0, 2);
        }

        if (var3[0].equals("ofGrass") && var3.length >= 2) {
            this.ofGrass = Integer.parseInt(var3[1]);
            this.ofGrass = Config.limit(this.ofGrass, 0, 2);
        }

        if (var3[0].equals("ofRain") && var3.length >= 2) {
            this.ofRain = Integer.parseInt(var3[1]);
            this.ofRain = Config.limit(this.ofRain, 0, 3);
        }

        if (var3[0].equals("ofWater") && var3.length >= 2) {
            this.ofWater = Integer.parseInt(var3[1]);
            this.ofWater = Config.limit(this.ofWater, 0, 3);
        }

        if (var3[0].equals("ofAnimatedWater") && var3.length >= 2) {
            this.ofAnimatedWater = Integer.parseInt(var3[1]);
            this.ofAnimatedWater = Config.limit(this.ofAnimatedWater, 0, 2);
        }

        if (var3[0].equals("ofAnimatedLava") && var3.length >= 2) {
            this.ofAnimatedLava = Integer.parseInt(var3[1]);
            this.ofAnimatedLava = Config.limit(this.ofAnimatedLava, 0, 2);
        }

        if (var3[0].equals("ofAnimatedFire") && var3.length >= 2) {
            this.ofAnimatedFire = Boolean.parseBoolean(var3[1]);
        }

        if (var3[0].equals("ofAnimatedPortal") && var3.length >= 2) {
            this.ofAnimatedPortal = Boolean.parseBoolean(var3[1]);
        }

        if (var3[0].equals("ofAnimatedRedstone") && var3.length >= 2) {
            this.ofAnimatedRedstone = Boolean.parseBoolean(var3[1]);
        }

        if (var3[0].equals("ofAnimatedExplosion") && var3.length >= 2) {
            this.ofAnimatedExplosion = Boolean.parseBoolean(var3[1]);
        }

        if (var3[0].equals("ofAnimatedFlame") && var3.length >= 2) {
            this.ofAnimatedFlame = Boolean.parseBoolean(var3[1]);
        }

        if (var3[0].equals("ofAnimatedSmoke") && var3.length >= 2) {
            this.ofAnimatedSmoke = Boolean.parseBoolean(var3[1]);
        }

        if (var3[0].equals("ofFastDebugInfo") && var3.length >= 2) {
            this.ofFastDebugInfo = Boolean.parseBoolean(var3[1]);
        }

        if (var3[0].equals("ofAutoSaveTicks") && var3.length >= 2) {
            this.ofAutoSaveTicks = Integer.parseInt(var3[1]);
            this.ofAutoSaveTicks = Config.limit(this.ofAutoSaveTicks, 40, 40000);
        }

        if (var3[0].equals("ofBetterGrass") && var3.length >= 2) {
            this.ofBetterGrass = Integer.parseInt(var3[1]);
            this.ofBetterGrass = Config.limit(this.ofBetterGrass, 1, 3);
        }

        if (var3[0].equals("ofWeather") && var3.length >= 2) {
            this.ofWeather = Boolean.parseBoolean(var3[1]);
        }

        if (var3[0].equals("ofSky") && var3.length >= 2) {
            this.ofSky = Boolean.parseBoolean(var3[1]);
        }

        if (var3[0].equals("ofStars") && var3.length >= 2) {
            this.ofStars = Boolean.parseBoolean(var3[1]);
        }

        if (var3[0].equals("ofChunkUpdates") && var3.length >= 2) {
            this.ofChunkUpdates = Integer.parseInt(var3[1]);
            this.ofChunkUpdates = Config.limit(this.ofChunkUpdates, 1, 5);
        }

        if (var3[0].equals("ofChunkUpdatesDynamic") && var3.length >= 2) {
            this.ofChunkUpdatesDynamic = Boolean.parseBoolean(var3[1]);
        }

        if (var3[0].equals("ofFarView") && var3.length >= 2) {
            this.ofFarView = Boolean.parseBoolean(var3[1]);
        }

        if (var3[0].equals("ofTime") && var3.length >= 2) {
            this.ofTime = Integer.parseInt(var3[1]);
            this.ofTime = Config.limit(this.ofTime, 0, 2);
        }

        if (var3[0].equals("ofClearWater") && var3.length >= 2) {
            this.ofClearWater = Boolean.parseBoolean(var3[1]);
            this.updateWaterOpacity();
        }

        if (var3[0].equals("ofAaLevel") && var3.length >= 2) {
            this.ofAaLevel = Integer.parseInt(var3[1]);
            this.ofAaLevel = Config.limit(this.ofAaLevel, 0, 16);
        }

        if (var3[0].equals("ofAfLevel") && var3.length >= 2) {
            this.ofAfLevel = Integer.parseInt(var3[1]);
            this.ofAfLevel = Config.limit(this.ofAfLevel, 1, 16);
        }
    }

    @Inject(method = "saveOptions", at = @At(
            value = "INVOKE",
            target = "Ljava/io/PrintWriter;close()V",
            shift = At.Shift.BEFORE),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void saveOptionsOF(CallbackInfo ci, PrintWriter var1, int var2) {
        var1.println("ofFogFancy:" + this.ofFogFancy);
        var1.println("ofFogStart:" + this.ofFogStart);
        var1.println("ofMipmapLevel:" + this.ofMipmapLevel);
        var1.println("ofMipmapLinear:" + this.ofMipmapLinear);
        var1.println("ofLoadFar:" + this.ofLoadFar);
        var1.println("ofPreloadedChunks:" + this.ofPreloadedChunks);
        var1.println("ofOcclusionFancy:" + this.ofOcclusionFancy);
        var1.println("ofSmoothFps:" + this.ofSmoothFps);
        var1.println("ofSmoothInput:" + this.ofSmoothInput);
        var1.println("ofBrightness:" + this.ofBrightness);
        var1.println("ofAoLevel:" + this.ofAoLevel);
        var1.println("ofClouds:" + this.ofClouds);
        var1.println("ofCloudsHeight:" + this.ofCloudsHeight);
        var1.println("ofTrees:" + this.ofTrees);
        var1.println("ofGrass:" + this.ofGrass);
        var1.println("ofRain:" + this.ofRain);
        var1.println("ofWater:" + this.ofWater);
        var1.println("ofAnimatedWater:" + this.ofAnimatedWater);
        var1.println("ofAnimatedLava:" + this.ofAnimatedLava);
        var1.println("ofAnimatedFire:" + this.ofAnimatedFire);
        var1.println("ofAnimatedPortal:" + this.ofAnimatedPortal);
        var1.println("ofAnimatedRedstone:" + this.ofAnimatedRedstone);
        var1.println("ofAnimatedExplosion:" + this.ofAnimatedExplosion);
        var1.println("ofAnimatedFlame:" + this.ofAnimatedFlame);
        var1.println("ofAnimatedSmoke:" + this.ofAnimatedSmoke);
        var1.println("ofFastDebugInfo:" + this.ofFastDebugInfo);
        var1.println("ofAutoSaveTicks:" + this.ofAutoSaveTicks);
        var1.println("ofBetterGrass:" + this.ofBetterGrass);
        var1.println("ofWeather:" + this.ofWeather);
        var1.println("ofSky:" + this.ofSky);
        var1.println("ofStars:" + this.ofStars);
        var1.println("ofChunkUpdates:" + this.ofChunkUpdates);
        var1.println("ofChunkUpdatesDynamic:" + this.ofChunkUpdatesDynamic);
        var1.println("ofFarView:" + this.ofFarView);
        var1.println("ofTime:" + this.ofTime);
        var1.println("ofClearWater:" + this.ofClearWater);
        var1.println("ofAaLevel:" + this.ofAaLevel);
        var1.println("ofAfLevel:" + this.ofAfLevel);
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
    public boolean ofMipmapLinear() {
        return ofMipmapLinear;
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
    public boolean ofOcclusionFancy() {
        return ofOcclusionFancy;
    }

    @Override
    public boolean ofSmoothFps() {
        return ofSmoothFps;
    }

    @Override
    public boolean ofSmoothInput() {
        return ofSmoothInput;
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
    public float ofCloudsHeight() {
        return ofCloudsHeight;
    }

    @Override
    public int ofTrees() {
        return ofTrees;
    }

    @Override
    public int ofGrass() {
        return ofGrass;
    }

    @Override
    public int ofRain() {
        return ofRain;
    }

    @Override
    public int ofWater() {
        return ofWater;
    }

    @Override
    public int ofBetterGrass() {
        return ofBetterGrass;
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
    public int ofChunkUpdates() {
        return ofChunkUpdates;
    }

    @Override
    public boolean ofChunkUpdatesDynamic() {
        return ofChunkUpdatesDynamic;
    }

    @Override
    public boolean ofFarView() {
        return ofFarView;
    }

    @Override
    public int ofTime() {
        return ofTime;
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
    public KeyBinding ofKeyBindZoom() {
        return ofKeyBindZoom;
    }
}
