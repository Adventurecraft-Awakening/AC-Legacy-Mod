package dev.adventurecraft.awakening.mixin.client;

import net.minecraft.client.Minecraft;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.PixelFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

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
