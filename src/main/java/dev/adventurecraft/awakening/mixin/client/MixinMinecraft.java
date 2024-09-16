package dev.adventurecraft.awakening.mixin.client;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.ACMainThread;
import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.client.options.Config;
import dev.adventurecraft.awakening.common.*;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.client.ExMinecraft;
import dev.adventurecraft.awakening.extension.client.entity.player.ExAbstractClientPlayerEntity;
import dev.adventurecraft.awakening.extension.client.gui.screen.ExScreen;
import dev.adventurecraft.awakening.extension.client.options.ExGameOptions;
import dev.adventurecraft.awakening.extension.client.render.ExWorldEventRenderer;
import dev.adventurecraft.awakening.extension.client.sound.ExSoundHelper;
import dev.adventurecraft.awakening.extension.entity.player.ExPlayerEntity;
import dev.adventurecraft.awakening.extension.inventory.ExPlayerInventory;
import dev.adventurecraft.awakening.extension.util.ExProgressListener;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import dev.adventurecraft.awakening.script.ScriptEntity;
import dev.adventurecraft.awakening.script.ScriptItem;
import dev.adventurecraft.awakening.script.ScriptVec3;
import net.fabricmc.loader.impl.util.Arguments;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.ProgressRenderer;
import net.minecraft.client.ScreenSizeCalculator;
import net.minecraft.client.User;
import net.minecraft.client.gamemode.CreativeMode;
import net.minecraft.client.gamemode.GameMode;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.InBedChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.input.KeyboardInput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.Textures;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.stats.Stats;
import net.minecraft.stats.StatsCounter;
import net.minecraft.util.Mth;
import net.minecraft.util.Vec3i;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkCache;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.storage.LevelFormat;
import net.minecraft.world.level.storage.LevelIO;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitType;
import org.lwjgl.LWJGLException;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.PixelFormat;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.*;
import java.net.URL;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft implements ExMinecraft {

    @Shadow
    private int width;
    @Shadow
    private int height;
    @Shadow
    private boolean isFullscreen;
    @Shadow
    public int actualWidth;
    @Shadow
    public int actualHeight;

    @Shadow
    protected abstract void updateScreenResolution(int i, int j);

    @Shadow
    public GameRenderer gameRenderer;

    @Shadow
    public Gui overlay;

    @Shadow
    public LocalPlayer player;

    @Shadow
    public Level world;

    @Shadow
    public volatile boolean paused;

    @Shadow
    public GameMode interactionManager;

    @Shadow
    public Textures textureManager;

    @Shadow
    public Screen currentScreen;

    @Shadow
    public abstract void openScreen(Screen arg);

    @Shadow
    private int mouseTicksProcessed;

    @Shadow
    private int ticksPlayed;

    @Shadow
    private int attackCooldown;

    @Shadow
    protected abstract void method_2110(int i, boolean bl);

    @Shadow
    public abstract void toggleFullscreen();

    @Shadow
    public abstract void openPauseMenu();

    @Shadow
    protected abstract void forceResourceReload();

    @Shadow
    public Options options;

    @Shadow
    public LevelRenderer worldRenderer;

    @Shadow
    public abstract boolean hasWorld();

    @Shadow
    long lastTickTime;

    @Shadow
    public boolean hasFocus;

    @Shadow
    public abstract void lockCursor();

    @Shadow
    protected abstract void method_2103();

    @Shadow
    private int spawnMobCounter;

    @Shadow
    public ParticleEngine particleManager;

    @Shadow
    protected abstract void startLoginThread();

    @Shadow
    public abstract void switchDimension();

    @Shadow
    public LivingEntity viewEntity;

    @Shadow
    protected abstract void loadIntoWorld(String string);

    @Shadow
    public abstract LevelFormat getWorldStorage();

    @Shadow
    public abstract void setWorld(Level arg);

    @Shadow
    public static File getGameDirectory() {
        return null;
    }

    @Shadow
    protected abstract void convertWorldFormat(String string, String string2);

    @Shadow
    public HitResult hitResult;

    @Shadow
    private File gameDir;

    @Shadow
    public static int frameRenderTimesAmount;

    @Shadow
    public StatsCounter statFileWriter;

    @Shadow
    public abstract void notifyStatus(Level arg, String string);

    @Shadow
    public ProgressRenderer progressListener;

    @Shadow
    public SoundEngine soundHelper;

    private long previousNanoTime;
    private double deltaTime;
    private int rightMouseTicksRan;
    public AC_MapList mapList;
    public AC_CutsceneCamera cutsceneCamera = new AC_CutsceneCamera();
    public AC_CutsceneCamera activeCutsceneCamera;
    public boolean cameraActive;
    public boolean cameraPause = true;
    public LivingEntity cutsceneCameraEntity;
    public AC_GuiStore storeGUI = new AC_GuiStore();
    ItemInstance lastItemUsed;
    Entity lastEntityHit;
    ScriptVec3 lastBlockHit;

    @Overwrite(remap = false)
    public static void main(String[] args) {
        var arguments = new Arguments();
        arguments.parse(args);

        String username = arguments.getOrDefault("username", "Player");
        String sessionId = "";

        if (arguments.containsKey("session")) {
            // 1.6
            sessionId = arguments.get("session");
        } else if (arguments.getExtraArgs().size() == 2) {
            // pre 1.6
            username = arguments.getExtraArgs().get(0);
            sessionId = arguments.getExtraArgs().get(1);
        }

        ACMainThread.workDir = new File(arguments.getOrDefault("gameDir", "."));

        if (arguments.containsKey("mapsDir")) {
            ACMainThread.mapsDirectory = new File(arguments.get("mapsDir"));
        }

        ACMainThread.glDebugContext = arguments.getExtraArgs().contains("--glDebugContext");

        ACMainThread.glDebugLogSeverity = ACMainThread.GlDebugSeverity.valueOf(
            arguments.getOrDefault("glDebugLogSeverity", ACMainThread.glDebugLogSeverity.name()));

        ACMainThread.glDebugTraceSeverity = ACMainThread.GlDebugSeverity.valueOf(
            arguments.getOrDefault("glDebugTraceSeverity", ACMainThread.glDebugTraceSeverity.name()));

        boolean fullscreen = arguments.getExtraArgs().contains("--fullscreen");
        int width = Integer.parseInt(arguments.getOrDefault("width", "854"));
        int height = Integer.parseInt(arguments.getOrDefault("height", "480"));

        var acThread = new ACMainThread(width, height, fullscreen);
        acThread.host = "www.minecraft.net";
        acThread.user = new User(username, sessionId);

        boolean doConnect = arguments.containsKey("server") && arguments.containsKey("port");
        if (doConnect) {
            String host = arguments.get("server");
            String port = arguments.get("port");
            acThread.connectTo(host, Integer.parseInt(port));
        }

        Thread thread = Thread.currentThread();
        thread.setName("Minecraft main thread");
        thread.setPriority(10);
        acThread.run();
    }

    @ModifyConstant(method = "init", constant = @Constant(stringValue = "Minecraft Minecraft Beta 1.7.3"))
    private String init_fixTitle(String constant) {
        return "Adventurecraft (Beta 1.7.3)";
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void init_makeResizable(CallbackInfo ci) {
        this.width = this.actualWidth;
        Display.setResizable(true);
    }

    @Redirect(
        method = "init",
        at = @At(
            value = "INVOKE",
            target = "Lorg/lwjgl/opengl/Display;create()V",
            remap = false,
            ordinal = 0))
    private void init_disableOriginalDisplay() {
    }

    @Inject(
        method = "init",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;options:Lnet/minecraft/client/options/GameOptions;",
            shift = At.Shift.AFTER,
            ordinal = 0))
    private void init_createDisplay(CallbackInfo ci) throws LWJGLException {
        int sampleCount = ((ExGameOptions) options).ofAaLevel();
        ACMod.LOGGER.info("MSAA Samples: {}x", sampleCount);

        if (ACMainThread.glDebugContext) {
            GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_DEBUG_CONTEXT, 1);
        }

        try {
            createDisplay(new PixelFormat().withSamples(sampleCount), true);
            Config.logOpenGlCaps();
            return;
        } catch (LWJGLException ex) {
            ACMod.LOGGER.warn("Error setting MSAA {}x: ", sampleCount, ex);
        }

        createDisplay(new PixelFormat(), false);
        Config.logOpenGlCaps();
    }

    private void createDisplay(PixelFormat pixelFormat, boolean rethrowLast) throws LWJGLException {
        try {
            Display.create(pixelFormat.withDepthBits(32));
            return;
        } catch (LWJGLException e) {
            ACMod.LOGGER.warn("Falling back to 24-bit depth buffer since 32-bit failed: ", e);
        }

        try {
            Display.create(pixelFormat.withDepthBits(24));
            return;
        } catch (LWJGLException e) {
            ACMod.LOGGER.warn("Falling back to 16-bit depth buffer since 24-bit failed: ", e);
        }

        try {
            Display.create(pixelFormat.withDepthBits(16));
            return;
        } catch (LWJGLException e) {
            ACMod.LOGGER.warn("Falling back to 8-bit depth buffer since 16-bit failed: ", e);
        }

        try {
            Display.create(pixelFormat.withDepthBits(8));
        } catch (LWJGLException e) {
            ACMod.LOGGER.warn("Falling back to unspecified depth buffer since 8-bit failed: ", e);

            if (rethrowLast) {
                throw e;
            }
        }

        Display.create(pixelFormat.withDepthBits(0));
    }

    @Inject(
        method = "init",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/TexturePackManager;<init>(Lnet/minecraft/client/Minecraft;Ljava/io/File;)V",
            shift = At.Shift.AFTER))
    private void init_createMapList(CallbackInfo ci) {
        this.mapList = new AC_MapList();
    }

    @Inject(
        method = "init",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/texture/TextureManager;addTextureBinder(Lnet/minecraft/client/render/TextureBinder;)V",
            ordinal = 0))
    private void init_addFanTextureBinder(CallbackInfo ci) {
        this.textureManager.addDynamicTexture(new AC_TextureFanFX());
    }

    @Inject(
        method = "run",
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Ljava/lang/System;currentTimeMillis()J",
            shift = At.Shift.BEFORE,
            ordinal = 0,
            remap = false))
    private void run_setup(CallbackInfo ci) {
        this.textureManager.loadTexture("/terrain.png");
        this.textureManager.loadTexture("/terrain2.png");
        this.textureManager.loadTexture("/terrain3.png");
        ContextFactory.initGlobal(new ContextFactory());
    }

    @Inject(
        method = "run",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/math/AxixAlignedBoundingBox;method_85()V",
            shift = At.Shift.BEFORE))
    private void run_updateDeltaTime(CallbackInfo ci) {
        long nanoTime = System.nanoTime();
        this.deltaTime = (nanoTime - this.previousNanoTime) / (double) 1_000_000_000;
        this.previousNanoTime = nanoTime;
    }

    @Redirect(
        method = "run",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/sound/SoundHelper;setSoundPosition(Lnet/minecraft/entity/LivingEntity;F)V"))
    private void run_setSoundListenerPos(SoundEngine instance, LivingEntity f, float v) {
        if (this.cameraActive) {
            instance.update(this.cutsceneCameraEntity, v);
        } else {
            instance.update(f, v);
        }
    }

    @Inject(
        method = "run",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Minecraft;stop()V",
            shift = At.Shift.BEFORE))
    private void run_stop(CallbackInfo ci) {
        ContextFactory.getGlobal().enterContext();
        Context.exit();
    }

    @Inject(
        method = "run",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Minecraft;checkTakingScreenshot()V",
            shift = At.Shift.AFTER))
    private void fix_resize(CallbackInfo ci) {
        if (!this.isFullscreen && (Display.getWidth() != this.actualWidth || Display.getHeight() != this.actualHeight)) {
            this.actualWidth = Display.getWidth();
            this.actualHeight = Display.getHeight();
            if (this.actualWidth <= 0) {
                this.actualWidth = 1;
            }

            if (this.actualHeight <= 0) {
                this.actualHeight = 1;
            }

            this.updateScreenResolution(this.actualWidth, this.actualHeight);
        }
    }

    @Redirect(
        method = "run",
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Ljava/lang/Thread;yield()V",
            remap = false))
    private void removeYield() {
    }

    @Redirect(
        method = "run",
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Lorg/lwjgl/opengl/Display;isActive()Z",
            remap = false))
    private boolean disableDoubleToggle() {
        return true;
    }

    @WrapWithCondition(
        method = "run",
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Minecraft;method_2111(J)V"))
    private boolean renderFrameTimeGraph(Minecraft instance, long time) {
        return !((ExGameOptions) this.options).ofFastDebugInfo();
    }

    @Inject(
        method = "run",
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Minecraft;method_2131()V",
            shift = At.Shift.AFTER,
            ordinal = 0))
    private void printStackOnOutOfMem(CallbackInfo ci, @Local OutOfMemoryError error) {
        error.printStackTrace();
    }

    @Redirect(
        method = "toggleFullscreen",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;width:I"))
    private int fix_getWidthAfterFullscreen(Minecraft instance) {
        return Display.getWidth();
    }

    @Redirect(
        method = "toggleFullscreen",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;height:I"))
    private int fix_getHeightAfterFullscreen(Minecraft instance) {
        return Display.getHeight();
    }

    @Inject(
        method = "toggleFullscreen",
        at = @At(
            value = "INVOKE",
            target = "Lorg/lwjgl/opengl/Display;setFullscreen(Z)V",
            remap = false,
            shift = At.Shift.AFTER))
    private void fix_restoreSizeAfterFullscreen(CallbackInfo ci) throws LWJGLException {
        if (!this.isFullscreen && !Display.isMaximized()) {
            Display.setDisplayMode(new DisplayMode(this.width, this.height));
        }
    }

    @Inject(method = "updateScreenResolution", at = @At("TAIL"))
    private void updateStoreGuiOnResize(int var1, int var2, CallbackInfo ci) {
        updateStoreGUI();
    }

    /**
     * @author Cryect
     * (Kiroto added Javadoc)
     * @reason Different things done on tick
     */
    @Overwrite
    public void tick() {
        if (this.ticksPlayed == 6000) {
            this.startLoginThread();
        }

        this.overlay.tick();
        this.gameRenderer.pick(1.0F);

        if (this.player != null) {
            ChunkSource worldSource = this.world.getChunkSource();
            if (worldSource instanceof ChunkCache chunkCache) {
                int chunkX = Mth.floor((float) ((int) this.player.x)) >> 4;
                int chunkZ = Mth.floor((float) ((int) this.player.z)) >> 4;
                chunkCache.centerOn(chunkX, chunkZ);
            }
            // Bed safety leave
            if(this.player.isSleeping() && this.player.getSleepTimer()>= 100)
            {
                if(((ExWorldProperties) this.world.levelData).getTimeRate()==0){
                    this.player.stopSleepInBed(true,false,false);
                } else {
                    ((ExWorld) this.world).setTimeOfDay(10000);
                }
            }
        }

        if (!this.paused && this.world != null) {
            this.interactionManager.tick();
        }

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.textureManager.loadTexture("/terrain.png"));
        if (!this.paused) {
            this.textureManager.tick();
        }

        if (this.currentScreen == null && this.player != null) {
            if (this.player.health <= 0) {
                this.openScreen(null);
            } else if (this.player.isSleeping() && this.world != null && this.world.isClientSide) {
                this.openScreen(new InBedChatScreen());
            }
        } else if (this.currentScreen != null && this.currentScreen instanceof InBedChatScreen && !this.player.isSleeping()) {
            this.openScreen(null);
        }

        if (this.currentScreen != null && !((ExScreen) this.currentScreen).isDisabledInputGrabbing()) {
            this.mouseTicksProcessed = this.ticksPlayed + 10000;

            this.currentScreen.updateEvents();
            if (this.currentScreen != null) {
                this.currentScreen.particles.tick();
                this.currentScreen.tick();
            }
        }

        if (this.currentScreen == null || this.currentScreen.passEvents || ((ExScreen) this.currentScreen).isDisabledInputGrabbing()) {
            label405:
            while (true) {
                while (true) {
                    long var7;
                    do {
                        if (Mouse.next()) {
                            var7 = System.currentTimeMillis() - this.lastTickTime;
                            continue;
                        }

                        if (this.attackCooldown > 0) {
                            --this.attackCooldown;
                        }

                        while (true) {

                            do {
                                if (!Keyboard.next()) {
                                    if (this.currentScreen == null || ((ExScreen) this.currentScreen).isDisabledInputGrabbing()) {
                                        if (Mouse.isButtonDown(0) && (float) (this.ticksPlayed - this.mouseTicksProcessed) >= 0.0F && this.hasFocus) {
                                            this.method_2107(0);
                                        }

                                        if (Mouse.isButtonDown(1) && (float) (this.ticksPlayed - this.rightMouseTicksRan) >= 0.0F && this.hasFocus) {
                                            this.method_2107(1);
                                        }
                                    }

                                    this.method_2110(0, (this.currentScreen == null || ((ExScreen) this.currentScreen).isDisabledInputGrabbing()) && Mouse.isButtonDown(0) && this.hasFocus);
                                    break label405;
                                }

                                this.player.setKey(Keyboard.getEventKey(), Keyboard.getEventKeyState());
                            } while (!Keyboard.getEventKeyState());

                            int eventKey = Keyboard.getEventKey();
                            boolean isShiftPressed = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
                            boolean isControlPressed = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);

                            if (eventKey == Keyboard.KEY_F11) {
                                this.toggleFullscreen();
                            } else {
                                if (this.currentScreen != null && !((ExScreen) this.currentScreen).isDisabledInputGrabbing()) {
                                    // TODO: fix doubled events (one for key press, one for text input)
                                    this.currentScreen.keyboardEvent();
                                } else {
                                    // Not compile time constants, else-if is a must.
                                    // Trust me, I tried to use a switch here.
                                    if (eventKey == Keyboard.KEY_ESCAPE) {
                                        this.openPauseMenu();

                                    } else if (eventKey == Keyboard.KEY_S && Keyboard.isKeyDown(Keyboard.KEY_F3)) {
                                        this.forceResourceReload();

                                    } else if (eventKey == Keyboard.KEY_F1) {
                                        this.options.hideGui = !this.options.hideGui;

                                    } else if (eventKey == Keyboard.KEY_F3) {
                                        this.options.renderDebug = !this.options.renderDebug;

                                    } else if (eventKey == Keyboard.KEY_F4) {
                                        AC_DebugMode.active = !AC_DebugMode.active;
                                        if (AC_DebugMode.active) {
                                            this.overlay.addMessage("Debug Mode Active");
                                        } else {
                                            this.overlay.addMessage("Debug Mode Deactivated");
                                        }
                                        ((ExWorldEventRenderer) this.worldRenderer).updateAllTheRenderers();

                                    } else if (eventKey == Keyboard.KEY_F5) {
                                        this.options.thirdPersonView = !this.options.thirdPersonView;

                                    } else if (eventKey == Keyboard.KEY_F6) {
                                        if (AC_DebugMode.active) {
                                            ((ExWorldEventRenderer) this.worldRenderer).resetAll();
                                            this.overlay.addMessage("Resetting all blocks in loaded chunks");
                                        }

                                    } else if (eventKey == Keyboard.KEY_F7 || (eventKey == this.options.keyInventory.key && isShiftPressed)) {
                                        ((ExAbstractClientPlayerEntity) this.player).displayGUIPalette();

                                    } else if (eventKey == this.options.keyInventory.key) {
                                        this.openScreen(new InventoryScreen(this.player));

                                    } else if (eventKey == this.options.keyDrop.key) {
                                        this.player.drop();

                                    } else if ((this.hasWorld() || AC_DebugMode.active) && eventKey == this.options.keyChat.key) {
                                        this.openScreen(new ChatScreen());

                                    } else if (AC_DebugMode.active && isControlPressed) {
                                        if (eventKey == Keyboard.KEY_Z) { // Undo
                                            ServerCommands.cmdUndo(new ServerCommandSource(
                                                (Minecraft) (Object) this, this.world, this.player), null);

                                        } else if (eventKey == Keyboard.KEY_Y) { // Redo
                                            ServerCommands.cmdRedo(new ServerCommandSource(
                                                (Minecraft) (Object) this, this.world, this.player), null);
                                        }
                                    }
                                }

                                int currentSlot = 0;

                                while (true) {
                                    if (currentSlot >= 9) {
                                        if (eventKey == this.options.keyFog.key) {
                                            this.options.toggle(Option.RENDER_DISTANCE, !isShiftPressed ? 1 : -1);
                                        }
                                        break;
                                    }

                                    if (eventKey == Keyboard.KEY_1 + currentSlot) {
                                        if (!isControlPressed) {
                                            if (currentSlot == ((ExPlayerInventory) this.player.inventory).getOffhandItem()) {
                                                ((ExPlayerInventory) this.player.inventory).setOffhandItem(this.player.inventory.selected);
                                            }

                                            this.player.inventory.selected = currentSlot;
                                        } else {
                                            if (currentSlot == this.player.inventory.selected) {
                                                this.player.inventory.selected = ((ExPlayerInventory) this.player.inventory).getOffhandItem();
                                            }

                                            ((ExPlayerInventory) this.player.inventory).setOffhandItem(currentSlot);
                                        }
                                    }

                                    ++currentSlot;
                                }
                            }

                            if (this.world != null) {
                                ((ExWorld) this.world).getScript().keyboard.processKeyPress(eventKey);
                            }
                        }

                    } while (var7 > 200L);

                    int wheelDelta = Mouse.getEventDWheel();
                    if (wheelDelta != 0) {
                        boolean ctrlDown = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
                        boolean menuDown = Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU);
                        if (wheelDelta > 0) {
                            wheelDelta = 1;
                        }

                        if (wheelDelta < 0) {
                            wheelDelta = -1;
                        }

                        if (AC_DebugMode.active && menuDown) {
                            AC_DebugMode.reachDistance += wheelDelta;
                            AC_DebugMode.reachDistance = Math.min(Math.max(AC_DebugMode.reachDistance, 2), 100);
                            this.overlay.addMessage(String.format("Reach Changed to %d", AC_DebugMode.reachDistance));
                        } else {
                            if (ctrlDown) {
                                int selectedSlot = this.player.inventory.selected;
                                this.player.inventory.selected = ((ExPlayerInventory) this.player.inventory).getOffhandItem();
                                ((ExPlayerInventory) this.player.inventory).setOffhandItem(selectedSlot);
                            }

                            this.player.inventory.swapPaint(wheelDelta);
                            if (ctrlDown) {
                                int selectedSlot = this.player.inventory.selected;
                                this.player.inventory.selected = ((ExPlayerInventory) this.player.inventory).getOffhandItem();
                                ((ExPlayerInventory) this.player.inventory).setOffhandItem(selectedSlot);
                            }

                            if (this.options.discreteMouseScroll) {
                                this.options.accumulatedScroll += (float) wheelDelta * 0.25F;
                            }
                        }
                    }

                    if (this.currentScreen != null && !((ExScreen) this.currentScreen).isDisabledInputGrabbing()) {
                        if (this.currentScreen != null) {
                            this.currentScreen.mouseEvent();
                        }
                    } else if (!this.hasFocus && Mouse.getEventButtonState()) {
                        this.lockCursor();
                    } else {
                        if (Mouse.getEventButton() == 0 && Mouse.getEventButtonState()) {
                            this.method_2107(0);
                        }

                        if (Mouse.getEventButton() == 1 && Mouse.getEventButtonState()) {
                            this.method_2107(1);
                        }

                        if (Mouse.getEventButton() == 2 && Mouse.getEventButtonState()) {
                            this.method_2103();
                        }
                    }
                }
            }
        }

        if (this.world != null) {
            if (this.player != null) {
                ++this.spawnMobCounter;
                if (this.spawnMobCounter == 30) {
                    this.spawnMobCounter = 0;
                    this.world.ensureAdded(this.player);
                }
            }

            this.world.difficulty = this.options.difficulty;
            if (this.world.isClientSide) {
                this.world.difficulty = 3;
            }

            if (!this.paused) {
                this.gameRenderer.tick();
            }

            if (!this.paused) {
                this.worldRenderer.tick();
            }

            if (!this.paused || this.hasWorld()) {
                ((ExWorld) this.world).ac$preTick();
            }

            if (!this.paused) {
                if (this.world.skyFlashTime > 0) {
                    --this.world.skyFlashTime;
                }

                this.world.tickEntities();
            }

            if (!this.paused || this.hasWorld()) {
                this.world.setSpawnSettings(this.options.difficulty > 0, true);
                this.world.tick();
            }

            if (!this.paused && this.world != null) {
                this.world.animateTick(
                    Mth.floor(this.player.x),
                    Mth.floor(this.player.y),
                    Mth.floor(this.player.z));
            }

            if (!this.paused) {
                this.particleManager.tick();
            }
        }

        this.lastTickTime = System.currentTimeMillis();
    }

    @Redirect(
        method = {"method_2110", "lockCursor"},
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;attackCooldown:I",
            ordinal = 0))
    private void keepAttackCooldown(Minecraft instance, int value) {
    }

    @Overwrite
    private void method_2107(int mouseButton) {
        if (mouseButton == 0 && this.attackCooldown > 0) {
            return;
        }

        var exWorld = (ExWorld) this.world;
        if (AC_DebugMode.active) {
            exWorld.getUndoStack().startRecording();
        }

        boolean swapOffhand = false;
        ItemInstance stack = this.player.inventory.getSelected();
        if (!AC_DebugMode.active) {
            if (mouseButton == 0) {
                stack = ((ExPlayerInventory) this.player.inventory).getOffhandItemStack();
                ((ExPlayerInventory) this.player.inventory).swapOffhandWithMain();
                swapOffhand = true;
                ((ExPlayerEntity) this.player).setSwappedItems(true);
            }

            int useDelay = 5;
            if (stack != null && Item.items[stack.id] instanceof AC_IUseDelayItem useDelayItem) {
                useDelay = useDelayItem.getItemUseDelay();
            }

            if (mouseButton == 0) {
                this.mouseTicksProcessed = this.ticksPlayed + useDelay;
            } else {
                this.rightMouseTicksRan = this.ticksPlayed + useDelay;
            }

            if (stack != null &&
                (Item.items[stack.id] instanceof AC_ILeftClickItem leftClickItem) &&
                leftClickItem.mainActionLeftClick()) {
                mouseButton = 0;
            } else {
                mouseButton = 1;
            }
        } else {
            this.mouseTicksProcessed = this.ticksPlayed + 5;
            this.rightMouseTicksRan = this.ticksPlayed + 5;
        }

        if (mouseButton == 0) {
            this.player.swing();
        }

        boolean useOnBlock = true;
        if (this.hitResult == null) {
            if (mouseButton == 0 && !(this.interactionManager instanceof CreativeMode)) {
                this.attackCooldown = 10;
            }
        } else if (this.hitResult.hitType == HitType.ENTITY) {
            if (mouseButton == 0) {
                this.interactionManager.attack(this.player, this.hitResult.entity);
            }

            if (mouseButton == 1) {
                this.interactionManager.interact(this.player, this.hitResult.entity);
            }
        } else if (this.hitResult.hitType == HitType.TILE) {
            int bX = this.hitResult.x;
            int bY = this.hitResult.y;
            int bZ = this.hitResult.z;
            int bSide = this.hitResult.face;
            Tile block = Tile.tiles[this.world.getTile(bX, bY, bZ)];
            if (block != null) {
                if (!AC_DebugMode.active && (block.id == Tile.CHEST.id || block.id == AC_Blocks.store.id)) {
                    mouseButton = 1;
                }

                if (!AC_DebugMode.active) {
                    int var11 = ((ExBlock) block).alwaysUseClick(this.world, bX, bY, bZ);
                    if (var11 != -1) {
                        mouseButton = var11;
                    }
                }

                if (mouseButton == 0) {
                    this.interactionManager.startDestroyBlock(bX, bY, bZ, this.hitResult.face);
                    if (stack != null && Item.items[stack.id] instanceof AC_ILeftClickItem leftClickItem) {
                        leftClickItem.onItemUseLeftClick(stack, this.player, this.world, bX, bY, bZ, bSide);
                    }
                } else {
                    int count = stack == null ? 0 : stack.count;
                    if (this.interactionManager.useItemOn(this.player, this.world, stack, bX, bY, bZ, bSide)) {
                        useOnBlock = false;
                        this.player.swing();
                    }

                    if (stack == null) {
                        if (AC_DebugMode.active) {
                            exWorld.getUndoStack().stopRecording();
                        }
                        //return;
                    } else if (stack.count == 0 && stack == this.player.inventory.items[this.player.inventory.selected]) {
                        this.player.inventory.items[this.player.inventory.selected] = null;
                    } else if (stack.count != count) {
                        this.gameRenderer.itemInHandRenderer.itemPlaced();
                    }
                }
            }
        }

        if (useOnBlock && mouseButton == 0 && stack != null) {
            if (Item.items[stack.id] instanceof AC_ILeftClickItem leftClickItem) {
                leftClickItem.onItemLeftClick(stack, this.world, this.player);
            }
        }

        if (useOnBlock && mouseButton == 1 && stack != null && this.interactionManager.useItem(this.player, this.world, stack)) {
            this.gameRenderer.itemInHandRenderer.itemUsed();
        }
        // Hitblock and hitEntity sets
        Scriptable globalScope = exWorld.getScript().globalScope;
        // lastItemUsed
        if (stack != null) {
            if (this.lastItemUsed != stack) {
                var tmp = Context.javaToJS(new ScriptItem(stack), globalScope);
                ScriptableObject.putProperty(globalScope, "lastItemUsed", tmp);
                this.lastItemUsed = stack;
            }
        }
        else {
            var tmp = Context.javaToJS(null, globalScope);
            ScriptableObject.putProperty(globalScope, "lastItemUsed", tmp);
            this.lastItemUsed = null;
        }
        // Hit result sets
        if (this.hitResult == null) {
            // Hit Air
            if (this.lastEntityHit != null) {
                this.lastEntityHit = null;
                var tmp = Context.javaToJS(null, globalScope);
                ScriptableObject.putProperty(globalScope, "hitEntity", tmp);
            }

            if (this.lastBlockHit != null) {
                this.lastBlockHit = null;
                var tmp = Context.javaToJS(null, globalScope);
                ScriptableObject.putProperty(globalScope, "hitBlock", tmp);
            }
        } else if (this.hitResult.hitType == HitType.ENTITY) {
            // Hit an entity
            if (this.lastEntityHit != this.hitResult.entity) {
                this.lastEntityHit = this.hitResult.entity;
                var tmp = Context.javaToJS(ScriptEntity.getEntityClass(this.hitResult.entity), globalScope);
                ScriptableObject.putProperty(globalScope, "hitEntity", tmp);
            }

            if (this.lastBlockHit != null) {
                this.lastBlockHit = null;
                var tmp = Context.javaToJS(null, globalScope);
                ScriptableObject.putProperty(globalScope, "hitBlock", tmp);
            }
        } else if (this.hitResult.hitType != HitType.TILE) {
            // Hit ???
            if (this.lastEntityHit != null) {
                this.lastEntityHit = null;
                var tmp = Context.javaToJS(null, globalScope);
                ScriptableObject.putProperty(globalScope, "hitEntity", tmp);
            }

            if (this.lastBlockHit != null) {
                this.lastBlockHit = null;
                    var tmp = Context.javaToJS(null, globalScope);
                    ScriptableObject.putProperty(globalScope, "hitBlock", tmp);
                }
        } else {
            // Hit a block
            if (this.lastBlockHit == null ||
                this.lastBlockHit.x != (double) this.hitResult.x ||
                this.lastBlockHit.y != (double) this.hitResult.y ||
                this.lastBlockHit.z != (double) this.hitResult.z) {

                this.lastBlockHit = new ScriptVec3((float) this.hitResult.x, (float) this.hitResult.y, (float) this.hitResult.z);
                var tmp = Context.javaToJS(this.lastBlockHit, globalScope);
                ScriptableObject.putProperty(globalScope, "hitBlock", tmp);
            }

            if (this.lastEntityHit != null) {
                this.lastEntityHit = null;
                var tmp = Context.javaToJS(null, globalScope);
                ScriptableObject.putProperty(globalScope, "hitEntity", tmp);
            }
        }
        // Trigger item scripts
        String scriptName;
        if(stack != null) {
            scriptName = stack.isStackedByData()
                ? String.format("item_%d_%d.js", stack.id, stack.getAuxValue())
                : String.format("item_%d.js", stack.id);
        }
        else {
            scriptName = "item_0.js";
        }
        exWorld.getScriptHandler().runScript(scriptName, exWorld.getScope(), false);

        if (swapOffhand) {
            ((ExPlayerInventory) this.player.inventory).swapOffhandWithMain();
            ((ExPlayerEntity) this.player).setSwappedItems(false);
        }

        if (AC_DebugMode.active) {
            exWorld.getUndoStack().stopRecording();
        }
    }

    @Inject(
        method = "initWorld",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/ClientInteractionManager;onInitWorld(Lnet/minecraft/world/World;)V",
            shift = At.Shift.BEFORE))
    private void loadMapTexOnInit(Level var1, String var2, Player var3, CallbackInfo ci) {
        ((ExWorld) this.world).loadMapTextures();
    }

    @Inject(
        method = "initWorld",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/ClientInteractionManager;rotatePlayer(Lnet/minecraft/entity/player/PlayerEntity;)V",
            shift = At.Shift.AFTER))
    private void initPlayerOnInit(Level var1, String var2, Player var3, CallbackInfo ci) {
        this.cutsceneCameraEntity = this.interactionManager.createPlayer(var1);
        ((ExWorld) this.world).getScript().initPlayer(this.player);
    }

    @Redirect(
        method = "loadIntoWorld",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/ProgressListenerImpl;progressStagePercentage(I)V"))
    private void reportPreciseTerrainProgress(
        ProgressRenderer instance,
        int i,
        @Local(ordinal = 1) int count,
        @Local(ordinal = 2) int max) {

        String stage = String.format("%4d / %4d", count, max);
        ((ExProgressListener) this.progressListener).notifyProgress(
            stage, count / (double) max, false);
    }

    @Overwrite
    public void spawn(boolean var1, int var2) {
        if (!this.world.isClientSide && !this.world.dimension.mayRespawn()) {
            this.switchDimension();
        }

        ChunkSource worldSource = this.world.getChunkSource();
        if (worldSource instanceof ChunkCache chunkCache) {
            Vec3i spawnPos = this.world.getSpawnPos();
            chunkCache.centerOn(spawnPos.x >> 4, spawnPos.z >> 4);
        }

        this.world.removeAllPendingEntityRemovals();
        int playerId = 0;
        if (this.player != null) {
            playerId = this.player.id;
            this.world.removeEntity(this.player);
        } else {
            this.player = (LocalPlayer) this.interactionManager.createPlayer(this.world);
            ((ExWorld) this.world).getScript().initPlayer(this.player);
        }

        ((ExWorldEventRenderer) this.worldRenderer).resetForDeath();
        Vec3i spawnPos = this.world.getSpawnPos();
        this.player.resetPos();
        this.player.moveTo((double) spawnPos.x + 0.5D, spawnPos.y, (double) spawnPos.z + 0.5D, 0.0F, 0.0F);
        this.viewEntity = this.player;
        this.player.resetPos();
        this.interactionManager.initPlayer(this.player);
        this.world.loadPlayer(this.player);
        this.player.input = new KeyboardInput(this.options);
        this.player.id = playerId;
        this.player.method_494();
        this.player.setRot(((ExWorld) this.world).getSpawnYaw(), 0.0F);
        this.interactionManager.adjustPlayer(this.player);
        this.loadIntoWorld("Respawning");
        if (this.currentScreen instanceof DeathScreen) {
            this.openScreen(null);
        }
    }

    @Overwrite
    public void createOrLoadWorld(String var1, String saveName, long seed) {
        String mapName = this.getMapUsed(var1);
        if (Mth.isStringInvalid(mapName)) {
            this.openScreen(new AC_GuiMapSelect(null, var1));
        } else {
            this.startWorld(var1, saveName, seed, mapName);
        }
    }

    @Override
    public Level getWorld(String saveName, long seed, String mapName) {
        this.setWorld(null);
        LevelIO dimData = this.getWorldStorage().method_1009(saveName, false);
        Level world = ExWorld.createWorld(mapName, dimData, saveName, seed, this.progressListener);
        return world;
    }

    @Override
    public void startWorld(String worldName, String saveName, long seed, String mapName) {
        this.setWorld(null);
        System.gc();
        if (worldName != null && this.getWorldStorage().requiresConversion(worldName)) {
            this.convertWorldFormat(worldName, saveName);
        } else {
            // TODO: reset global state in consistent matter
            AC_DebugMode.active = false;
            AC_DebugMode.levelEditing = false;
            LevelIO dimData = null;
            if (worldName != null) {
                dimData = this.getWorldStorage().method_1009(worldName, false);
            }

            if (saveName == null) {
                saveName = "Map Editing";
            }

            Level world = ExWorld.createWorld(mapName, dimData, saveName, seed, this.progressListener);
            if (world.isNew) {
                this.statFileWriter.addStat(Stats.CREATE_WORLD, 1);
                this.statFileWriter.addStat(Stats.START_GAME, 1);
                this.notifyStatus(world, "Generating level");
            } else {
                this.statFileWriter.addStat(Stats.LOAD_WORLD, 1);
                this.statFileWriter.addStat(Stats.START_GAME, 1);
                this.notifyStatus(world, "Loading level");
            }
        }

        this.openScreen(null);
    }

    private File getWorldFolder(String worldName) {
        File gameFolder = getGameDirectory();
        File savesFolder = new File(gameFolder, "saves");
        File worldFolder = new File(savesFolder, worldName);
        return worldFolder;
    }

    @Override
    public String getMapUsed(String worldName) {
        File worldFolder = getWorldFolder(worldName);
        File mapNameFile = new File(worldFolder, "map.txt");
        if (mapNameFile.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(mapNameFile));
                String mapName = reader.readLine();
                reader.close();
                return mapName;
            } catch (FileNotFoundException var8) {
            } catch (IOException var9) {
            }
        }
        return null;
    }

    @Override
    public void saveMapUsed(String worldName, String mapName) {
        File worldFolder = getWorldFolder(worldName);
        worldFolder.mkdirs();
        File mapNameFile = new File(worldFolder, "map.txt");
        try {
            mapNameFile.delete();
            mapNameFile.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(mapNameFile));
            writer.write(mapName);
            writer.close();
        } catch (FileNotFoundException var8) {
        } catch (IOException var9) {
        }
    }

    @Override
    public void loadSoundFromDir(String path, URL url) {
        int n = path.indexOf("/");
        String firstDir = path.substring(0, n);
        String id = path.substring(n + 1);

        var sound = (ExSoundHelper) this.soundHelper;
        if (firstDir.equalsIgnoreCase("sound")) {
            sound.addSound(id, url);
        } else if (firstDir.equalsIgnoreCase("newsound")) {
            sound.addSound(id, url);
        } else if (firstDir.equalsIgnoreCase("streaming")) {
            sound.addStreaming(id, url);
        } else if (firstDir.equalsIgnoreCase("music")) {
            sound.addMusic(id, url);
        } else if (firstDir.equalsIgnoreCase("newmusic")) {
            sound.addMusic(id, url);
        }
    }

    @Override
    public double getFrameTime() {
        return this.deltaTime;
    }

    @Override
    public void updateStoreGUI() {
        var scaler = new ScreenSizeCalculator(this.options, this.actualWidth, this.actualHeight);
        int width = scaler.getWidth();
        int height = scaler.getHeight();
        this.storeGUI.init((Minecraft) (Object) this, width, height);
    }

    @Override
    public boolean isCameraActive() {
        return this.cameraActive;
    }

    @Override
    public void setCameraActive(boolean value) {
        this.cameraActive = value;
    }

    @Override
    public boolean isCameraPause() {
        return this.cameraPause;
    }

    @Override
    public void setCameraPause(boolean value) {
        this.cameraPause = value;
    }

    @Override
    public AC_CutsceneCamera getCutsceneCamera() {
        return this.cutsceneCamera;
    }

    @Override
    public void setCutsceneCamera(AC_CutsceneCamera value) {
        this.cutsceneCamera = value;
    }

    @Override
    public AC_CutsceneCamera getActiveCutsceneCamera() {
        return this.activeCutsceneCamera;
    }

    @Override
    public void setActiveCutsceneCamera(AC_CutsceneCamera value) {
        this.activeCutsceneCamera = value;
    }

    @Override
    public LivingEntity getCutsceneCameraEntity() {
        return this.cutsceneCameraEntity;
    }

    @Override
    public AC_GuiStore getStoreGUI() {
        return this.storeGUI;
    }

    @Override
    public AC_MapList getMapList() {
        return this.mapList;
    }
}
