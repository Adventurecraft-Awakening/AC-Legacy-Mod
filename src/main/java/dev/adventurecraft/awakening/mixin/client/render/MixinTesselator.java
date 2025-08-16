package dev.adventurecraft.awakening.mixin.client.render;

import dev.adventurecraft.awakening.extension.client.render.ExTesselator;
import dev.adventurecraft.awakening.util.GLUtil;
import dev.adventurecraft.awakening.util.MathF;
import net.minecraft.client.renderer.Tesselator;
import org.spongepowered.asm.mixin.*;

import java.nio.ByteOrder;

@Mixin(Tesselator.class)
public abstract class MixinTesselator implements ExTesselator {

    @Shadow private static boolean TRIANGLE_MODE;
    @Shadow private static boolean USE_VBO;

    @Shadow private boolean hasColor;
    @Shadow private boolean hasTexture;
    @Shadow private boolean hasNormal;

    @Shadow private double u;
    @Shadow private double v;
    @Shadow private int packedColor;

    @Shadow private int[] array;
    @Shadow private int vertices;
    @Shadow private int p;
    @Shadow private int count;

    @Shadow private boolean noColor;
    @Shadow public int mode;

    @Shadow private double xo;
    @Shadow private double yo;
    @Shadow private double zo;
    @Shadow private int normal;

    @Shadow public boolean tesselating;
    @Shadow private int size;

    static {
        // TODO: use index buffer
        TRIANGLE_MODE = false;
        USE_VBO = true;
    }

    @Shadow
    public abstract void end();

    public @Override void ac$vertex(float x, float y, float z) {
        this.addVertex(x, y, z, (float) this.u, (float) this.v);
    }

    public @Override void ac$tex(float u, float v) {
        this.u = u;
        this.v = v;
        this.hasTexture = true;
    }

    public @Override void ac$vertexUV(float x, float y, float z, float u, float v) {
        this.ac$tex(u, v);
        this.addVertex(x, y, z, u, v);
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

    public @Overwrite void vertex(double x, double y, double z) {
        this.addVertex(
            (float) (x + this.xo),
            (float) (y + this.yo),
            (float) (z + this.zo),
            (float) this.u,
            (float) this.v
        );
    }

    public @Overwrite void normal(float x, float y, float z) {
        this.hasNormal = true;
        this.normal = GLUtil.packByteNormal(x, y, z);
    }

    public @Override void ac$normal(float x, float y, float z) {
        this.normal(x, y, z);
    }

    @Unique
    private void addVertex(float x, float y, float z, float u, float v) {
        ++this.count;
        if (this.mode == 7 && TRIANGLE_MODE && this.count % 4 == 0) {
            this.triangulateQuad();
        }

        int i = this.p;
        int[] a = this.array;
        a[i + 0] = Float.floatToRawIntBits(x);
        a[i + 1] = Float.floatToRawIntBits(y);
        a[i + 2] = Float.floatToRawIntBits(z);
        a[i + 3] = Float.floatToRawIntBits(u);
        a[i + 4] = Float.floatToRawIntBits(v);
        a[i + 5] = this.packedColor;
        a[i + 6] = this.normal;
        a[i + 7] = 0;
        this.p = i + 8;

        ++this.vertices;
        if (this.vertices % 4 == 0 && this.p >= this.size - 32) {
            this.end();
            this.tesselating = true;
        }
    }

    @Unique
    private void triangulateQuad() {
        int[] a = this.array;
        for (int v = 0; v < 2; ++v) {
            int n = 8 * (3 - v);
            int i = this.p;
            a[i + 0] = a[i - n + 0];
            a[i + 1] = a[i - n + 1];
            a[i + 2] = a[i - n + 2];
            a[i + 3] = a[i - n + 3];
            a[i + 4] = a[i - n + 4];
            a[i + 5] = a[i - n + 5];
            a[i + 6] = a[i - n + 6];
            a[i + 7] = a[i - n + 7];
            this.p = i + 8;
            ++this.vertices;
        }
    }

    public final @Override double getX() {
        return this.xo;
    }

    public final @Override double getY() {
        return this.yo;
    }

    public final @Override double getZ() {
        return this.zo;
    }
}
