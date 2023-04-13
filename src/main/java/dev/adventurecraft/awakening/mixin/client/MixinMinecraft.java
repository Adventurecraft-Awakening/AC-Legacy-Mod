package dev.adventurecraft.awakening.mixin.client;

import dev.adventurecraft.awakening.ACMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.Session;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.PixelFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

    // TODO: remove this when start_removeAwt works
    @Redirect(method = "<init>", at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;canvas:Ljava/awt/Canvas;",
            ordinal = 0))
    private void init_ignoreCanvas(Minecraft instance, Canvas value) {
    }

    // FIXME: this method fails to apply since it is too early
    @Inject(method = "start(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", at = @At("HEAD"))
    private static void start_removeAwt(String string, String string2, String string3, CallbackInfo ci) {
        boolean var3 = false;
        ACMainThread var7 = new ACMainThread(854, 480, var3);
        Thread var8 = new Thread(var7, "Minecraft main thread");
        var8.setPriority(10);
        var7.minecraftUrl = "www.minecraft.net";
        if (string != null && string2 != null) {
            var7.session = new Session(string, string2);
        } else {
            var7.session = new Session("Player" + System.currentTimeMillis() % 1000L, "");
        }

        if (string3 != null) {
            String[] var9 = string3.split(":");
            var7.setIpPort(var9[0], Integer.parseInt(var9[1]));
        }

        //var5.addWindowListener(var7.new MinecraftWindowAdapter(var8));
        var8.start();
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
}