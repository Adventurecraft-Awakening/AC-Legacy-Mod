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
import dev.adventurecraft.awakening.script.ScriptEntity;
import dev.adventurecraft.awakening.script.ScriptItem;
import dev.adventurecraft.awakening.script.ScriptVec3;
import net.fabricmc.loader.impl.util.Arguments;
import net.minecraft.block.Block;
import net.minecraft.client.ClientInteractionManager;
import net.minecraft.client.CreativeClientInteractionManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MovementManager;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.gui.InGameHud;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.container.PlayerInventoryScreen;
import net.minecraft.client.gui.screen.ingame.ChatScreen;
import net.minecraft.client.gui.screen.ingame.DeathScreen;
import net.minecraft.client.gui.screen.ingame.SleepingScreen;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.Option;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.WorldEventRenderer;
import net.minecraft.client.sound.SoundHelper;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.ScreenScaler;
import net.minecraft.client.util.Session;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stat.Stats;
import net.minecraft.util.ProgressListenerImpl;
import net.minecraft.util.Vec3i;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitType;
import net.minecraft.util.io.StatsFileWriter;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkCache;
import net.minecraft.world.dimension.DimensionData;
import net.minecraft.world.source.WorldSource;
import net.minecraft.world.storage.WorldStorage;
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
import java.util.Objects;

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
    public InGameHud overlay;

    @Shadow
    public AbstractClientPlayerEntity player;

    @Shadow
    public World world;

    @Shadow
    public volatile boolean paused;

    @Shadow
    public ClientInteractionManager interactionManager;

    @Shadow
    public TextureManager textureManager;

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
    public GameOptions options;

    @Shadow
    public WorldEventRenderer worldRenderer;

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
    public ParticleManager particleManager;

    @Shadow
    protected abstract void startLoginThread();

    @Shadow
    public abstract void switchDimension();

    @Shadow
    public LivingEntity viewEntity;

    @Shadow
    protected abstract void loadIntoWorld(String string);

    @Shadow
    public abstract WorldStorage getWorldStorage();

    @Shadow
    public abstract void setWorld(World arg);

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
    public StatsFileWriter statFileWriter;

    @Shadow
    public abstract void notifyStatus(World arg, String string);

    @Shadow
    public ProgressListenerImpl progressListener;

    @Shadow
    public SoundHelper soundHelper;

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
    ItemStack lastItemUsed;
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

        ACMainThread.gameDirectory = new File(arguments.getOrDefault("gameDir", "."));

        if (arguments.containsKey("mapsDir")) {
            ACMainThread.mapsDirectory = new File(arguments.get("mapsDir"));
        }

        ACMainThread.glDebug = arguments.containsKey("glDebug") || arguments.getExtraArgs().contains("--glDebug");
        ACMainThread.glDebugTrace = Objects.equals(arguments.get("glDebug"), "trace");

        boolean fullscreen = arguments.getExtraArgs().contains("--fullscreen");
        int width = Integer.parseInt(arguments.getOrDefault("width", "854"));
        int height = Integer.parseInt(arguments.getOrDefault("height", "480"));

        var acThread = new ACMainThread(width, height, fullscreen);
        acThread.minecraftUrl = "www.minecraft.net";
        acThread.session = new Session(username, sessionId);

        boolean doConnect = arguments.containsKey("server") && arguments.containsKey("port");
        if (doConnect) {
            String host = arguments.get("server");
            String port = arguments.get("port");
            acThread.setIpPort(host, Integer.parseInt(port));
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

        if (ACMainThread.glDebug) {
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
        this.textureManager.addTextureBinder(new AC_TextureFanFX());
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
        this.textureManager.getTextureId("/terrain.png");
        this.textureManager.getTextureId("/terrain2.png");
        this.textureManager.getTextureId("/terrain3.png");
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
    private void run_setSoundListenerPos(SoundHelper instance, LivingEntity f, float v) {
        if (this.cameraActive) {
            instance.setSoundPosition(this.cutsceneCameraEntity, v);
        } else {
            instance.setSoundPosition(f, v);
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

    @Overwrite
    public void tick() {
        if (this.ticksPlayed == 6000) {
            this.startLoginThread();
        }

        this.overlay.runTick();
        this.gameRenderer.method_1838(1.0F);

        if (this.player != null) {
            WorldSource worldSource = this.world.getCache();
            if (worldSource instanceof ChunkCache chunkCache) {
                int chunkX = MathHelper.floor((float) ((int) this.player.x)) >> 4;
                int chunkZ = MathHelper.floor((float) ((int) this.player.z)) >> 4;
                chunkCache.method_1242(chunkX, chunkZ);
            }
        }

        if (!this.paused && this.world != null) {
            this.interactionManager.tick();
        }

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.textureManager.getTextureId("/terrain.png"));
        if (!this.paused) {
            this.textureManager.tick();
        }

        if (this.currentScreen == null && this.player != null) {
            if (this.player.health <= 0) {
                this.openScreen(null);
            } else if (this.player.isLyingOnBed() && this.world != null && this.world.isClient) {
                this.openScreen(new SleepingScreen());
            }
        } else if (this.currentScreen != null && this.currentScreen instanceof SleepingScreen && !this.player.isLyingOnBed()) {
            this.openScreen(null);
        }

        if (this.currentScreen != null && !((ExScreen) this.currentScreen).isDisabledInputGrabbing()) {
            this.mouseTicksProcessed = this.ticksPlayed + 10000;

            this.currentScreen.method_130();
            if (this.currentScreen != null) {
                this.currentScreen.smokeRenderer.render();
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

                                this.player.method_136(Keyboard.getEventKey(), Keyboard.getEventKeyState());
                            } while (!Keyboard.getEventKeyState());

                            if (Keyboard.getEventKey() == Keyboard.KEY_F11) {
                                this.toggleFullscreen();
                            } else {
                                if (this.currentScreen != null && !((ExScreen) this.currentScreen).isDisabledInputGrabbing()) {
                                    // TODO: fix doubled events (one for key press, one for text input)
                                    this.currentScreen.onKeyboardEvent();
                                } else {
                                    if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
                                        this.openPauseMenu();
                                    }

                                    if (Keyboard.getEventKey() == Keyboard.KEY_S && Keyboard.isKeyDown(Keyboard.KEY_F3)) {
                                        this.forceResourceReload();
                                    }

                                    if (Keyboard.getEventKey() == Keyboard.KEY_F1) {
                                        this.options.hideHud = !this.options.hideHud;
                                    }

                                    if (Keyboard.getEventKey() == Keyboard.KEY_F3) {
                                        this.options.debugHud = !this.options.debugHud;
                                    }

                                    if (Keyboard.getEventKey() == Keyboard.KEY_F4) {
                                        AC_DebugMode.active = !AC_DebugMode.active;
                                        if (AC_DebugMode.active) {
                                            this.overlay.addChatMessage("Debug Mode Active");
                                        } else {
                                            this.overlay.addChatMessage("Debug Mode Deactivated");
                                        }

                                        ((ExWorldEventRenderer) this.worldRenderer).updateAllTheRenderers();
                                    }

                                    if (Keyboard.getEventKey() == Keyboard.KEY_F5) {
                                        this.options.thirdPerson = !this.options.thirdPerson;
                                    }

                                    if (Keyboard.getEventKey() == Keyboard.KEY_F6) {
                                        if (AC_DebugMode.active) {
                                            ((ExWorldEventRenderer) this.worldRenderer).resetAll();
                                            this.overlay.addChatMessage("Resetting all blocks in loaded chunks");
                                        }
                                    }

                                    if (Keyboard.getEventKey() == Keyboard.KEY_F7) {
                                        ((ExAbstractClientPlayerEntity) this.player).displayGUIPalette();
                                    }

                                    if (Keyboard.getEventKey() == this.options.inventoryKey.key) {
                                        this.openScreen(new PlayerInventoryScreen(this.player));
                                    }

                                    if (Keyboard.getEventKey() == this.options.dropKey.key) {
                                        this.player.dropSelectedItem();
                                    }

                                    if ((this.hasWorld() || AC_DebugMode.active) && Keyboard.getEventKey() == this.options.chatKey.key) {
                                        this.openScreen(new ChatScreen());
                                    }

                                    if (AC_DebugMode.active && (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL))) {
                                        if (Keyboard.getEventKey() == Keyboard.KEY_Z) {
                                            ServerCommands.cmdUndo(new ServerCommandSource(
                                                (Minecraft) (Object) this, this.world, this.player), null);
                                        } else if (Keyboard.getEventKey() == Keyboard.KEY_Y) {
                                            ServerCommands.cmdRedo(new ServerCommandSource(
                                                (Minecraft) (Object) this, this.world, this.player), null);
                                        }
                                    }
                                }

                                int currentSlot = 0;

                                while (true) {
                                    if (currentSlot >= 9) {
                                        if (Keyboard.getEventKey() == this.options.fogKey.key) {
                                            this.options.setIntOption(Option.RENDER_DISTANCE, !Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && !Keyboard.isKeyDown(Keyboard.KEY_RSHIFT) ? 1 : -1);
                                        }
                                        break;
                                    }

                                    if (Keyboard.getEventKey() == Keyboard.KEY_1 + currentSlot) {
                                        if (!Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && !Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) {
                                            if (currentSlot == ((ExPlayerInventory) this.player.inventory).getOffhandItem()) {
                                                ((ExPlayerInventory) this.player.inventory).setOffhandItem(this.player.inventory.selectedHotBarSlot);
                                            }

                                            this.player.inventory.selectedHotBarSlot = currentSlot;
                                        } else {
                                            if (currentSlot == this.player.inventory.selectedHotBarSlot) {
                                                this.player.inventory.selectedHotBarSlot = ((ExPlayerInventory) this.player.inventory).getOffhandItem();
                                            }

                                            ((ExPlayerInventory) this.player.inventory).setOffhandItem(currentSlot);
                                        }
                                    }

                                    ++currentSlot;
                                }
                            }

                            if (this.world != null) {
                                ((ExWorld) this.world).getScript().keyboard.processKeyPress(Keyboard.getEventKey());
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
                            this.overlay.addChatMessage(String.format("Reach Changed to %d", AC_DebugMode.reachDistance));
                        } else {
                            if (ctrlDown) {
                                int selectedSlot = this.player.inventory.selectedHotBarSlot;
                                this.player.inventory.selectedHotBarSlot = ((ExPlayerInventory) this.player.inventory).getOffhandItem();
                                ((ExPlayerInventory) this.player.inventory).setOffhandItem(selectedSlot);
                            }

                            this.player.inventory.scrollInHotBar(wheelDelta);
                            if (ctrlDown) {
                                int selectedSlot = this.player.inventory.selectedHotBarSlot;
                                this.player.inventory.selectedHotBarSlot = ((ExPlayerInventory) this.player.inventory).getOffhandItem();
                                ((ExPlayerInventory) this.player.inventory).setOffhandItem(selectedSlot);
                            }

                            if (this.options.field_1445) {
                                this.options.field_1448 += (float) wheelDelta * 0.25F;
                            }
                        }
                    }

                    if (this.currentScreen != null && !((ExScreen) this.currentScreen).isDisabledInputGrabbing()) {
                        if (this.currentScreen != null) {
                            this.currentScreen.onMouseEvent();
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
                    this.world.method_287(this.player);
                }
            }

            this.world.difficulty = this.options.difficulty;
            if (this.world.isClient) {
                this.world.difficulty = 3;
            }

            if (!this.paused) {
                this.gameRenderer.method_1837();
            }

            if (!this.paused) {
                this.worldRenderer.method_1557();
            }

            if (!this.paused) {
                if (this.world.field_210 > 0) {
                    --this.world.field_210;
                }

                this.world.method_227();
            }

            if (!this.paused || this.hasWorld()) {
                this.world.method_196(this.options.difficulty > 0, true);
                this.world.method_242();
            }

            if (!this.paused && this.world != null) {
                this.world.method_294(
                    MathHelper.floor(this.player.x),
                    MathHelper.floor(this.player.y),
                    MathHelper.floor(this.player.z));
            }

            if (!this.paused) {
                this.particleManager.method_320();
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
        ItemStack stack = this.player.inventory.getHeldItem();
        if (!AC_DebugMode.active) {
            if (mouseButton == 0) {
                stack = ((ExPlayerInventory) this.player.inventory).getOffhandItemStack();
                ((ExPlayerInventory) this.player.inventory).swapOffhandWithMain();
                swapOffhand = true;
                ((ExPlayerEntity) this.player).setSwappedItems(true);
            }

            int useDelay = 5;
            if (stack != null && Item.byId[stack.itemId] instanceof AC_IUseDelayItem useDelayItem) {
                useDelay = useDelayItem.getItemUseDelay();
            }

            if (mouseButton == 0) {
                this.mouseTicksProcessed = this.ticksPlayed + useDelay;
            } else {
                this.rightMouseTicksRan = this.ticksPlayed + useDelay;
            }

            if (stack != null &&
                (Item.byId[stack.itemId] instanceof AC_ILeftClickItem leftClickItem) &&
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
            this.player.swingHand();
        }

        boolean useOnBlock = true;
        if (this.hitResult == null) {
            if (mouseButton == 0 && !(this.interactionManager instanceof CreativeClientInteractionManager)) {
                this.attackCooldown = 10;
            }
        } else if (this.hitResult.type == HitType.field_790) {
            if (mouseButton == 0) {
                this.interactionManager.attack(this.player, this.hitResult.field_1989);
            }

            if (mouseButton == 1) {
                this.interactionManager.method_1714(this.player, this.hitResult.field_1989);
            }
        } else if (this.hitResult.type == HitType.field_789) {
            int bX = this.hitResult.x;
            int bY = this.hitResult.y;
            int bZ = this.hitResult.z;
            int bSide = this.hitResult.field_1987;
            Block block = Block.BY_ID[this.world.getBlockId(bX, bY, bZ)];
            if (block != null) {
                if (!AC_DebugMode.active && (block.id == Block.CHEST.id || block.id == AC_Blocks.store.id)) {
                    mouseButton = 1;
                }

                if (!AC_DebugMode.active) {
                    int var11 = ((ExBlock) block).alwaysUseClick(this.world, bX, bY, bZ);
                    if (var11 != -1) {
                        mouseButton = var11;
                    }
                }

                if (mouseButton == 0) {
                    this.interactionManager.destroyFireAndBreakBlock(bX, bY, bZ, this.hitResult.field_1987);
                    if (stack != null && Item.byId[stack.itemId] instanceof AC_ILeftClickItem leftClickItem) {
                        leftClickItem.onItemUseLeftClick(stack, this.player, this.world, bX, bY, bZ, bSide);
                    }
                } else {
                    int count = stack == null ? 0 : stack.count;
                    if (this.interactionManager.useItemOnBlock(this.player, this.world, stack, bX, bY, bZ, bSide)) {
                        useOnBlock = false;
                        this.player.swingHand();
                    }

                    if (stack == null) {
                        if (swapOffhand) {
                            ((ExPlayerInventory) this.player.inventory).swapOffhandWithMain();
                            ((ExPlayerEntity) this.player).setSwappedItems(false);
                        }

                        if (AC_DebugMode.active) {
                            exWorld.getUndoStack().stopRecording();
                        }
                        return;
                    }

                    if (stack.count == 0 && stack == this.player.inventory.main[this.player.inventory.selectedHotBarSlot]) {
                        this.player.inventory.main[this.player.inventory.selectedHotBarSlot] = null;
                    } else if (stack.count != count) {
                        this.gameRenderer.heldItemRenderer.method_1863();
                    }
                }
            }
        }

        if (useOnBlock && mouseButton == 0 && stack != null) {
            if (Item.byId[stack.itemId] instanceof AC_ILeftClickItem leftClickItem) {
                leftClickItem.onItemLeftClick(stack, this.world, this.player);
            }
        }

        if (useOnBlock && mouseButton == 1 && stack != null && this.interactionManager.method_1712(this.player, this.world, stack)) {
            this.gameRenderer.heldItemRenderer.method_1865();
        }

        if (stack != null) {
            Scriptable globalScope = exWorld.getScript().globalScope;

            if (this.lastItemUsed != stack) {
                var tmp = Context.javaToJS(new ScriptItem(stack), globalScope);
                ScriptableObject.putProperty(globalScope, "lastItemUsed", tmp);
                this.lastItemUsed = stack;
            }

            if (this.hitResult == null) {
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
            } else if (this.hitResult.type == HitType.field_790) {
                if (this.lastEntityHit != this.hitResult.field_1989) {
                    this.lastEntityHit = this.hitResult.field_1989;
                    var tmp = Context.javaToJS(ScriptEntity.getEntityClass(this.hitResult.field_1989), globalScope);
                    ScriptableObject.putProperty(globalScope, "hitEntity", tmp);
                }

                if (this.lastBlockHit != null) {
                    this.lastBlockHit = null;
                    var tmp = Context.javaToJS(null, globalScope);
                    ScriptableObject.putProperty(globalScope, "hitBlock", tmp);
                }
            } else if (this.hitResult.type != HitType.field_789) {
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

            String scriptName = stack.usesMeta()
                ? String.format("item_%d_%d.js", stack.itemId, stack.getMeta())
                : String.format("item_%d.js", stack.itemId);
            exWorld.getScriptHandler().runScript(scriptName, exWorld.getScope(), false);
        }

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
    private void loadMapTexOnInit(World var1, String var2, PlayerEntity var3, CallbackInfo ci) {
        ((ExWorld) this.world).loadMapTextures();
    }

    @Inject(
        method = "initWorld",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/ClientInteractionManager;rotatePlayer(Lnet/minecraft/entity/player/PlayerEntity;)V",
            shift = At.Shift.AFTER))
    private void initPlayerOnInit(World var1, String var2, PlayerEntity var3, CallbackInfo ci) {
        this.cutsceneCameraEntity = this.interactionManager.method_1717(var1);
        ((ExWorld) this.world).getScript().initPlayer(this.player);
    }

    @Redirect(
        method = "loadIntoWorld",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/ProgressListenerImpl;progressStagePercentage(I)V"))
    private void reportPreciseTerrainProgress(
        ProgressListenerImpl instance,
        int i,
        @Local(ordinal = 1) int count,
        @Local(ordinal = 2) int max) {

        String stage = String.format("%4d / %4d", count, max);
        ((ExProgressListener) this.progressListener).notifyProgress(
            stage, count / (double) max, false);
    }

    @Overwrite
    public void spawn(boolean var1, int var2) {
        if (!this.world.isClient && !this.world.dimension.canPlayerSleep()) {
            this.switchDimension();
        }

        WorldSource worldSource = this.world.getCache();
        if (worldSource instanceof ChunkCache chunkCache) {
            Vec3i spawnPos = this.world.getSpawnPosition();
            chunkCache.method_1242(spawnPos.x >> 4, spawnPos.z >> 4);
        }

        this.world.method_295();
        int playerId = 0;
        if (this.player != null) {
            playerId = this.player.entityId;
            this.world.removeEntity(this.player);
        } else {
            this.player = (AbstractClientPlayerEntity) this.interactionManager.method_1717(this.world);
            ((ExWorld) this.world).getScript().initPlayer(this.player);
        }

        ((ExWorldEventRenderer) this.worldRenderer).resetForDeath();
        Vec3i spawnPos = this.world.getSpawnPosition();
        this.player.afterSpawn();
        this.player.setPositionAndAngles((double) spawnPos.x + 0.5D, spawnPos.y, (double) spawnPos.z + 0.5D, 0.0F, 0.0F);
        this.viewEntity = this.player;
        this.player.afterSpawn();
        this.interactionManager.rotatePlayer(this.player);
        this.world.addPlayer(this.player);
        this.player.playerKeypressManager = new MovementManager(this.options);
        this.player.entityId = playerId;
        this.player.method_494();
        this.player.setRotation(((ExWorld) this.world).getSpawnYaw(), 0.0F);
        this.interactionManager.setDefaultHotbar(this.player);
        this.loadIntoWorld("Respawning");
        if (this.currentScreen instanceof DeathScreen) {
            this.openScreen(null);
        }
    }

    @Overwrite
    public void createOrLoadWorld(String var1, String saveName, long seed) {
        String mapName = this.getMapUsed(var1);
        if (MathHelper.isStringEmpty(mapName)) {
            this.openScreen(new AC_GuiMapSelect(null, var1));
        } else {
            this.startWorld(var1, saveName, seed, mapName);
        }
    }

    @Override
    public World getWorld(String saveName, long seed, String mapName) {
        this.setWorld(null);
        DimensionData dimData = this.getWorldStorage().method_1009(saveName, false);
        World world = ExWorld.createWorld(mapName, dimData, saveName, seed, this.progressListener);
        return world;
    }

    @Override
    public void startWorld(String worldName, String saveName, long seed, String mapName) {
        this.setWorld(null);
        System.gc();
        if (worldName != null && this.getWorldStorage().isOld(worldName)) {
            this.convertWorldFormat(worldName, saveName);
        } else {
            // TODO: reset global state in consistent matter
            AC_DebugMode.active = false;
            AC_DebugMode.levelEditing = false;
            DimensionData dimData = null;
            if (worldName != null) {
                dimData = this.getWorldStorage().method_1009(worldName, false);
            }

            if (saveName == null) {
                saveName = "Map Editing";
            }

            World world = ExWorld.createWorld(mapName, dimData, saveName, seed, this.progressListener);
            if (world.field_215) {
                this.statFileWriter.incrementStat(Stats.createWorld, 1);
                this.statFileWriter.incrementStat(Stats.startGame, 1);
                this.notifyStatus(world, "Generating level");
            } else {
                this.statFileWriter.incrementStat(Stats.loadWorld, 1);
                this.statFileWriter.incrementStat(Stats.startGame, 1);
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
        var scaler = new ScreenScaler(this.options, this.actualWidth, this.actualHeight);
        int width = scaler.getScaledWidth();
        int height = scaler.getScaledHeight();
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
