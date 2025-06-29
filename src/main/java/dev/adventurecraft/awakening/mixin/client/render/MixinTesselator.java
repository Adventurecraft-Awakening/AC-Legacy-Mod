package dev.adventurecraft.awakening.mixin.client.render;

import dev.adventurecraft.awakening.extension.client.render.ExTesselator;
import dev.adventurecraft.awakening.util.MathF;
import net.minecraft.client.renderer.Tesselator;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.nio.ByteOrder;

@Mixin(Tesselator.class)
public abstract class MixinTesselator implements ExTesselator {

    @Shadow private boolean hasColor;
    @Shadow private int packedColor;
    @Shadow private boolean noColor;

    @Redirect(
        method = "<init>",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/renderer/Tesselator;USE_VBO:Z"
        )
    )
    private boolean useVbo() {
        return true;
    }

    private @Unique void ac$packedColor(int rgba) {
        this.hasColor = true;
        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            this.packedColor = rgba;
        }
        else {
            this.packedColor = Integer.reverseBytes(rgba);
        }
    }

    private @Unique void ac$color(int r, int g, int b, int a) {
        this.ac$packedColor(a << 24 | b << 16 | g << 8 | r);
    }

    public @Override void ac$color(int rgba) {
        if (this.noColor) {
            return;
        }
        this.ac$packedColor(rgba);
    }

    public @Overwrite void color(int r, int g, int b, int a) {
        if (this.noColor) {
            return;
        }
        r = MathF.clamp(r, 0, 255);
        g = MathF.clamp(g, 0, 255);
        b = MathF.clamp(b, 0, 255);
        a = MathF.clamp(a, 0, 255);
        this.ac$color(r, g, b, a);
    }

    public @Overwrite void color(int r, int g, int b) {
        if (this.noColor) {
            return;
        }
        r = MathF.clamp(r, 0, 255);
        g = MathF.clamp(g, 0, 255);
        b = MathF.clamp(b, 0, 255);
        this.ac$color(r, g, b, 255);
    }
}
