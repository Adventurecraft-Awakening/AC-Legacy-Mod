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
    private void init_fixDepth() throws LWJGLException {
        try {
            Display.create((new PixelFormat()).withDepthBits(32));
        } catch (LWJGLException e) {
            e.printStackTrace();
            Display.create((new PixelFormat()).withDepthBits(24));
        }
    }
}
