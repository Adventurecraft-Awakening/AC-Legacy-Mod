package dev.adventurecraft.awakening.mixin.client.options;

import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.client.options.Config;
import dev.adventurecraft.awakening.client.options.ConnectedGrassOption;
import dev.adventurecraft.awakening.client.options.OptionOF;
import dev.adventurecraft.awakening.extension.client.options.ExGameOptions;
import dev.adventurecraft.awakening.extension.client.render.ExWorldEventRenderer;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.locale.I18n;
import net.minecraft.util.Language;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.tile.Tile;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.BufferedReader;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mixin(Options.class)
public abstract class MixinGameOptions implements ExGameOptions {

    private static final int MAX_CHAT_BUFFER_LIMIT = 10000;
    private static final int MAX_PARTICLE_LIMIT = 1024 * 32;

    @Final
    @Shadow
    private static String[] RENDER_DISTANCES;
    @Final
    @Shadow
    private static String[] DIFFICULTIES;
    @Final
    @Shadow
    private static String[] GUI_SCALES;
    @Final
    @Shadow
    private static String[] PERFORMANCE_OPTIONS;

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
    public KeyMapping keyUp;
    @Shadow
    public KeyMapping keyLeft;
    @Shadow
    public KeyMapping keyDown;
    @Shadow
    public KeyMapping keyRight;
    @Shadow
    public KeyMapping keyJump;
    @Shadow
    public KeyMapping keyInventory;
    @Shadow
    public KeyMapping keyDrop;
    @Shadow
    public KeyMapping keyChat;
    @Shadow
    public KeyMapping keyFog;
    @Shadow
    public KeyMapping keySneak;
    @Shadow
    protected Minecraft minecraft;
    @Shadow
    public int difficulty;
    @Shadow
    public int guiScale;

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
    public ConnectedGrassOption ofConnectedGrass = ConnectedGrassOption.OFF;
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
    public KeyMapping ofKeyBindZoom;
    public List<KeyMapping> keyBindings;
    public boolean autoFarClip = false;
    public boolean grass3d = true;
    public int chatMessageBufferLimit = 100;
    public int particleLimit = 1024 * 4;
    public boolean allowJavaInScript = false;

    @Shadow
    public abstract float getProgressValue(Option option);

    @Shadow
    public abstract boolean getBooleanValue(Option option);

    @Inject(method = "<init>(Lnet/minecraft/client/Minecraft;Ljava/io/File;)V", at = @At("TAIL"))
    private void init(Minecraft file, File par2, CallbackInfo ci) {
        this.sharedInit();
    }

    @Inject(method = "<init>()V", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        this.sharedInit();
    }

