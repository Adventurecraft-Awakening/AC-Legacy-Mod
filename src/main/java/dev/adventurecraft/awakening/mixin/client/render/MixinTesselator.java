package dev.adventurecraft.awakening.mixin.client.render;

import dev.adventurecraft.awakening.extension.client.render.ExTesselator;
import dev.adventurecraft.awakening.util.MathF;
import net.minecraft.client.renderer.Tesselator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.nio.ByteOrder;

@Mixin(Tesselator.class)
public abstract class MixinTesselator implements ExTesselator {

    @Shadow private static boolean TRIANGLE_MODE;
    @Shadow private static boolean USE_VBO;

    @Shadow private boolean hasColor;
    @Shadow private int packedColor;
    @Shadow private boolean noColor;

    @Shadow private double u;
    @Shadow private double v;

    @Shadow
    public abstract void vertex(double x, double y, double z);

    @Shadow
    public abstract void vertexUV(double x, double y, double z, double u, double v);

    static {
        TRIANGLE_MODE = false;
        USE_VBO = true;
    }

    public @Override void ac$vertex(float x, float y, float z) {
        this.vertex(x, y, z);
    }

    public @Override void ac$tex(float u, float v) {
        this.u = u;
        this.v = v;
    }

    public @Override void ac$color(int rgba) {
        this.hasColor = true;
        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            this.packedColor = rgba;
        }
        else {
            this.packedColor = Integer.reverseBytes(rgba);
        }
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
        this.color(r, g, b, 255);
    }
}
