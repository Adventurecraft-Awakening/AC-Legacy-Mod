package dev.adventurecraft.awakening.mixin.client;

import dev.adventurecraft.awakening.ACMainThread;
import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.client.options.Config;
import dev.adventurecraft.awakening.common.*;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.client.ExMinecraft;
import dev.adventurecraft.awakening.extension.client.entity.player.ExAbstractClientPlayerEntity;
import dev.adventurecraft.awakening.extension.client.gui.screen.ExScreen;
import dev.adventurecraft.awakening.extension.client.render.ExWorldEventRenderer;
import dev.adventurecraft.awakening.extension.entity.player.ExPlayerEntity;
import dev.adventurecraft.awakening.extension.inventory.ExPlayerInventory;
import dev.adventurecraft.awakening.extension.item.ExItem;
import dev.adventurecraft.awakening.extension.item.ExItemStack;
import dev.adventurecraft.awakening.extension.world.ExWorld;
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
import net.minecraft.client.render.HeldItemRenderer;
import net.minecraft.client.render.Tessellator;
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
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.PixelFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.*;

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

    private static long[] updateTimes = new long[512];
    private static long updateRendererTime;

    private int rightMouseTicksRan;
    public AC_MapList mapList;
    public int nextFrameTime;
    public long prevFrameTimeForAvg;
    public long[] tFrameTimes = new long[60];
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
        Arguments arguments = new Arguments();
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

        File gameDir = new File(arguments.getOrDefault("gameDir", "."));

        boolean doConnect = arguments.containsKey("server") && arguments.containsKey("port");
        String host = "";
        String port = "";

        if (doConnect) {
            host = arguments.get("server");
            port = arguments.get("port");
        }

        boolean fullscreen = arguments.getExtraArgs().contains("--fullscreen");
        int width = Integer.parseInt(arguments.getOrDefault("width", "854"));
        int height = Integer.parseInt(arguments.getOrDefault("height", "480"));

        ACMainThread acThread = new ACMainThread(width, height, fullscreen);
        ACMainThread.gameDirectory = gameDir;
        acThread.minecraftUrl = "www.minecraft.net";
        acThread.session = new Session(username, sessionId);
        if (doConnect) {
            acThread.setIpPort(host, Integer.parseInt(port));
        }

        Thread thread = new Thread(acThread, "Minecraft main thread");
        thread.setPriority(10);
        thread.start();
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

    @Redirect(method = "init", at = @At(
            value = "INVOKE",
            target = "Lorg/lwjgl/opengl/Display;create()V",
            remap = false,
            ordinal = 0))
    private void init_disableOriginalDisplay() {
    }

    @Inject(method = "init", at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;options:Lnet/minecraft/client/options/GameOptions;",
            shift = At.Shift.AFTER,
            ordinal = 0))
    private void init_createDisplay(CallbackInfo ci) throws LWJGLException {
        if (Config.isMultiTexture()) {
            int sampleCount = Config.getAntialiasingLevel();
            ACMod.LOGGER.info("MSAA Samples: " + sampleCount);

            try {
                createDisplay(new PixelFormat().withSamples(sampleCount), true);
                return;
            } catch (LWJGLException ex) {
                ACMod.LOGGER.warn("Error setting MSAA: " + sampleCount + "x: ", ex);
            }
        }

        createDisplay(new PixelFormat(), false);
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

        Config.logOpenGlCaps();
    }

    @Inject(method = "init", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/TexturePackManager;<init>(Lnet/minecraft/client/Minecraft;Ljava/io/File;)V",
            shift = At.Shift.AFTER))
    private void init_createMapList(CallbackInfo ci) {
        this.mapList = new AC_MapList(this.gameDir);
    }

    @Inject(method = "init", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/texture/TextureManager;addTextureBinder(Lnet/minecraft/client/render/TextureBinder;)V",
            ordinal = 0))
    private void init_addFanTextureBinder(CallbackInfo ci) {
        this.textureManager.addTextureBinder(new AC_TextureFanFX());
    }

    @Inject(method = "run", at = @At(
            value = "INVOKE",
            target = "Ljava/lang/System;currentTimeMillis()J",
            shift = At.Shift.BEFORE,
            ordinal = 0,
            remap = false),
            remap = false)
    private void run_setup(CallbackInfo ci) {
        this.textureManager.getTextureId("/terrain.png");
        this.textureManager.getTextureId("/terrain2.png");
        this.textureManager.getTextureId("/terrain3.png");
        //ContextFactory.initGlobal(new ContextFactory()); TODO
    }

    @Redirect(method = "run", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/sound/SoundHelper;setSoundPosition(Lnet/minecraft/entity/LivingEntity;F)V"))
    private void run_setSoundListenerPos(SoundHelper instance, LivingEntity f, float v) {
        if (this.cameraActive) {
            instance.setSoundPosition(this.cutsceneCameraEntity, v);
        } else {
            instance.setSoundPosition(f, v);
        }
    }

    @Inject(method = "run", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Minecraft;printOpenGLError(Ljava/lang/String;)V",
            shift = At.Shift.AFTER,
            ordinal = 1))
    private void run_updateFrameTimes(CallbackInfo ci) {
        this.prevFrameTimeForAvg = System.nanoTime();
        this.tFrameTimes[this.nextFrameTime] = this.prevFrameTimeForAvg;
        this.nextFrameTime = (this.nextFrameTime + 1) % 60;
    }

    @Inject(method = "run", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Minecraft;stop()V",
            shift = At.Shift.BEFORE))
    private void run_stop(CallbackInfo ci) {
        /* TODO
        ContextFactory.getGlobal().enterContext();
        Context.exit();
         */
    }

    @Inject(method = "run", at = @At(
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

    @Redirect(method = "run", remap = false, at = @At(
            value = "INVOKE",
            target = "Lorg/lwjgl/opengl/Display;isActive()Z",
            remap = false))
    private boolean disableDoubleToggle() {
        return true;
    }

    @Inject(method = "method_2111", at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;lastFrameRenderTime:J",
            shift = At.Shift.BEFORE,
            ordinal = 2))
    private void setUpdateTime(long var1, CallbackInfo ci) {
        updateTimes[frameRenderTimesAmount & updateTimes.length - 1] = updateRendererTime;
    }

    @Inject(method = "method_2111", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/Tessellator;addVertex(DDD)V",
            shift = At.Shift.BEFORE,
            ordinal = 12),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void renderUpdateTime(long var1, CallbackInfo ci, long var3, long var5, Tessellator var7, int var8, long var9, int var11, int var12, int var13, int var14, int var15, long var16, long var18) {
        long updateTime = updateTimes[var12] / 200000L;
        var7.color(var14 * 1);
        var7.addVertex((float) var12 + 0.5F, (float) ((long) this.actualHeight - (var16 - var18)) + 0.5F, 0.0D);
        var7.addVertex((float) var12 + 0.5F, (float) ((long) this.actualHeight - (var16 - var18 - updateTime)) + 0.5F, 0.0D);
    }

    @Redirect(method = "toggleFullscreen", at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;width:I"))
    private int fix_getWidthAfterFullscreen(Minecraft instance) {
        return Display.getWidth();
    }

    @Redirect(method = "toggleFullscreen", at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;height:I"))
    private int fix_getHeightAfterFullscreen(Minecraft instance) {
        return Display.getHeight();
    }

    @Inject(method = "toggleFullscreen", at = @At(
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
        int var3;
        if (this.player != null) {
            WorldSource var1 = this.world.getCache();
            if (var1 instanceof ChunkCache) {
                ChunkCache var2 = (ChunkCache) var1;
                var3 = MathHelper.floor((float) ((int) this.player.x)) >> 4;
                int var4 = MathHelper.floor((float) ((int) this.player.z)) >> 4;
                var2.method_1242(var3, var4);
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
        }

        if (this.currentScreen != null && !((ExScreen) this.currentScreen).isDisabledInputGrabbing()) {
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
                        if (!Mouse.next()) {
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
                                                ((ExWorld) this.world).loadBrightness();
                                            }

                                            ((ExWorldEventRenderer) this.worldRenderer).updateAllTheRenderers();
                                        }

                                        if (Keyboard.getEventKey() == Keyboard.KEY_F5) {
                                            this.options.thirdPerson = !this.options.thirdPerson;
                                        }

                                        if (Keyboard.getEventKey() == Keyboard.KEY_F6) {
                                            ((ExWorldEventRenderer) this.worldRenderer).resetAll();
                                            this.overlay.addChatMessage("Resetting all blocks in loaded chunks");
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
                                                ((ExWorld) this.world).undo();
                                            } else if (Keyboard.getEventKey() == Keyboard.KEY_Y) {
                                                ((ExWorld) this.world).redo();
                                            }
                                        }
                                    }

                                    int var8 = 0;

                                    while (true) {
                                        if (var8 >= 9) {
                                            if (Keyboard.getEventKey() == this.options.fogKey.key) {
                                                this.options.setIntOption(Option.RENDER_DISTANCE, !Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && !Keyboard.isKeyDown(Keyboard.KEY_RSHIFT) ? 1 : -1);
                                            }
                                            break;
                                        }

                                        if (Keyboard.getEventKey() == Keyboard.KEY_1 + var8) {
                                            if (!Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && !Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) {
                                                if (var8 == ((ExPlayerInventory) this.player.inventory).getOffhandItem()) {
                                                    ((ExPlayerInventory) this.player.inventory).setOffhandItem(this.player.inventory.selectedHotBarSlot);
                                                }

                                                this.player.inventory.selectedHotBarSlot = var8;
                                            } else {
                                                if (var8 == this.player.inventory.selectedHotBarSlot) {
                                                    this.player.inventory.selectedHotBarSlot = ((ExPlayerInventory) this.player.inventory).getOffhandItem();
                                                }

                                                ((ExPlayerInventory) this.player.inventory).setOffhandItem(var8);
                                            }
                                        }

                                        ++var8;
                                    }
                                }

                                if (this.world != null) {
                                    //((ExWorld) this.world).getScript().keyboard.processKeyPress(Keyboard.getEventKey());
                                }
                            }
                        }

                        var7 = System.currentTimeMillis() - this.lastTickTime;
                    } while (var7 > 200L);

                    var3 = Mouse.getEventDWheel();
                    if (var3 != 0) {
                        boolean var9 = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
                        boolean var5 = Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU);
                        if (var3 > 0) {
                            var3 = 1;
                        }

                        if (var3 < 0) {
                            var3 = -1;
                        }

                        if (AC_DebugMode.active && var5) {
                            AC_DebugMode.reachDistance += var3;
                            AC_DebugMode.reachDistance = Math.min(Math.max(AC_DebugMode.reachDistance, 2), 100);
                            this.overlay.addChatMessage(String.format("Reach Changed to %d", AC_DebugMode.reachDistance));
                        } else {
                            int var6;
                            if (var9) {
                                var6 = this.player.inventory.selectedHotBarSlot;
                                this.player.inventory.selectedHotBarSlot = ((ExPlayerInventory) this.player.inventory).getOffhandItem();
                                ((ExPlayerInventory) this.player.inventory).setOffhandItem(var6);
                            }

                            this.player.inventory.scrollInHotBar(var3);
                            if (var9) {
                                var6 = this.player.inventory.selectedHotBarSlot;
                                this.player.inventory.selectedHotBarSlot = ((ExPlayerInventory) this.player.inventory).getOffhandItem();
                                ((ExPlayerInventory) this.player.inventory).setOffhandItem(var6);
                            }

                            if (this.options.field_1445) {
                                this.options.field_1448 += (float) var3 * 0.25F;
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
                this.world.method_294(MathHelper.floor(this.player.x), MathHelper.floor(this.player.y), MathHelper.floor(this.player.z));
            }

            if (!this.paused) {
                this.particleManager.method_320();
            }
        }

        this.lastTickTime = System.currentTimeMillis();
    }

    @Redirect(method = {"method_2110", "lockCursor"}, at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;attackCooldown:I",
            ordinal = 0))
    private void keepAttackCooldown(Minecraft instance, int value) {
    }

    @Overwrite
    private void method_2107(int var1) {
        if (var1 != 0 || this.attackCooldown <= 0) {
            if (AC_DebugMode.active) {
                ((ExWorld) this.world).getUndoStack().startRecording();
            }

            boolean var4 = false;
            ItemStack var2 = this.player.inventory.getHeldItem();
            if (!AC_DebugMode.active) {
                if (var1 == 0) {
                    var2 = ((ExPlayerInventory) this.player.inventory).getOffhandItemStack();
                    ((ExPlayerInventory) this.player.inventory).swapOffhandWithMain();
                    var4 = true;
                    ((ExPlayerEntity) this.player).setSwappedItems(true);
                }

                int var5 = 5;
                if (var2 != null) {
                    var5 = ((ExItem) Item.byId[var2.itemId]).getItemUseDelay();
                }

                if (var1 == 0) {
                    this.mouseTicksProcessed = this.ticksPlayed + var5;
                } else {
                    this.rightMouseTicksRan = this.ticksPlayed + var5;
                }

                if (var2 != null && ((ExItem) Item.byId[var2.itemId]).mainActionLeftClick()) {
                    var1 = 0;
                } else {
                    var1 = 1;
                }
            } else {
                this.mouseTicksProcessed = this.ticksPlayed + 5;
                this.rightMouseTicksRan = this.ticksPlayed + 5;
            }

            if (var1 == 0) {
                this.player.swingHand();
            }

            boolean var12 = true;
            if (this.hitResult == null) {
                if (var1 == 0 && !(this.interactionManager instanceof CreativeClientInteractionManager)) {
                    this.attackCooldown = 10;
                }
            } else if (this.hitResult.type == HitType.field_790) {
                if (var1 == 0) {
                    this.interactionManager.attack(this.player, this.hitResult.field_1989);
                }

                if (var1 == 1) {
                    this.interactionManager.method_1714(this.player, this.hitResult.field_1989);
                }
            } else if (this.hitResult.type == HitType.field_789) {
                int var6 = this.hitResult.x;
                int var7 = this.hitResult.y;
                int var8 = this.hitResult.z;
                int var9 = this.hitResult.field_1987;
                Block var10 = Block.BY_ID[this.world.getBlockId(var6, var7, var8)];
                if (!AC_DebugMode.active && (var10.id == Block.CHEST.id || var10.id == AC_Blocks.store.id)) {
                    var1 = 1;
                }

                if (var10 != null) {
                    int var11;
                    if (!AC_DebugMode.active) {
                        var11 = ((ExBlock) var10).alwaysUseClick(this.world, var6, var7, var8);
                        if (var11 != -1) {
                            var1 = var11;
                        }
                    }

                    if (var1 == 0) {
                        this.interactionManager.destroyFireAndBreakBlock(var6, var7, var8, this.hitResult.field_1987);
                        if (var2 != null) {
                            ((ExItemStack) (Object) var2).useItemLeftClick(this.player, this.world, var6, var7, var8, var9);
                        }
                    } else {
                        var11 = var2 == null ? 0 : var2.count;
                        if (this.interactionManager.useItemOnBlock(this.player, this.world, var2, var6, var7, var8, var9)) {
                            var12 = false;
                            this.player.swingHand();
                        }

                        if (var2 == null) {
                            if (var4) {
                                ((ExPlayerInventory) this.player.inventory).swapOffhandWithMain();
                                ((ExPlayerEntity) this.player).setSwappedItems(false);
                            }

                            if (AC_DebugMode.active) {
                                ((ExWorld) this.world).getUndoStack().stopRecording();
                            }

                            return;
                        }

                        if (var2.count == 0 && var2 == this.player.inventory.main[this.player.inventory.selectedHotBarSlot]) {
                            this.player.inventory.main[this.player.inventory.selectedHotBarSlot] = null;
                        } else if (var2.count != var11) {
                            this.gameRenderer.heldItemRenderer.method_1863();
                        }
                    }
                }
            }

            if (var12 && var1 == 0 && var2 != null && Item.byId[var2.itemId] != null) {
                ((ExItem) Item.byId[var2.itemId]).onItemLeftClick(var2, this.world, this.player);
            }

            if (var12 && var1 == 1 && var2 != null && this.interactionManager.method_1712(this.player, this.world, var2)) {
                this.gameRenderer.heldItemRenderer.method_1865();
            }

            /* TODO
            if (var2 != null) {
                Object var13;
                if (this.lastItemUsed != var2) {
                    var13 = Context.javaToJS(new ScriptItem(var2), this.world.script.globalScope);
                    ScriptableObject.putProperty(this.world.script.globalScope, "lastItemUsed", var13);
                    this.lastItemUsed = var2;
                }

                if (this.hitResult == null) {
                    if (this.lastEntityHit != null) {
                        this.lastEntityHit = null;
                        var13 = Context.javaToJS((Object) null, this.world.script.globalScope);
                        ScriptableObject.putProperty(this.world.script.globalScope, "hitEntity", var13);
                    }

                    if (this.lastBlockHit != null) {
                        this.lastBlockHit = null;
                        var13 = Context.javaToJS((Object) null, this.world.script.globalScope);
                        ScriptableObject.putProperty(this.world.script.globalScope, "hitBlock", var13);
                    }
                } else if (this.hitResult.type == HitType.field_790) {
                    if (this.lastEntityHit != this.hitResult.field_1989) {
                        this.lastEntityHit = this.hitResult.field_1989;
                        var13 = Context.javaToJS(ScriptEntity.getEntityClass(this.hitResult.field_1989), this.world.script.globalScope);
                        ScriptableObject.putProperty(this.world.script.globalScope, "hitEntity", var13);
                    }

                    if (this.lastBlockHit != null) {
                        this.lastBlockHit = null;
                        var13 = Context.javaToJS((Object) null, this.world.script.globalScope);
                        ScriptableObject.putProperty(this.world.script.globalScope, "hitBlock", var13);
                    }
                } else if (this.hitResult.type != HitType.field_789) {
                    if (this.lastEntityHit != null) {
                        this.lastEntityHit = null;
                        var13 = Context.javaToJS((Object) null, this.world.script.globalScope);
                        ScriptableObject.putProperty(this.world.script.globalScope, "hitEntity", var13);
                    }

                    if (this.lastBlockHit != null) {
                        this.lastBlockHit = null;
                        var13 = Context.javaToJS((Object) null, this.world.script.globalScope);
                        ScriptableObject.putProperty(this.world.script.globalScope, "hitBlock", var13);
                    }
                } else {
                    if (this.lastBlockHit == null || this.lastBlockHit.x != (double) this.hitResult.x || this.lastBlockHit.y != (double) this.hitResult.y || this.lastBlockHit.z != (double) this.hitResult.z) {
                        this.lastBlockHit = new ScriptVec3((float) this.hitResult.x, (float) this.hitResult.y, (float) this.hitResult.z);
                        var13 = Context.javaToJS(this.lastBlockHit, this.world.script.globalScope);
                        ScriptableObject.putProperty(this.world.script.globalScope, "hitBlock", var13);
                    }

                    if (this.lastEntityHit != null) {
                        this.lastEntityHit = null;
                        var13 = Context.javaToJS((Object) null, this.world.script.globalScope);
                        ScriptableObject.putProperty(this.world.script.globalScope, "hitEntity", var13);
                    }
                }

                if (var2.usesMeta()) {
                    this.world.scriptHandler.runScript(String.format("item_%d_%d.js", new Object[]{var2.itemId, var2.getMeta()}), this.world.scope, false);
                } else {
                    this.world.scriptHandler.runScript(String.format("item_%d.js", new Object[]{var2.itemId}), this.world.scope, false);
                }
            }
            */

            if (var4) {
                ((ExPlayerInventory) this.player.inventory).swapOffhandWithMain();
                ((ExPlayerEntity) this.player).setSwappedItems(false);
            }

            if (AC_DebugMode.active) {
                ((ExWorld) this.world).getUndoStack().stopRecording();
            }
        }
    }

    @Inject(method = "initWorld", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/ClientInteractionManager;onInitWorld(Lnet/minecraft/world/World;)V",
            shift = At.Shift.BEFORE))
    private void loadMapTexOnInit(World var1, String var2, PlayerEntity var3, CallbackInfo ci) {
        ((ExWorld) this.world).loadMapTextures();
    }

    @Inject(method = "initWorld", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/ClientInteractionManager;rotatePlayer(Lnet/minecraft/entity/player/PlayerEntity;)V",
            shift = At.Shift.AFTER))
    private void initPlayerOnInit(World var1, String var2, PlayerEntity var3, CallbackInfo ci) {
        this.cutsceneCameraEntity = this.interactionManager.method_1717(var1);
        //((ExWorld) this.world.getScript()).initPlayer(); TODO
    }

    @Overwrite
    public void spawn(boolean var1, int var2) {
        if (!this.world.isClient && !this.world.dimension.canPlayerSleep()) {
            this.switchDimension();
        }

        Vec3i var3 = this.world.getSpawnPosition();
        WorldSource var4 = this.world.getCache();
        if (var4 instanceof ChunkCache var5) {
            var5.method_1242(var3.x >> 4, var3.z >> 4);
        }

        this.world.method_295();
        int var7 = 0;
        if (this.player != null) {
            var7 = this.player.entityId;
            this.world.removeEntity(this.player);
        } else {
            this.player = (AbstractClientPlayerEntity) this.interactionManager.method_1717(this.world);
            //((ExWorld)this.world).getScript().initPlayer(); TODO
        }

        ((ExWorldEventRenderer) this.worldRenderer).resetForDeath();
        Vec3i var6 = this.world.getSpawnPosition();
        this.player.afterSpawn();
        this.player.setPositionAndAngles((double) var6.x + 0.5D, var6.y, (double) var6.z + 0.5D, 0.0F, 0.0F);
        this.viewEntity = this.player;
        this.player.afterSpawn();
        this.interactionManager.rotatePlayer(this.player);
        this.world.addPlayer(this.player);
        this.player.playerKeypressManager = new MovementManager(this.options);
        this.player.entityId = var7;
        this.player.method_494();
        this.player.setRotation(((ExWorld) this.world).getSpawnYaw(), 0.0F);
        this.interactionManager.setDefaultHotbar(this.player);
        this.loadIntoWorld("Respawning");
        if (this.currentScreen instanceof DeathScreen) {
            this.openScreen(null);
        }
    }

    @Overwrite
    public void createOrLoadWorld(String var1, String var2, long var3) {
        String var5 = this.getMapUsed(var1);
        if (MathHelper.isStringEmpty(var5)) {
            this.openScreen(new AC_GuiMapSelect(null, var1));
        } else {
            this.startWorld(var1, var2, var3, var5);
        }
    }

    @Override
    public World getWorld(String var1, long var2, String var4) {
        this.setWorld(null);
        DimensionData var5 = this.getWorldStorage().method_1009(var1, false);
        World var6 = ExWorld.createWorld(var4, var5, var1, var2);
        return var6;
    }

    @Override
    public void startWorld(String var1, String var2, long var3, String var5) {
        this.setWorld(null);
        System.gc();
        if (var1 != null && this.getWorldStorage().isOld(var1)) {
            this.convertWorldFormat(var1, var2);
        } else {
            AC_DebugMode.active = false;
            AC_DebugMode.levelEditing = false;
            DimensionData var6 = null;
            if (var1 != null) {
                var6 = this.getWorldStorage().method_1009(var1, false);
            }

            if (var2 == null) {
                var2 = "Map Editing";
            }

            World var7 = ExWorld.createWorld(var5, var6, var2, var3);
            if (var7.field_215) {
                this.statFileWriter.incrementStat(Stats.createWorld, 1);
                this.statFileWriter.incrementStat(Stats.startGame, 1);
                this.notifyStatus(var7, "Generating level");
            } else {
                this.statFileWriter.incrementStat(Stats.loadWorld, 1);
                this.statFileWriter.incrementStat(Stats.startGame, 1);
                this.notifyStatus(var7, "Loading level");
            }
        }

        this.openScreen(null);
    }

    @Override
    public String getMapUsed(String var1) {
        File var2 = getGameDirectory();
        File var3 = new File(var2, "saves");
        File var4 = new File(var3, var1);
        File var5 = new File(var4, "map.txt");
        if (var5.exists()) {
            try {
                BufferedReader var7 = new BufferedReader(new FileReader(var5));
                String var6 = var7.readLine();
                var7.close();
                return var6;
            } catch (FileNotFoundException var8) {
            } catch (IOException var9) {
            }
        }

        return null;
    }

    @Override
    public void saveMapUsed(String var1, String var2) {
        File var3 = getGameDirectory();
        File var4 = new File(var3, "saves");
        File var5 = new File(var4, var1);
        var5.mkdirs();
        File var6 = new File(var5, "map.txt");

        try {
            if (var6.exists()) {
                var6.delete();
            }

            var6.createNewFile();
            BufferedWriter var7 = new BufferedWriter(new FileWriter(var6));
            var7.write(var2);
            var7.close();
        } catch (FileNotFoundException var8) {
        } catch (IOException var9) {
        }
    }

    @Override
    public long getAvgFrameTime() {
        return this.tFrameTimes[this.nextFrameTime] != 0L ? (this.prevFrameTimeForAvg - this.tFrameTimes[this.nextFrameTime]) / 60L : 23333333L;
    }

    @Override
    public void updateStoreGUI() {
        ScreenScaler var1 = new ScreenScaler(this.options, this.actualWidth, this.actualHeight);
        int var2 = var1.getScaledWidth();
        int var3 = var1.getScaledHeight();
        this.storeGUI.init((Minecraft) (Object) this, var2, var3);
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