    private void sharedInit() {
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

    @Inject(method = "set", at = @At("TAIL"))
    private void setFloatOptionOF(Option option, float value, CallbackInfo ci) {
        if (option == OptionOF.BRIGHTNESS) {
            this.ofBrightness = value;
            this.updateWorldLightLevels();
        } else if (option == OptionOF.CLOUD_HEIGHT) {
            this.ofCloudsHeight = value;
        } else if (option == OptionOF.AO_LEVEL) {
            this.ofAoLevel = value;
            this.ao = this.ofAoLevel > 0.0F;
            this.minecraft.levelRenderer.allChanged();
        } else if (option == OptionOF.CHAT_MESSAGE_BUFFER_LIMIT) {
            this.chatMessageBufferLimit = (int) (value * (MAX_CHAT_BUFFER_LIMIT - 1)) + 1;
        } else if (option == OptionOF.PARTICLE_LIMIT) {
            this.particleLimit = (int) (value * MAX_PARTICLE_LIMIT);
        }
    }

    private void updateWorldLightLevels() {
        if (this.minecraft.level != null) {
            ((ExWorld) this.minecraft.level).loadBrightness();
            ((ExWorldEventRenderer) this.minecraft.levelRenderer).updateAllTheRenderers();
        }
    }

    private void updateWaterOpacity() {
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
            ordinal = 0))
    private void setIntOptionOF_ADVANCED_OPENGL(Options instance, boolean value) {
        if (!GLContext.getCapabilities().GL_ARB_occlusion_query) {
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

        ((ExWorldEventRenderer) this.minecraft.levelRenderer).setAllRenderersVisible();
    }

    @Redirect(
        method = "toggle",
        at = @At(
            value = "FIELD",
            opcode = Opcodes.PUTFIELD,
            target = "Lnet/minecraft/client/Options;fpsLimit:I",
            ordinal = 0))
    private void setIntOptionPutFpsLimit(Options instance, int value, @Local(argsOnly = true) int i) {
        this.fpsLimit = (this.fpsLimit + i) % 4;
        Display.setVSyncEnabled(this.fpsLimit == 3);
    }

    @Inject(
        method = "toggle",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Options;save()V",
            shift = At.Shift.BEFORE))
    private void setIntOptionOF(Option option, int value, CallbackInfo ci) {
        if (option == OptionOF.FOG_FANCY) {
            if (!GLContext.getCapabilities().GL_NV_fog_distance) {
                this.ofFogFancy = false;
            } else {
                this.ofFogFancy = !this.ofFogFancy;
            }
        }

        if (option == OptionOF.FOG_START) {
            this.ofFogStart += 0.2F;
            if (this.ofFogStart > 0.81F) {
                this.ofFogStart = 0.2F;
            }
        }

        if (option == OptionOF.MIPMAP_LEVEL) {
            ++this.ofMipmapLevel;
            if (this.ofMipmapLevel > 4) {
                this.ofMipmapLevel = 0;
            }

            this.minecraft.textures.reloadAll();
        }

        if (option == OptionOF.MIPMAP_TYPE) {
            this.ofMipmapLinear = !this.ofMipmapLinear;
            this.minecraft.textures.reloadAll();
        }

        if (option == OptionOF.LOAD_FAR) {
            this.ofLoadFar = !this.ofLoadFar;
            this.minecraft.levelRenderer.allChanged();
        }

        if (option == OptionOF.PRELOADED_CHUNKS) {
            this.ofPreloadedChunks += 2;
            if (this.ofPreloadedChunks > 8) {
                this.ofPreloadedChunks = 0;
            }

            this.minecraft.levelRenderer.allChanged();
        }

        if (option == OptionOF.SMOOTH_FPS) {
            this.ofSmoothFps = !this.ofSmoothFps;
        }

        if (option == OptionOF.SMOOTH_INPUT) {
            this.ofSmoothInput = !this.ofSmoothInput;
        }

        if (option == OptionOF.CLOUDS) {
            ++this.ofClouds;
            if (this.ofClouds > 3) {
                this.ofClouds = 0;
            }
        }

        if (option == OptionOF.LEAVES) {
            ++this.ofTrees;
            if (this.ofTrees > 2) {
                this.ofTrees = 0;
            }

            this.minecraft.levelRenderer.allChanged();
        }

        if (option == OptionOF.GRASS) {
            ++this.ofGrass;
            if (this.ofGrass > 2) {
                this.ofGrass = 0;
            }

            this.minecraft.levelRenderer.allChanged();
        }

        if (option == OptionOF.RAIN) {
            ++this.ofRain;
            if (this.ofRain > 3) {
                this.ofRain = 0;
            }
        }

        if (option == OptionOF.WATER) {
            ++this.ofWater;
            if (this.ofWater > 2) {
                this.ofWater = 0;
            }
        }

        if (option == OptionOF.ANIMATED_WATER) {
            ++this.ofAnimatedWater;
            if (this.ofAnimatedWater > 2) {
                this.ofAnimatedWater = 0;
            }

            this.minecraft.textures.reloadAll();
        }

        if (option == OptionOF.ANIMATED_LAVA) {
            ++this.ofAnimatedLava;
            if (this.ofAnimatedLava > 2) {
                this.ofAnimatedLava = 0;
            }

            this.minecraft.textures.reloadAll();
        }

        if (option == OptionOF.ANIMATED_FIRE) {
            this.ofAnimatedFire = !this.ofAnimatedFire;
            this.minecraft.textures.reloadAll();
        }

        if (option == OptionOF.ANIMATED_PORTAL) {
            this.ofAnimatedPortal = !this.ofAnimatedPortal;
            this.minecraft.textures.reloadAll();
        }

        if (option == OptionOF.ANIMATED_REDSTONE) {
            this.ofAnimatedRedstone = !this.ofAnimatedRedstone;
        }

        if (option == OptionOF.ANIMATED_EXPLOSION) {
            this.ofAnimatedExplosion = !this.ofAnimatedExplosion;
        }

        if (option == OptionOF.ANIMATED_FLAME) {
            this.ofAnimatedFlame = !this.ofAnimatedFlame;
        }

        if (option == OptionOF.ANIMATED_SMOKE) {
            this.ofAnimatedSmoke = !this.ofAnimatedSmoke;
        }

        if (option == OptionOF.FAST_DEBUG_INFO) {
            this.ofFastDebugInfo = !this.ofFastDebugInfo;
        }

        if (option == OptionOF.AUTOSAVE_TICKS) {
            this.ofAutoSaveTicks *= 10;
            if (this.ofAutoSaveTicks > 40000) {
                this.ofAutoSaveTicks = 40;
            }
        }

        if (option == OptionOF.CONNECTED_GRASS) {
            ofConnectedGrass = switch (this.ofConnectedGrass) {
                case OFF -> ConnectedGrassOption.FAST;
                case FAST -> ConnectedGrassOption.FANCY;
                case FANCY -> ConnectedGrassOption.OFF;
            };
            this.minecraft.levelRenderer.allChanged();
        }

        if (option == OptionOF.WEATHER) {
            this.ofWeather = !this.ofWeather;
        }

        if (option == OptionOF.SKY) {
            this.ofSky = !this.ofSky;
        }

        if (option == OptionOF.STARS) {
            this.ofStars = !this.ofStars;
        }

        if (option == OptionOF.CHUNK_UPDATES) {
            ++this.ofChunkUpdates;
            if (this.ofChunkUpdates > 5) {
                this.ofChunkUpdates = 1;
            }
        }

        if (option == OptionOF.CHUNK_UPDATES_DYNAMIC) {
            this.ofChunkUpdatesDynamic = !this.ofChunkUpdatesDynamic;
        }

        if (option == OptionOF.FAR_VIEW) {
            this.ofFarView = !this.ofFarView;
            this.minecraft.levelRenderer.allChanged();
        }

        if (option == OptionOF.TIME) {
            ++this.ofTime;
            if (this.ofTime > 2) {
                this.ofTime = 0;
            }
        }

        if (option == OptionOF.CLEAR_WATER) {
            this.ofClearWater = !this.ofClearWater;
            this.updateWaterOpacity();
        }

        if (option == OptionOF.AA_LEVEL) {
            int[] var3 = new int[]{0, 2, 4, 6, 8, 12, 16};
            boolean var4 = false;

            for (int i = 0; i < var3.length - 1; ++i) {
                if (this.ofAaLevel == var3[i]) {
                    this.ofAaLevel = var3[i + 1];
                    var4 = true;
                    break;
                }
            }

            if (!var4) {
                this.ofAaLevel = 0;
            }
        }

        if (option == OptionOF.AF_LEVEL) {
            this.ofAfLevel *= 2;
            if (this.ofAfLevel > 16) {
                this.ofAfLevel = 1;
            }

            this.ofAfLevel = Config.limit(this.ofAfLevel, 1, 16);
            this.minecraft.textures.reloadAll();
        }

        if (option == OptionOF.AUTO_FAR_CLIP) {
            this.autoFarClip = !this.autoFarClip;
        }

        if (option == OptionOF.GRASS_3D) {
            this.grass3d = !this.grass3d;
            this.minecraft.levelRenderer.allChanged();
        }
    }

