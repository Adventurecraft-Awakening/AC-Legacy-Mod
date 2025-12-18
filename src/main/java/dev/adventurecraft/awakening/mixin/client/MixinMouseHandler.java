package dev.adventurecraft.awakening.mixin.client;

import net.minecraft.client.MouseHandler;
import org.lwjgl.LWJGLException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MouseHandler.class)
public abstract class MixinMouseHandler {

    @Redirect(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lorg/lwjgl/LWJGLException;printStackTrace()V"
        )
    )
    private void muteException(LWJGLException instance) {
    }
}
