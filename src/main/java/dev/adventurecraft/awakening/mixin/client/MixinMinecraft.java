package dev.adventurecraft.awakening.mixin.client;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.ACMainThread;
import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.client.gl.GLDevice;
import dev.adventurecraft.awakening.client.options.Config;
import dev.adventurecraft.awakening.common.*;
import dev.adventurecraft.awakening.client.gui.AC_ChatScreen;
import dev.adventurecraft.awakening.common.gui.AC_GuiMapSelect;
import dev.adventurecraft.awakening.common.gui.AC_GuiStore;
import dev.adventurecraft.awakening.client.gui.AC_InBedChatScreen;
import dev.adventurecraft.awakening.entity.player.AdventureGameMode;
import dev.adventurecraft.awakening.entity.player.DebugGameMode;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.client.ExMinecraft;
import dev.adventurecraft.awakening.extension.client.gui.ExInGameHud;
import dev.adventurecraft.awakening.extension.client.gui.screen.ExScreen;
import dev.adventurecraft.awakening.extension.client.options.ExGameOptions;
import dev.adventurecraft.awakening.extension.client.render.ExWorldEventRenderer;
import dev.adventurecraft.awakening.extension.client.sound.ExSoundHelper;
import dev.adventurecraft.awakening.extension.entity.player.ExPlayerEntity;
import dev.adventurecraft.awakening.extension.inventory.ExPlayerInventory;
import dev.adventurecraft.awakening.extension.util.ExProgressListener;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import dev.adventurecraft.awakening.item.AC_ILeftClickItem;
import dev.adventurecraft.awakening.item.AC_IUseDelayItem;
import dev.adventurecraft.awakening.script.ScriptEntity;
import dev.adventurecraft.awakening.script.ScriptItem;
import dev.adventurecraft.awakening.script.ScriptModel;
import dev.adventurecraft.awakening.script.ScriptVec3;
import dev.adventurecraft.awakening.tile.AC_Blocks;
import dev.adventurecraft.awakening.util.MathF;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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
import net.minecraft.client.gui.screens.DeathScreen;
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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
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
import org.lwjgl.opengl.*;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.*;
import java.net.URL;
import java.util.Arrays;
import java.nio.file.Path;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft implements ExMinecraft {

    @Shadow private int orgWidth;
    @Shadow private int orgHeight;
    @Shadow private boolean fullscreen;
    @Shadow public int width;
    @Shadow public int height;

    @Shadow
    protected abstract void resize(int i, int j);

    @Shadow public GameRenderer gameRenderer;
    @Shadow public Gui gui;
    @Shadow public LocalPlayer player;
    @Shadow public Level level;
    @Shadow public volatile boolean pause;
    @Shadow public GameMode gameMode;
    @Shadow public Textures textures;
    @Shadow public Screen screen;

    @Shadow
    public abstract void setScreen(Screen arg);

    @Shadow private int ticks;
    @Shadow private int missTime;

    @Shadow
    protected abstract void handleMouseDown(int i, boolean bl);

    @Shadow
    public abstract void toggleFullScreen();

    @Shadow
    public abstract void pauseGame();

    @Shadow
    protected abstract void reloadSound();

    @Shadow public Options options;
    @Shadow public LevelRenderer levelRenderer;

    @Shadow
    public abstract boolean isOnline();

    @Shadow long lastTickTime;
    @Shadow public boolean mouseGrabbed;

    @Shadow
    public abstract void grabMouse();

    @Shadow private int recheckPlayerIn;
    @Shadow public ParticleEngine particleEngine;

    @Shadow(
        remap = false,
        aliases = "method_2104"
    )
    protected abstract void startLoginThread();

    @Shadow
    public abstract void toggleDimension();

    @Shadow public Mob cameraEntity;

    @Shadow
    protected abstract void prepareLevel(String string);

    @Shadow
    public abstract LevelFormat getLevelSource();

    @Shadow
    public abstract void setLevel(Level arg);

    @Shadow
    public static File getWorkingDirectory() {
        return null;
    }

    @Shadow
    protected abstract void convertWorld(String string, String string2);

    @Shadow public HitResult hitResult;
    @Shadow public StatsCounter statManager;

    @Shadow
    public abstract void setLevel(Level arg, String string);

    @Shadow public ProgressRenderer progressRenderer;
    @Shadow public SoundEngine soundEngine;

    @Shadow
    protected abstract void renderLoadingScreen();

    @Unique private long previousNanoTime;
    @Unique private double deltaTime;
    @Unique private final int[] lastClickTicks = new int[3];
    @Unique public AC_CutsceneCamera cutsceneCamera;
    @Unique public AC_CutsceneCamera activeCutsceneCamera;
    @Unique public boolean cameraActive;
    @Unique public boolean cameraPause = true;
    @Unique public Mob cutsceneCameraEntity;
    @Unique public AC_GuiStore storeGUI = new AC_GuiStore();
    @Unique ItemInstance lastItemUsed;
    @Unique Entity lastEntityHit;
    @Unique ScriptVec3 lastBlockHit;
    @Unique private GLDevice glDevice;

    @Overwrite(remap = false)
    public static void main(String[] args) {
        var arguments = new Arguments();
        arguments.parse(args);

        String username = arguments.getOrDefault("username", "Player");
        String sessionId = "";

        if (arguments.containsKey("session")) {
            // 1.6
            sessionId = arguments.get("session");
        }
        else if (arguments.getExtraArgs().size() == 2) {
            // pre 1.6
            username = arguments.getExtraArgs().get(0);
            sessionId = arguments.getExtraArgs().get(1);
        }

        ACMainThread.workDir = new File(arguments.getOrDefault("gameDir", "."));

        if (arguments.containsKey("mapsDir")) {
            ACMod.setMapsDir(Path.of(arguments.get("mapsDir")));
        }

        ACMainThread.glDebugContext = arguments.getExtraArgs().contains("--glDebugContext");

        ACMainThread.glDebugLogSeverity = ACMainThread.GlDebugSeverity.valueOf(arguments.getOrDefault("glDebugLogSeverity",
            ACMainThread.glDebugLogSeverity.name()
        ));

        ACMainThread.glDebugTraceSeverity = ACMainThread.GlDebugSeverity.valueOf(arguments.getOrDefault("glDebugTraceSeverity",
            ACMainThread.glDebugTraceSeverity.name()
        ));

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

    @ModifyConstant(
        method = "init",
        constant = @Constant(stringValue = "Minecraft Minecraft Beta 1.7.3")
    )
    private String init_fixTitle(String constant) {
        return "Adventurecraft (Beta 1.7.3)";
    }

    @Inject(
        method = "init",
        at = @At("HEAD")
    )
    private void init_makeResizable(CallbackInfo ci) {
        this.orgWidth = this.width;
        Display.setResizable(true);
    }

    @Redirect(
        method = "init",
        at = @At(
            value = "INVOKE",
            target = "Lorg/lwjgl/opengl/Display;create()V",
            remap = false,
            ordinal = 0
        )
    )
    private void init_disableOriginalDisplay() {
    }

    @Inject(
        method = "init",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;options:Lnet/minecraft/client/Options;",
            shift = At.Shift.AFTER,
            ordinal = 0
        )
    )
    private void init_createDisplay(CallbackInfo ci)
        throws LWJGLException {
        int sampleCount = ((ExGameOptions) options).ofAaLevel();
        ACMod.LOGGER.info("MSAA Samples: {}x", sampleCount);

        if (ACMainThread.glDebugContext) {
            GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_DEBUG_CONTEXT, 1);
        }

        var pixelFormat = new PixelFormat();
        try {
            createDisplay(pixelFormat.withSamples(sampleCount), true);
        }
        catch (LWJGLException ex) {
            ACMod.LOGGER.warn("Error setting MSAA {}x: ", sampleCount, ex);
            createDisplay(pixelFormat, false);
        }

        var caps = GLContext.getCapabilities();
        Config.logOpenGlCaps(caps);

        this.glDevice = new GLDevice(caps);
    }

    private void createDisplay(PixelFormat pixelFormat, boolean rethrowLast)
        throws LWJGLException {
        try {
            Display.create(pixelFormat.withDepthBits(32));
            return;
        }
        catch (LWJGLException e) {
            ACMod.LOGGER.warn("Falling back to 24-bit depth buffer since 32-bit failed: ", e);
        }

        try {
            Display.create(pixelFormat.withDepthBits(24));
            return;
        }
        catch (LWJGLException e) {
            ACMod.LOGGER.warn("Falling back to 16-bit depth buffer since 24-bit failed: ", e);
        }

        try {
            Display.create(pixelFormat.withDepthBits(16));
            return;
        }
        catch (LWJGLException e) {
            ACMod.LOGGER.warn("Falling back to 8-bit depth buffer since 16-bit failed: ", e);
        }

        try {
            Display.create(pixelFormat.withDepthBits(8));
        }
        catch (LWJGLException e) {
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
            target = "Lnet/minecraft/client/renderer/Textures;addDynamicTexture(Lnet/minecraft/client/renderer/ptexture/DynamicTexture;)V",
            ordinal = 0
        )
    )
    private void init_addFanTextureBinder(CallbackInfo ci) {
        this.textures.addDynamicTexture(new AC_TextureFanFX());
    }

    @Inject(
        method = "run",
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Ljava/lang/System;currentTimeMillis()J",
            shift = At.Shift.BEFORE,
            ordinal = 0,
            remap = false
        )
    )
    private void run_setup(CallbackInfo ci) {
        this.textures.loadTexture("/terrain.png");
        this.textures.loadTexture("/terrain2.png");
        this.textures.loadTexture("/terrain3.png");
        ContextFactory.initGlobal(new ContextFactory());
    }

    @Inject(
        method = "run",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/phys/AABB;resetPool()V",
            shift = At.Shift.BEFORE
        )
    )
    private void run_updateDeltaTime(CallbackInfo ci) {
        long nanoTime = System.nanoTime();
        this.deltaTime = (nanoTime - this.previousNanoTime) / (double) 1_000_000_000;
        this.previousNanoTime = nanoTime;
    }

    @Redirect(
        method = "run",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/sounds/SoundEngine;update(Lnet/minecraft/world/entity/Mob;F)V"
        )
    )
    private void run_setSoundListenerPos(SoundEngine instance, Mob f, float v) {
        if (this.cameraActive) {
            instance.update(this.cutsceneCameraEntity, v);
        }
        else {
            instance.update(f, v);
        }
    }

    @Inject(
        method = "run",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Minecraft;stop()V",
            shift = At.Shift.BEFORE
        )
    )
    private void run_stop(CallbackInfo ci) {
        ContextFactory.getGlobal().enterContext();
        Context.exit();
    }

    @Inject(
        method = "run",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Minecraft;checkScreenshot()V",
            shift = At.Shift.AFTER
        )
    )
    private void fix_resize(CallbackInfo ci) {
        if (!this.fullscreen && (Display.getWidth() != this.width || Display.getHeight() != this.height)) {
            this.width = Display.getWidth();
            this.height = Display.getHeight();
            if (this.width <= 0) {
                this.width = 1;
            }

            if (this.height <= 0) {
                this.height = 1;
            }

            this.resize(this.width, this.height);
        }
    }

    @Redirect(
        method = "run",
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Ljava/lang/Thread;yield()V",
            remap = false
        )
    )
    private void removeYield() {
    }

    @Redirect(
        method = "run",
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Lorg/lwjgl/opengl/Display;isActive()Z",
            remap = false
        )
    )
    private boolean disableDoubleToggle() {
        return true;
    }

    @WrapWithCondition(
        method = "run",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Minecraft;renderFpsMeter(J)V"
        )
    )
    private boolean renderFrameTimeGraph(Minecraft instance, long time) {
        return !((ExGameOptions) this.options).ofFastDebugInfo();
    }

    @Inject(
        method = "run",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Minecraft;emergencySave()V",
            shift = At.Shift.AFTER,
            ordinal = 0
        )
    )
    private void printStackOnOutOfMem(CallbackInfo ci, @Local OutOfMemoryError error) {
        error.printStackTrace();
    }

    @Redirect(
        method = "toggleFullScreen",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;orgWidth:I"
        )
    )
    private int fix_getWidthAfterFullscreen(Minecraft instance) {
        return Display.getWidth();
    }

    @Redirect(
        method = "toggleFullScreen",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;orgHeight:I"
        )
    )
    private int fix_getHeightAfterFullscreen(Minecraft instance) {
        return Display.getHeight();
    }

    @Inject(
        method = "toggleFullScreen",
        at = @At(
            value = "INVOKE",
            target = "Lorg/lwjgl/opengl/Display;setFullscreen(Z)V",
            remap = false,
            shift = At.Shift.AFTER
        )
    )
    private void fix_restoreSizeAfterFullscreen(CallbackInfo ci)
        throws LWJGLException {
        if (!this.fullscreen && !Display.isMaximized()) {
            Display.setDisplayMode(new DisplayMode(this.orgWidth, this.orgHeight));
        }
    }

    @Inject(
        method = "resize",
        at = @At("TAIL")
    )
    private void updateStoreGuiOnResize(int var1, int var2, CallbackInfo ci) {
        updateStoreGUI();
    }

    @Inject(
        method = "init",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;textures:Lnet/minecraft/client/renderer/Textures;",
            opcode = Opcodes.PUTFIELD,
            ordinal = 0,
            shift = At.Shift.AFTER
        )
    )
    private void addEarlierLoadingScreen(CallbackInfo ci) {
        this.renderLoadingScreen();
    }

    @Redirect(
        method = "renderLoadingScreen",
        at = @At(
            value = "INVOKE",
            target = "Lorg/lwjgl/opengl/Display;swapBuffers()V",
            remap = false
        )
    )
    private void updateDisplayAfterLoad()
        throws LWJGLException {
        Display.update(false);
    }

    /**
     * @author Cryect
     * (Kiroto added Javadoc)
     * @reason Different things done on tick
     */
    @Overwrite
    public void tick() {
        if (this.ticks == 6000) {
            this.startLoginThread();
        }

        this.gui.tick();
        this.gameRenderer.pick(1.0F);

        if (this.player != null) {
            ChunkSource worldSource = this.level.getChunkSource();
            if (worldSource instanceof ChunkCache chunkCache) {
                int chunkX = Mth.floor((float) ((int) this.player.x)) >> 4;
                int chunkZ = Mth.floor((float) ((int) this.player.z)) >> 4;
                chunkCache.centerOn(chunkX, chunkZ);
            }
            // Bed safety leave
            if (this.player.isSleeping() && this.player.getSleepTimer() >= 100) {
                if (((ExWorldProperties) this.level.levelData).getTimeRate() == 0) {
                    this.player.stopSleepInBed(true, false, false);
                }
                else {
                    ((ExWorld) this.level).setTimeOfDay(10000);
                }
            }
        }

        if (!this.pause && this.level != null) {
            this.gameMode.tick();
        }

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.textures.loadTexture("/terrain.png"));
        if (!this.pause) {
            this.textures.tick();
        }

        if (this.screen == null && this.player != null) {
            if (this.player.health <= 0) {
                this.setScreen(null);
            }
            else if (this.player.isSleeping() && this.level != null && this.level.isClientSide) {
                this.setScreen(new AC_InBedChatScreen());
            }
        }
        else if (this.screen != null && this.screen instanceof AC_InBedChatScreen && !this.player.isSleeping()) {
            this.setScreen(null);
        }

        if (this.screen != null && !((ExScreen) this.screen).isDisabledInputGrabbing()) {
            // TODO: reset all values?
            this.lastClickTicks[0] = this.ticks + 10000;

            this.screen.updateEvents();
            if (this.screen != null) {
                this.screen.particles.tick();
                this.screen.tick();
            }
        }

        if (this.screen == null || this.screen.passEvents || ((ExScreen) this.screen).isDisabledInputGrabbing()) {
            //noinspection StatementWithEmptyBody
            while (this.processInput()) {
            }
        }

        if (this.level != null) {
            this.tickLevel();
        }

        this.lastTickTime = System.currentTimeMillis();
    }

    @Unique
    private boolean processInput() {
        long clickDelta;
        do {
            if (Mouse.next()) {
                clickDelta = System.currentTimeMillis() - this.lastTickTime;
                continue;
            }

            if (this.missTime > 0) {
                --this.missTime;
            }

            while (true) {
                do {
                    if (Keyboard.next()) {
                        this.player.setKey(Keyboard.getEventKey(), Keyboard.getEventKeyState());
                        continue;
                    }

                    boolean handle = this.screen == null || ((ExScreen) this.screen).isDisabledInputGrabbing();
                    if (handle) {
                        for (int i = 0; i < this.lastClickTicks.length; i++) {
                            int delta = this.ticks - this.lastClickTicks[i];
                            if (Mouse.isButtonDown(i) && delta >= 0 && this.mouseGrabbed) {
                                this.handleMouseClick(i);
                            }
                        }
                    }
                    this.handleMouseDown(0, handle && Mouse.isButtonDown(0) && this.mouseGrabbed);
                    return false;
                }
                while (!Keyboard.getEventKeyState());

                int eventKey = Keyboard.getEventKey();
                boolean isShiftPressed =
                    Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
                boolean isControlPressed =
                    Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);

                if (eventKey == Keyboard.KEY_F11) {
                    this.toggleFullScreen();
                }
                else {
                    if (this.screen != null && !((ExScreen) this.screen).isDisabledInputGrabbing()) {
                        // TODO: fix doubled events (one for key press, one for text input)
                        this.screen.keyboardEvent();
                    }
                    else {
                        // Not compile time constants, else-if is a must.
                        // Trust me, I tried to use a switch here.
                        if (eventKey == Keyboard.KEY_ESCAPE) {
                            this.pauseGame();
                        }
                        else if (eventKey == Keyboard.KEY_S && Keyboard.isKeyDown(Keyboard.KEY_F3)) {
                            this.reloadSound();
                        }
                        else if (eventKey == Keyboard.KEY_F1) {
                            this.options.hideGui = !this.options.hideGui;
                        }
                        else if (eventKey == Keyboard.KEY_F3) {
                            this.options.renderDebug = !this.options.renderDebug;
                        }
                        else if (eventKey == Keyboard.KEY_F4) {
                            var exPlayer = (ExPlayerEntity) this.player;
                            //noinspection SwitchStatementWithTooFewBranches
                            exPlayer.setGameMode(switch (exPlayer.getGameMode()) {
                                case AdventureGameMode ignored -> new DebugGameMode();
                                default -> new AdventureGameMode();
                            });
                            if (AC_DebugMode.isActive()) {
                                this.gui.addMessage("Debug Mode Active");
                            }
                            else {
                                this.gui.addMessage("Debug Mode Deactivated");
                            }
                            ((ExWorldEventRenderer) this.levelRenderer).updateAllTheRenderers();
                        }
                        else if (eventKey == Keyboard.KEY_F5) {
                            this.options.thirdPersonView = !this.options.thirdPersonView;
                        }
                        else if (eventKey == Keyboard.KEY_F6) {
                            if (AC_DebugMode.isActive()) {
                                ((ExWorldEventRenderer) this.levelRenderer).resetAll();
                                this.gui.addMessage("Resetting all blocks in loaded chunks");
                            }
                        }
                        else if (eventKey == Keyboard.KEY_F7 ||
                            (AC_DebugMode.isActive() && eventKey == this.options.keyInventory.key && isShiftPressed)) {
                            ((ExPlayerEntity) this.player).openPalette();
                        }
                        else if (eventKey == this.options.keyInventory.key) {
                            this.setScreen(new InventoryScreen(this.player));
                        }
                        else if (eventKey == this.options.keyDrop.key) {
                            this.player.drop();
                        }
                        else if ((this.isOnline() || AC_DebugMode.isActive()) && eventKey == this.options.keyChat.key) {
                            this.setScreen(new AC_ChatScreen());
                        }
                        else if (AC_DebugMode.isActive() && isControlPressed) {
                            var mc = (Minecraft) (Object) this;
                            if (eventKey == Keyboard.KEY_Z) { // Undo
                                ServerCommands.cmdUndo(new ServerCommandSource(mc, this.level, this.player), null);
                            }
                            else if (eventKey == Keyboard.KEY_Y) { // Redo
                                ServerCommands.cmdRedo(new ServerCommandSource(mc, this.level, this.player), null);
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
                                if (currentSlot == ((ExPlayerInventory) this.player.inventory).getOffhandSlot()) {
                                    ((ExPlayerInventory) this.player.inventory).setOffhandSlot(this.player.inventory.selected);
                                }

                                this.player.inventory.selected = currentSlot;
                            }
                            else {
                                if (currentSlot == this.player.inventory.selected) {
                                    this.player.inventory.selected = ((ExPlayerInventory) this.player.inventory).getOffhandSlot();
                                }

                                ((ExPlayerInventory) this.player.inventory).setOffhandSlot(currentSlot);
                            }
                        }

                        ++currentSlot;
                    }
                }

                if (this.level != null) {
                    ((ExWorld) this.level).getScript().keyboard.processKeyPress(eventKey);
                }
            }

        }
        while (clickDelta > 200L);

        int wheelDelta = Mouse.getEventDWheel();
        if (wheelDelta != 0) {
            boolean ctrlDown = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
            boolean menuDown = Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU);

            // TODO: are these clamps appropriate?
            if (wheelDelta > 0) {
                wheelDelta = 1;
            }
            if (wheelDelta < 0) {
                wheelDelta = -1;
            }

            if (AC_DebugMode.isActive() && menuDown) {
                AC_DebugMode.reachDistance += wheelDelta;
                AC_DebugMode.reachDistance = MathF.clamp(AC_DebugMode.reachDistance, 2, 100);
                this.gui.addMessage(String.format("Reach Changed to %d", AC_DebugMode.reachDistance));
            }
            else {
                if (ctrlDown) {
                    ((ExPlayerInventory) this.player.inventory).swapOffhandWithMain();
                }

                this.player.inventory.swapPaint(wheelDelta);
                if (ctrlDown) {
                    ((ExPlayerInventory) this.player.inventory).swapOffhandWithMain();
                }

                if (this.options.discreteMouseScroll) {
                    this.options.accumulatedScroll += (float) wheelDelta * 0.25F;
                }
            }
        }

        if (this.screen != null && !((ExScreen) this.screen).isDisabledInputGrabbing()) {
            if (this.screen != null) {
                this.screen.mouseEvent();
            }
        }
        else if (!this.mouseGrabbed && Mouse.getEventButtonState()) {
            this.grabMouse();
        }
        else {
            if (Mouse.getEventButtonState()) {
                this.handleMouseClick(Mouse.getEventButton());
            }
        }
        return true;
    }

    @Unique
    private void tickLevel() {
        if (this.player != null) {
            ++this.recheckPlayerIn;
            if (this.recheckPlayerIn == 30) {
                this.recheckPlayerIn = 0;
                this.level.ensureAdded(this.player);
            }
        }

        this.level.difficulty = this.options.difficulty;
        if (this.level.isClientSide) {
            this.level.difficulty = 3;
        }

        if (!this.pause) {
            this.gameRenderer.tick();
        }

        if (!this.pause) {
            this.levelRenderer.tick();
        }

        if (!this.pause || this.isOnline()) {
            ((ExWorld) this.level).ac$preTick();
        }

        if (!this.pause) {
            if (this.level.skyFlashTime > 0) {
                --this.level.skyFlashTime;
            }

            this.level.tickEntities();
        }

        if (!this.pause || this.isOnline()) {
            this.level.setSpawnSettings(this.options.difficulty > 0, true);
            this.level.tick();
        }

        if (!this.pause && this.level != null) {
            this.level.animateTick(Mth.floor(this.player.x), Mth.floor(this.player.y), Mth.floor(this.player.z));
        }

        if (!this.pause) {
            this.particleEngine.tick();
        }
    }

    @Overwrite
    private void pickBlock() {
        var hit = this.hitResult;
        if (hit == null) {
            return;
        }

        int id = this.level.getTile(hit.x, hit.y, hit.z);
        int meta = this.level.getData(hit.x, hit.y, hit.z);

        Inventory inv = this.player.inventory;
        int slotId = ((ExPlayerInventory) inv).getSlot(id, meta);
        if (slotId >= 0 && slotId < 9) {
            ((ExPlayerInventory) inv).selectSlot(slotId);
            return;
        }

        ItemInstance handItem = inv.getItem(inv.selected);

        ItemInstance pickItem;
        if (slotId == -1) {
            // Only grant items in debug mode.
            if (!AC_DebugMode.isActive()) {
                return;
            }
            pickItem = new ItemInstance(id, -64, meta);
        }
        else {
            pickItem = inv.getItem(slotId);
            // Swap hand item into found slot.
            inv.setItem(slotId, handItem);
            handItem = null;
        }

        inv.setItem(inv.selected, pickItem);
        if (handItem != null) {
            if (!inv.add(handItem)) {
                this.player.drop(handItem);
            }
        }
    }

    @Redirect(
        method = {"handleMouseDown", "grabMouse"},
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;missTime:I",
            ordinal = 0
        )
    )
    private void keepAttackCooldown(Minecraft instance, int value) {
    }

    @Redirect(
        method = "grabMouse",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;lastClickTick:I"
        )
    )
    private void setLastClickTickOnGrab(Minecraft instance, int value) {
        this.lastClickTicks[0] = value;
    }

    @Overwrite
    private void handleMouseClick(int mouseButton) {
        if (mouseButton == 0 && this.missTime > 0) {
            return;
        }

        if (mouseButton == 2) {
            this.pickBlock();
            return;
        }

        var exWorld = (ExWorld) this.level;
        boolean hasRecording = AC_DebugMode.isActive() && exWorld.getUndoStack().startRecording();

        boolean swapOffhand = false;
        ItemInstance stack = this.player.inventory.getSelected();
        if (!AC_DebugMode.isActive()) {
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

            this.lastClickTicks[mouseButton] = this.ticks + useDelay;

            if (stack != null && (Item.items[stack.id] instanceof AC_ILeftClickItem leftClickItem) &&
                leftClickItem.mainActionLeftClick()) {
                mouseButton = 0;
            }
            else {
                mouseButton = 1;
            }
        }
        else {
            Arrays.fill(this.lastClickTicks, this.ticks + 5);
        }

        if (mouseButton == 0) {
            this.player.swing();
        }

        boolean useOnBlock = true;
        if (this.hitResult == null) {
            if (mouseButton == 0 && !(this.gameMode instanceof CreativeMode)) {
                this.missTime = 10;
            }
        }
        else if (this.hitResult.hitType == HitType.ENTITY) {
            if (mouseButton == 0) {
                this.gameMode.attack(this.player, this.hitResult.entity);
            }

            if (mouseButton == 1) {
                this.gameMode.interact(this.player, this.hitResult.entity);
            }
        }
        else if (this.hitResult.hitType == HitType.TILE) {
            int bX = this.hitResult.x;
            int bY = this.hitResult.y;
            int bZ = this.hitResult.z;
            int bSide = this.hitResult.face;
            Tile block = Tile.tiles[this.level.getTile(bX, bY, bZ)];
            if (block != null) {
                if (!AC_DebugMode.isActive() && (block.id == Tile.CHEST.id || block.id == AC_Blocks.store.id)) {
                    mouseButton = 1;
                }

                if (!AC_DebugMode.isActive()) {
                    int var11 = ((ExBlock) block).alwaysUseClick(this.level, bX, bY, bZ);
                    if (var11 != -1) {
                        mouseButton = var11;
                    }
                }

                if (mouseButton == 0) {
                    this.gameMode.startDestroyBlock(bX, bY, bZ, this.hitResult.face);
                    if (stack != null && Item.items[stack.id] instanceof AC_ILeftClickItem leftClickItem) {
                        leftClickItem.onItemUseLeftClick(stack, this.player, this.level, bX, bY, bZ, bSide);
                    }
                }
                else if (mouseButton == 1) {
                    int count = stack == null ? 0 : stack.count;
                    if (this.gameMode.useItemOn(this.player, this.level, stack, bX, bY, bZ, bSide)) {
                        useOnBlock = false;
                        this.player.swing();
                    }

                    if (stack == null) {
                        this.endMouseClick(swapOffhand, exWorld, hasRecording);
                        return;
                    }

                    if (stack.count == 0 && stack == this.player.inventory.items[this.player.inventory.selected]) {
                        this.player.inventory.items[this.player.inventory.selected] = null;
                    }
                    else if (stack.count != count) {
                        this.gameRenderer.itemInHandRenderer.itemPlaced();
                    }
                }
            }
        }

        if (useOnBlock && mouseButton == 0 && stack != null) {
            if (Item.items[stack.id] instanceof AC_ILeftClickItem leftClickItem) {
                leftClickItem.onItemLeftClick(stack, this.level, this.player);
            }
        }

        if (useOnBlock && mouseButton == 1 && stack != null && this.gameMode.useItem(this.player, this.level, stack)) {
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
        }
        else if (this.hitResult.hitType == HitType.ENTITY) {
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
        }
        else if (this.hitResult.hitType != HitType.TILE) {
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
        }
        else {
            // Hit a block
            if (this.lastBlockHit == null || this.lastBlockHit.x != (double) this.hitResult.x ||
                this.lastBlockHit.y != (double) this.hitResult.y || this.lastBlockHit.z != (double) this.hitResult.z) {

                this.lastBlockHit = new ScriptVec3(
                    (float) this.hitResult.x,
                    (float) this.hitResult.y,
                    (float) this.hitResult.z
                );
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
        if (stack != null) {
            scriptName = stack.isStackedByData()
                ? String.format("item_%d_%d.js", stack.id, stack.getAuxValue())
                : String.format("item_%d.js", stack.id);
        }
        else {
            scriptName = "item_0.js";
        }
        exWorld.getScriptHandler().runScript(scriptName, exWorld.getScope(), false);

        this.endMouseClick(swapOffhand, exWorld, hasRecording);
    }

    @Unique
    private void endMouseClick(boolean swapOffhand, ExWorld exWorld, boolean hasRecording) {
        if (swapOffhand) {
            ((ExPlayerInventory) this.player.inventory).swapOffhandWithMain();
            ((ExPlayerEntity) this.player).setSwappedItems(false);
        }

        if (hasRecording) {
            exWorld.getUndoStack().stopRecording();
        }
    }

    @Inject(
        method = "setLevel(Lnet/minecraft/world/level/Level;Ljava/lang/String;Lnet/minecraft/world/entity/player/Player;)V",
        at = @At("HEAD")
    )
    private void resetStateOnInit(Level level, String stage, Player newPlayer, CallbackInfo ci) {
        ((ExInGameHud) this.gui).getScriptUI().clear();
        ScriptModel.clearAll();
        this.setCameraActive(false);

        if (level == null) {
            ((ExSoundHelper) this.soundEngine).stopMusic(null);
        }
    }

    @Environment(EnvType.CLIENT)
    @Inject(
        method = "setLevel(Lnet/minecraft/world/level/Level;Ljava/lang/String;Lnet/minecraft/world/entity/player/Player;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gamemode/GameMode;initLevel(Lnet/minecraft/world/level/Level;)V"
        )
    )
    private void loadMapTexOnInit(Level level, String stage, Player newPlayer, CallbackInfo ci) {
        ((ExWorld) level).loadMapTextures();
    }

    @Inject(
        method = "setLevel(Lnet/minecraft/world/level/Level;Ljava/lang/String;Lnet/minecraft/world/entity/player/Player;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gamemode/GameMode;initPlayer(Lnet/minecraft/world/entity/player/Player;)V"
        )
    )
    private void initPlayerOnInit(Level level, String stage, Player newPlayer, CallbackInfo ci) {
        this.cutsceneCamera = new AC_CutsceneCamera(level);
        this.cutsceneCameraEntity = this.gameMode.createPlayer(level);
        ((ExWorld) level).getScript().initPlayer(this.player);
    }

    @Redirect(
        method = "prepareLevel",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/ProgressRenderer;progressStagePercentage(I)V"
        )
    )
    private void reportPreciseTerrainProgress(
        ProgressRenderer instance,
        int i,
        @Local(ordinal = 1) int count,
        @Local(ordinal = 2) int max
    ) {

        String stage = String.format("%4d / %4d", count, max);
        ((ExProgressListener) this.progressRenderer).notifyProgress(stage, count / (double) max, false);
    }

    @Overwrite
    public void respawnPlayer(boolean var1, int var2) {
        if (!this.level.isClientSide && !this.level.dimension.mayRespawn()) {
            this.toggleDimension();
        }

        ChunkSource worldSource = this.level.getChunkSource();
        if (worldSource instanceof ChunkCache chunkCache) {
            Vec3i spawnPos = this.level.getSpawnPos();
            chunkCache.centerOn(spawnPos.x >> 4, spawnPos.z >> 4);
        }

        this.level.removeAllPendingEntityRemovals();
        int playerId = 0;
        if (this.player != null) {
            playerId = this.player.id;
            this.level.removeEntity(this.player);
        }
        else {
            this.player = (LocalPlayer) this.gameMode.createPlayer(this.level);
            ((ExWorld) this.level).getScript().initPlayer(this.player);
        }

        ((ExWorldEventRenderer) this.levelRenderer).resetForDeath();
        Vec3i spawnPos = this.level.getSpawnPos();
        this.player.resetPos();
        this.player.moveTo((double) spawnPos.x + 0.5D, spawnPos.y, (double) spawnPos.z + 0.5D, 0.0F, 0.0F);
        this.cameraEntity = this.player;
        this.player.resetPos();
        this.gameMode.initPlayer(this.player);
        this.level.loadPlayer(this.player);
        this.player.input = new KeyboardInput(this.options);
        this.player.id = playerId;
        this.player.method_494();
        this.player.setRot(((ExWorld) this.level).getSpawnYaw(), 0.0F);
        this.gameMode.adjustPlayer(this.player);
        this.prepareLevel("Respawning");
        if (this.screen instanceof DeathScreen) {
            this.setScreen(null);
        }
    }

    @Overwrite
    public void selectLevel(String worldName, String saveName, long seed) {
        String mapName = this.getMapUsed(worldName);
        if (Mth.isStringInvalid(mapName)) {
            this.setScreen(new AC_GuiMapSelect(null, worldName));
        }
        else {
            this.startWorld(worldName, saveName, seed, mapName);
        }
    }

    @Override
    public Level getWorld(String saveName, long seed, String mapName) {
        this.setLevel(null);
        LevelIO dimData = this.getLevelSource().method_1009(saveName, false);
        Level world = ExWorld.createWorld(mapName, dimData, saveName, seed, this.progressRenderer);
        return world;
    }

    @Override
    public void startWorld(String worldName, String saveName, long seed, String mapName) {
        this.setLevel(null);
        System.gc();
        if (worldName != null && this.getLevelSource().requiresConversion(worldName)) {
            this.convertWorld(worldName, saveName);
        }
        else {
            // TODO: reset global state in consistent matter
            AC_DebugMode.levelEditing = false;
            LevelIO dimData = null;
            if (worldName != null) {
                dimData = this.getLevelSource().method_1009(worldName, false);
            }

            if (saveName == null) {
                saveName = "Map Editing";
            }

            Level world = ExWorld.createWorld(mapName, dimData, saveName, seed, this.progressRenderer);
            if (world.isNew) {
                this.statManager.addStat(Stats.CREATE_WORLD, 1);
                this.statManager.addStat(Stats.START_GAME, 1);
                this.setLevel(world, "Generating level");
            }
            else {
                this.statManager.addStat(Stats.LOAD_WORLD, 1);
                this.statManager.addStat(Stats.START_GAME, 1);
                this.setLevel(world, "Loading level");
            }
        }

        this.setScreen(null);
    }

    private File getWorldFolder(String worldName) {
        File gameFolder = getWorkingDirectory();
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
            }
            catch (FileNotFoundException var8) {
            }
            catch (IOException var9) {
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
        }
        catch (FileNotFoundException var8) {
        }
        catch (IOException var9) {
        }
    }

    @Override
    public void loadSoundFromDir(String path, URL url) {
        int n = path.indexOf("/");
        String firstDir = path.substring(0, n);
        String id = path.substring(n + 1);

        var sound = (ExSoundHelper) this.soundEngine;
        if (firstDir.equalsIgnoreCase("sound")) {
            sound.addSound(id, url);
        }
        else if (firstDir.equalsIgnoreCase("newsound")) {
            sound.addSound(id, url);
        }
        else if (firstDir.equalsIgnoreCase("streaming")) {
            sound.addStreaming(id, url);
        }
        else if (firstDir.equalsIgnoreCase("music")) {
            sound.addMusic(id, url);
        }
        else if (firstDir.equalsIgnoreCase("newmusic")) {
            sound.addMusic(id, url);
        }
    }

    @Override
    public double getFrameTime() {
        return this.deltaTime;
    }

    @Override
    public void updateStoreGUI() {
        var scaler = new ScreenSizeCalculator(this.options, this.width, this.height);
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
    public Mob getCutsceneCameraEntity() {
        return this.cutsceneCameraEntity;
    }

    @Override
    public AC_GuiStore getStoreGUI() {
        return this.storeGUI;
    }

    public @Override GLDevice getGlDevice() {
        return this.glDevice;
    }
}
