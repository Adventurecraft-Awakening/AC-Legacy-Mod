package dev.adventurecraft.awakening.mixin.client.render;

import dev.adventurecraft.awakening.extension.client.render.ExTesselator;
import net.minecraft.client.renderer.Tesselator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.nio.ByteOrder;

@Mixin(Tesselator.class)
public abstract class MixinTesselator implements ExTesselator {

    @Shadow
    private boolean hasColor;

    @Shadow
    private int packedColor;

    @Shadow
    private boolean noColor;

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
