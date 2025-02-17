package dev.adventurecraft.awakening.mixin.client.render;

import dev.adventurecraft.awakening.extension.client.render.ExTesselator;
import net.minecraft.client.renderer.Tesselator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.nio.ByteOrder;

@Mixin(Tesselator.class)
public abstract class MixinTesselator implements ExTesselator {

    @Shadow
    private boolean hasColor;

    @Shadow
    private int packedColor;

    @Shadow
    private boolean noColor;

    @Redirect(
        method = "<init>",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/renderer/Tesselator;USE_VBO:Z"))
    private boolean useVbo() {
        return true;
    }

    @Override
    public void ac$color(int rgba) {
        if (this.noColor) {
            return;
        }
        this.hasColor = true;

        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            this.packedColor = rgba;
        } else {
            this.packedColor = Integer.reverseBytes(rgba);
        }
    }
}