    @Inject(method = "getProgressValue", at = @At("HEAD"), cancellable = true)
    private void getFloatValueOF(Option option, CallbackInfoReturnable<Float> cir) {
        if (option == OptionOF.BRIGHTNESS) {
            cir.setReturnValue(this.ofBrightness);
        } else if (option == OptionOF.CLOUD_HEIGHT) {
            cir.setReturnValue(this.ofCloudsHeight);
        } else if (option == OptionOF.AO_LEVEL) {
            cir.setReturnValue(this.ofAoLevel);
        } else if (option == OptionOF.CHAT_MESSAGE_BUFFER_LIMIT) {
            cir.setReturnValue((float) ((double) this.chatMessageBufferLimit / (MAX_CHAT_BUFFER_LIMIT - 1)));
        } else if (option == OptionOF.PARTICLE_LIMIT) {
            cir.setReturnValue((float) ((double) this.particleLimit / MAX_PARTICLE_LIMIT));
        }
    }

    @Inject(method = "getBooleanValue", at = @At("HEAD"), cancellable = true)
    private void getBooleanValueOF(Option option, CallbackInfoReturnable<Boolean> cir) {
        if (option == OptionOF.AUTO_FAR_CLIP) {
            cir.setReturnValue(this.autoFarClip);
        } else if (option == OptionOF.GRASS_3D) {
            cir.setReturnValue(this.grass3d);
        } else if (option == OptionOF.ALLOW_JAVA_IN_SCRIPT) {
            cir.setReturnValue(this.allowJavaInScript);
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
        if (option.isProgress()) {
            float value = this.getProgressValue(option);
            if (option == OptionOF.CHAT_MESSAGE_BUFFER_LIMIT) {
                return prefix + (int) (value * (double) (MAX_CHAT_BUFFER_LIMIT - 1));
            }
            if (option == Option.SENSITIVITY) {
                if (value == 0.0F)
                    return prefix + ts.get("options.sensitivity.min");
                if (value == 1.0F)
                    return prefix + ts.get("options.sensitivity.max");
                return prefix + (int) (value * 200.0F) + "%";
            }
            if (value == 0.0F) {
                return prefix + ts.get("options.off");
            }
            return prefix + (int) (value * 100.0F) + "%";
        } else if (option == Option.ADVANCED_OPENGL) {
            if (!this.advancedOpengl) {
                return prefix + "OFF";
            }
            return this.ofOcclusionFancy ? prefix + "Fancy" : prefix + "Fast";
        } else if (option.isBoolean()) {
            boolean value = this.getBooleanValue(option);
            if (value) {
                return prefix + ts.get("options.on");
            }
            return prefix + ts.get("options.off");
        } else if (option == Option.RENDER_DISTANCE) {
            return prefix + ts.get(RENDER_DISTANCES[this.viewDistance]);
        } else if (option == Option.DIFFICULTY) {
            return prefix + ts.get(DIFFICULTIES[this.difficulty]);
        } else if (option == Option.GUI_SCALE) {
            return prefix + ts.get(GUI_SCALES[this.guiScale]);
        } else if (option == Option.FRAMERATE_LIMIT) {
            if (this.fpsLimit == 3) {
                return prefix + "VSync";
            }
            return prefix + Language.getOrDefault(PERFORMANCE_OPTIONS[this.fpsLimit]);
        } else if (option == OptionOF.FOG_FANCY) {
            return this.ofFogFancy ? prefix + "Fancy" : prefix + "Fast";
        } else if (option == OptionOF.FOG_START) {
            return prefix + this.ofFogStart;
        } else if (option == OptionOF.MIPMAP_LEVEL) {
            return prefix + this.ofMipmapLevel;
        } else if (option == OptionOF.MIPMAP_TYPE) {
            return this.ofMipmapLinear ? prefix + "Linear" : prefix + "Nearest";
        } else if (option == OptionOF.LOAD_FAR) {
            return this.ofLoadFar ? prefix + "ON" : prefix + "OFF";
        } else if (option == OptionOF.PRELOADED_CHUNKS) {
            if (this.ofPreloadedChunks == 0) {
                return prefix + "OFF";
            }
            return prefix + this.ofPreloadedChunks;
        } else if (option == OptionOF.SMOOTH_FPS) {
            return this.ofSmoothFps ? prefix + "ON" : prefix + "OFF";
        } else if (option == OptionOF.SMOOTH_INPUT) {
            return this.ofSmoothInput ? prefix + "ON" : prefix + "OFF";
        } else if (option == OptionOF.CLOUDS) {
            return switch (this.ofClouds) {
                case 1 -> prefix + "Fast";
                case 2 -> prefix + "Fancy";
                case 3 -> prefix + "OFF";
                default -> prefix + "Default";
            };
        } else if (option == OptionOF.LEAVES) {
            return switch (this.ofTrees) {
                case 1 -> prefix + "Fast";
                case 2 -> prefix + "Fancy";
                default -> prefix + "Default";
            };
        } else if (option == OptionOF.GRASS) {
            return switch (this.ofGrass) {
                case 1 -> prefix + "Fast";
                case 2 -> prefix + "Fancy";
                default -> prefix + "Default";
            };
        } else if (option == OptionOF.RAIN) {
            return switch (this.ofRain) {
                case 1 -> prefix + "Fast";
                case 2 -> prefix + "Fancy";
                case 3 -> prefix + "OFF";
                default -> prefix + "Default";
            };
        } else if (option == OptionOF.WATER) {
            return switch (this.ofWater) {
                case 1 -> prefix + "Fast";
                case 2 -> prefix + "Fancy";
                case 3 -> prefix + "OFF";
                default -> prefix + "Default";
            };
        } else if (option == OptionOF.ANIMATED_WATER) {
            return switch (this.ofAnimatedWater) {
                case 1 -> prefix + "Dynamic";
                case 2 -> prefix + "OFF";
                default -> prefix + "ON";
            };
        } else if (option == OptionOF.ANIMATED_LAVA) {
            return switch (this.ofAnimatedLava) {
                case 1 -> prefix + "Dynamic";
                case 2 -> prefix + "OFF";
                default -> prefix + "ON";
            };
        } else if (option == OptionOF.ANIMATED_FIRE) {
            return this.ofAnimatedFire ? prefix + "ON" : prefix + "OFF";
        } else if (option == OptionOF.ANIMATED_PORTAL) {
            return this.ofAnimatedPortal ? prefix + "ON" : prefix + "OFF";
        } else if (option == OptionOF.ANIMATED_REDSTONE) {
            return this.ofAnimatedRedstone ? prefix + "ON" : prefix + "OFF";
        } else if (option == OptionOF.ANIMATED_EXPLOSION) {
            return this.ofAnimatedExplosion ? prefix + "ON" : prefix + "OFF";
        } else if (option == OptionOF.ANIMATED_FLAME) {
            return this.ofAnimatedFlame ? prefix + "ON" : prefix + "OFF";
        } else if (option == OptionOF.ANIMATED_SMOKE) {
            return this.ofAnimatedSmoke ? prefix + "ON" : prefix + "OFF";
        } else if (option == OptionOF.FAST_DEBUG_INFO) {
            return this.ofFastDebugInfo ? prefix + "ON" : prefix + "OFF";
        } else if (option == OptionOF.AUTOSAVE_TICKS) {
            if (this.ofAutoSaveTicks <= 40) {
                return prefix + "Default (2s)";
            }
            if (this.ofAutoSaveTicks <= 400) {
                return prefix + "20s";
            }
            if (this.ofAutoSaveTicks <= 4000) {
                return prefix + "3min";
            }
            return prefix + "30min";
        } else if (option == OptionOF.CONNECTED_GRASS) {
            return switch (this.ofConnectedGrass) {
                case FAST -> prefix + "Fast";
                case FANCY -> prefix + "Fancy";
                default -> prefix + "OFF";
            };
        } else {
            if (option == OptionOF.WEATHER)
                return this.ofWeather ? prefix + "ON" : prefix + "OFF";
            if (option == OptionOF.SKY)
                return this.ofSky ? prefix + "ON" : prefix + "OFF";
            if (option == OptionOF.STARS)
                return this.ofStars ? prefix + "ON" : prefix + "OFF";
            if (option == OptionOF.CHUNK_UPDATES)
                return prefix + this.ofChunkUpdates;
            if (option == OptionOF.CHUNK_UPDATES_DYNAMIC)
                return this.ofChunkUpdatesDynamic ? prefix + "ON" : prefix + "OFF";
            if (option == OptionOF.FAR_VIEW)
                return this.ofFarView ? prefix + "ON" : prefix + "OFF";
            if (option == OptionOF.TIME) {
                if (this.ofTime == 1) {
                    return prefix + "Day Only";
                }
                if (this.ofTime == 2) {
                    return prefix + "Night Only";
                }
                return prefix + "Default";
            }
            if (option == OptionOF.CLEAR_WATER)
                return this.ofClearWater ? prefix + "ON" : prefix + "OFF";
            if (option == OptionOF.AA_LEVEL) {
                if (this.ofAaLevel == 0) {
                    return prefix + "OFF";
                }
                return prefix + this.ofAaLevel;
            }
            if (option == OptionOF.AF_LEVEL) {
                if (this.ofAfLevel == 1) {
                    return prefix + "OFF";
                }
                return prefix + this.ofAfLevel;
            }
            if (option == Option.GRAPHICS) {
                if (this.fancyGraphics) {
                    return prefix + ts.get("options.graphics.fancy");
                }
                return prefix + ts.get("options.graphics.fast");
            }
            return prefix;
        }
    }

    @Inject(method = "load", at = @At(
        value = "FIELD",
        target = "Lnet/minecraft/client/Options;fpsLimit:I",
        shift = At.Shift.AFTER))
    private void load_fpsLimit(CallbackInfo ci) {
        Display.setVSyncEnabled(this.fpsLimit == 3);
    }

    @Inject(method = "load", at = @At(
        value = "FIELD",
        target = "Lnet/minecraft/client/Options;ao:Z",
        shift = At.Shift.AFTER))
    private void load_ao(CallbackInfo ci) {
        if (this.ao) {
            this.ofAoLevel = 1.0F;
        } else {
            this.ofAoLevel = 0.0F;
        }
    }

    @Inject(
        method = "load",
        at = @At(
            value = "INVOKE",
            target = "Ljava/lang/String;equals(Ljava/lang/Object;)Z",
            shift = At.Shift.BEFORE,
            ordinal = 0))
    private void loadOF(
        CallbackInfo ci,
        @Local BufferedReader reader,
        @Local String[] parts) {
        if (parts.length < 2) {
            return;
        }

        String name = parts[0];
        String value = parts[1];

        switch (name) {
            case "ofFogFancy" -> this.ofFogFancy = Boolean.parseBoolean(value);
            case "ofFogStart" -> {
                this.ofFogStart = Float.parseFloat(value);
                if (this.ofFogStart < 0.2F) {
                    this.ofFogStart = 0.2F;
                }
                if (this.ofFogStart > 0.81F) {
                    this.ofFogStart = 0.8F;
                }
            }
            case "ofMipmapLevel" -> {
                this.ofMipmapLevel = Integer.parseInt(value);
                if (this.ofMipmapLevel < 0) {
                    this.ofMipmapLevel = 0;
                }
                if (this.ofMipmapLevel > 4) {
                    this.ofMipmapLevel = 4;
                }
            }
            case "ofMipmapLinear" -> this.ofMipmapLinear = Boolean.parseBoolean(value);
            case "ofLoadFar" -> this.ofLoadFar = Boolean.parseBoolean(value);
            case "ofPreloadedChunks" -> {
                this.ofPreloadedChunks = Integer.parseInt(value);
                if (this.ofPreloadedChunks < 0) {
                    this.ofPreloadedChunks = 0;
                }
                if (this.ofPreloadedChunks > 8) {
                    this.ofPreloadedChunks = 8;
                }
            }
            case "ofOcclusionFancy" -> this.ofOcclusionFancy = Boolean.parseBoolean(value);
            case "ofSmoothFps" -> this.ofSmoothFps = Boolean.parseBoolean(value);
            case "ofSmoothInput" -> this.ofSmoothInput = Boolean.parseBoolean(value);
            case "ofBrightness" -> {
                this.ofBrightness = Float.parseFloat(value);
                this.ofBrightness = Config.limit(this.ofBrightness, 0.0F, 1.0F);
                this.updateWorldLightLevels();
            }
            case "ofAoLevel" -> {
                this.ofAoLevel = Float.parseFloat(value);
                this.ofAoLevel = Config.limit(this.ofAoLevel, 0.0F, 1.0F);
                this.ao = this.ofAoLevel > 0.0F;
            }
            case "ofClouds" -> {
                this.ofClouds = Integer.parseInt(value);
                this.ofClouds = Config.limit(this.ofClouds, 0, 3);
            }
            case "ofCloudsHeight" -> {
                this.ofCloudsHeight = Float.parseFloat(value);
                this.ofCloudsHeight = Config.limit(this.ofCloudsHeight, 0.0F, 1.0F);
            }
            case "ofTrees" -> {
                this.ofTrees = Integer.parseInt(value);
                this.ofTrees = Config.limit(this.ofTrees, 0, 2);
            }
            case "ofGrass" -> {
                this.ofGrass = Integer.parseInt(value);
                this.ofGrass = Config.limit(this.ofGrass, 0, 2);
            }
            case "ofRain" -> {
                this.ofRain = Integer.parseInt(value);
                this.ofRain = Config.limit(this.ofRain, 0, 3);
            }
            case "ofWater" -> {
                this.ofWater = Integer.parseInt(value);
                this.ofWater = Config.limit(this.ofWater, 0, 3);
            }
            case "ofAnimatedWater" -> {
                this.ofAnimatedWater = Integer.parseInt(value);
                this.ofAnimatedWater = Config.limit(this.ofAnimatedWater, 0, 2);
            }
            case "ofAnimatedLava" -> {
                this.ofAnimatedLava = Integer.parseInt(value);
                this.ofAnimatedLava = Config.limit(this.ofAnimatedLava, 0, 2);
            }
            case "ofAnimatedFire" -> this.ofAnimatedFire = Boolean.parseBoolean(value);
            case "ofAnimatedPortal" -> this.ofAnimatedPortal = Boolean.parseBoolean(value);
            case "ofAnimatedRedstone" -> this.ofAnimatedRedstone = Boolean.parseBoolean(value);
            case "ofAnimatedExplosion" -> this.ofAnimatedExplosion = Boolean.parseBoolean(value);
            case "ofAnimatedFlame" -> this.ofAnimatedFlame = Boolean.parseBoolean(value);
            case "ofAnimatedSmoke" -> this.ofAnimatedSmoke = Boolean.parseBoolean(value);
            case "ofFastDebugInfo" -> this.ofFastDebugInfo = Boolean.parseBoolean(value);
            case "ofAutoSaveTicks" -> {
                this.ofAutoSaveTicks = Integer.parseInt(value);
                this.ofAutoSaveTicks = Config.limit(this.ofAutoSaveTicks, 40, 40000);
            }
            case "ofConnectedGrass" -> {
                try {
                    this.ofConnectedGrass = ConnectedGrassOption.valueOf(value);
                } catch (IllegalArgumentException e) {
                    this.ofConnectedGrass = ConnectedGrassOption.OFF;
                }
            }
            case "ofWeather" -> this.ofWeather = Boolean.parseBoolean(value);
            case "ofSky" -> this.ofSky = Boolean.parseBoolean(value);
            case "ofStars" -> this.ofStars = Boolean.parseBoolean(value);
            case "ofChunkUpdates" -> {
                this.ofChunkUpdates = Integer.parseInt(value);
                this.ofChunkUpdates = Config.limit(this.ofChunkUpdates, 1, 5);
            }
            case "ofChunkUpdatesDynamic" -> this.ofChunkUpdatesDynamic = Boolean.parseBoolean(value);
            case "ofFarView" -> this.ofFarView = Boolean.parseBoolean(value);
            case "ofTime" -> {
                this.ofTime = Integer.parseInt(value);
                this.ofTime = Config.limit(this.ofTime, 0, 2);
            }
            case "ofClearWater" -> {
                this.ofClearWater = Boolean.parseBoolean(value);
                this.updateWaterOpacity();
            }
            case "ofAaLevel" -> {
                this.ofAaLevel = Integer.parseInt(value);
                this.ofAaLevel = Config.limit(this.ofAaLevel, 0, 16);
            }
            case "ofAfLevel" -> {
                this.ofAfLevel = Integer.parseInt(value);
                this.ofAfLevel = Config.limit(this.ofAfLevel, 1, 16);
            }
            case "autoFarClip" -> this.autoFarClip = Boolean.parseBoolean(value);
            case "grass3d" -> this.grass3d = Boolean.parseBoolean(value);
            case "chatMessageBufferLimit" -> {
                this.chatMessageBufferLimit = Integer.parseInt(value);
                this.chatMessageBufferLimit = Config.limit(this.chatMessageBufferLimit, 1, MAX_CHAT_BUFFER_LIMIT);
            }
            case "particleLimit" -> {
                this.particleLimit = Integer.parseInt(value);
                this.particleLimit = Config.limit(this.particleLimit, 0, MAX_PARTICLE_LIMIT);
            }
            case "allowJavaInScript" -> this.allowJavaInScript = Boolean.parseBoolean(value);
        }
    }

    @Inject(
        method = "save",
        at = @At(
            value = "INVOKE",
            target = "Ljava/io/PrintWriter;close()V",
            shift = At.Shift.BEFORE))
    private void saveOptionsOF(CallbackInfo ci, @Local PrintWriter writer) {
        writer.println("ofFogFancy:" + this.ofFogFancy);
        writer.println("ofFogStart:" + this.ofFogStart);
        writer.println("ofMipmapLevel:" + this.ofMipmapLevel);
        writer.println("ofMipmapLinear:" + this.ofMipmapLinear);
        writer.println("ofLoadFar:" + this.ofLoadFar);
        writer.println("ofPreloadedChunks:" + this.ofPreloadedChunks);
        writer.println("ofOcclusionFancy:" + this.ofOcclusionFancy);
        writer.println("ofSmoothFps:" + this.ofSmoothFps);
        writer.println("ofSmoothInput:" + this.ofSmoothInput);
        writer.println("ofBrightness:" + this.ofBrightness);
        writer.println("ofAoLevel:" + this.ofAoLevel);
        writer.println("ofClouds:" + this.ofClouds);
        writer.println("ofCloudsHeight:" + this.ofCloudsHeight);
        writer.println("ofTrees:" + this.ofTrees);
        writer.println("ofGrass:" + this.ofGrass);
        writer.println("ofRain:" + this.ofRain);
        writer.println("ofWater:" + this.ofWater);
        writer.println("ofAnimatedWater:" + this.ofAnimatedWater);
        writer.println("ofAnimatedLava:" + this.ofAnimatedLava);
        writer.println("ofAnimatedFire:" + this.ofAnimatedFire);
        writer.println("ofAnimatedPortal:" + this.ofAnimatedPortal);
        writer.println("ofAnimatedRedstone:" + this.ofAnimatedRedstone);
        writer.println("ofAnimatedExplosion:" + this.ofAnimatedExplosion);
        writer.println("ofAnimatedFlame:" + this.ofAnimatedFlame);
        writer.println("ofAnimatedSmoke:" + this.ofAnimatedSmoke);
        writer.println("ofFastDebugInfo:" + this.ofFastDebugInfo);
        writer.println("ofAutoSaveTicks:" + this.ofAutoSaveTicks);
        writer.println("ofConnectedGrass:" + this.ofConnectedGrass);
        writer.println("ofWeather:" + this.ofWeather);
        writer.println("ofSky:" + this.ofSky);
        writer.println("ofStars:" + this.ofStars);
        writer.println("ofChunkUpdates:" + this.ofChunkUpdates);
        writer.println("ofChunkUpdatesDynamic:" + this.ofChunkUpdatesDynamic);
        writer.println("ofFarView:" + this.ofFarView);
        writer.println("ofTime:" + this.ofTime);
        writer.println("ofClearWater:" + this.ofClearWater);
        writer.println("ofAaLevel:" + this.ofAaLevel);
        writer.println("ofAfLevel:" + this.ofAfLevel);
        writer.println("autoFarClip:" + this.autoFarClip);
        writer.println("grass3d:" + this.grass3d);
        writer.println("chatMessageBufferLimit:" + this.chatMessageBufferLimit);
        writer.println("particleLimit:" + this.particleLimit);
        writer.println("allowJavaInScript:" + this.allowJavaInScript);
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
    public int getMipmapType() {
        if (ofMipmapLinear()) {
            return GL11.GL_NEAREST_MIPMAP_LINEAR;
        }
        return GL11.GL_NEAREST_MIPMAP_NEAREST;
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
    public boolean isOcclusionEnabled() {
        return advancedOpengl;
    }

    @Override
    public boolean isOcclusionFancy() {
        if (!isOcclusionEnabled()) {
            return false;
        }
        return ofOcclusionFancy();
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
    public boolean isAutoFarClip() {
        return this.autoFarClip;
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
}
