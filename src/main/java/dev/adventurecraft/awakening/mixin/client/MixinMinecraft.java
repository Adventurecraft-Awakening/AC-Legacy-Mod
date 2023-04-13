package dev.adventurecraft.awakening.mixin.client;

import dev.adventurecraft.awakening.ACMainThread;
import dev.adventurecraft.awakening.ACMod;
import net.fabricmc.loader.impl.util.Arguments;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.Session;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.PixelFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

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

    @Overwrite
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

    @Inject(method = "init", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Minecraft;getGameDirectory()Ljava/io/File;"))
    private void init_makeResizable(CallbackInfo ci) {
        this.width = this.actualWidth;
        Display.setResizable(true);
    }

    @Redirect(method = "init", at = @At(
            value = "INVOKE",
            target = "Lorg/lwjgl/opengl/Display;create()V",
            remap = false,
            ordinal = 0))
    private void init_fixDepth() {
        try {
            Display.create((new PixelFormat()).withDepthBits(32));
        } catch (LWJGLException e32) {
            ACMod.LOGGER.warn("Falling back to 24-bit depth buffer since 32-bit failed: ", e32);

            try {
                Display.create((new PixelFormat()).withDepthBits(24));
            } catch (LWJGLException e24) {
                ACMod.LOGGER.warn("Falling back to 16-bit depth buffer since 24-bit failed: ", e24);

                try {
                    Display.create((new PixelFormat()).withDepthBits(16));
                } catch (LWJGLException e16) {
                    ACMod.LOGGER.warn("Falling back to unspecified depth buffer since 16-bit failed: ", e16);
                }
            }
        }
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

    @Inject(method = "toggleFullscreen", at = @At(
            value = "INVOKE",
            target = "Lorg/lwjgl/opengl/Display;setFullscreen(Z)V",
            shift = At.Shift.AFTER))
    private void fix_toggleFullscreen(CallbackInfo ci) throws LWJGLException {
        if (!this.isFullscreen) {
            Display.setDisplayMode(new DisplayMode(this.width, this.height));
        }
    }
}