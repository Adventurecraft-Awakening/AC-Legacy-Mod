package dev.adventurecraft.awakening.mixin.client.render;

import dev.adventurecraft.awakening.extension.client.render.ExTesselator;
import dev.adventurecraft.awakening.client.render.compat.LegacyGlAdapter;
import dev.adventurecraft.awakening.image.Rgba;
import dev.adventurecraft.awakening.client.render.VertexFormat;
import dev.adventurecraft.awakening.client.render.VertexPacking;
import net.minecraft.client.renderer.Tesselator;
import org.spongepowered.asm.mixin.*;

import java.nio.ByteOrder;
import java.nio.ByteBuffer;

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

    @Shadow private ByteBuffer buffer;
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
        // The Vulkan backend indexes quads without duplicating vertices.
        TRIANGLE_MODE = false;
        USE_VBO = false;
    }

    /** Submit packed vertices directly; inherited GL buffer and client-array state is unnecessary. */
    @Overwrite
    public void end() {
        if (!this.tesselating) {
            throw new IllegalStateException("Not tesselating!");
        }
        this.tesselating = false;
        if (this.vertices > 0) {
            this.buffer.position(0).limit(Math.multiplyExact(this.p, Integer.BYTES));
            LegacyGlAdapter.submitVertices(this.buffer, this.mode, this.hasColor, this.hasTexture, this.hasNormal);
        }
        // Match vanilla clear without resetting attribute flags: automatic flushes continue the same batch.
        this.vertices = 0;
        this.buffer.clear();
        this.p = 0;
        this.count = 0;
    }

    @Shadow
    public abstract void color(float r, float g, float b, float a);

    @Shadow
    public abstract void color(float r, float g, float b);

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

    public @Override void ac$color8(int rgba) {
        this.hasColor = true;
        this.packedColor = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN ? rgba : Integer.reverseBytes(rgba);
    }

    public @Override void ac$color32(float r, float g, float b, float a) {
        this.color(r, g, b, a);
    }

    public @Override void ac$color32(float r, float g, float b) {
        this.color(r, g, b);
    }

    public @Overwrite void color(int r, int g, int b, int a) {
        if (this.noColor) {
            return;
        }
        this.ac$color8(Rgba.saturateFromRgba8(r, g, b, a));
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
        this.ac$normal8(VertexPacking.packByteNormal(x, y, z));
    }

    public @Override void ac$normal8(int xyz) {
        this.hasNormal = true;
        this.normal = xyz;
    }

    public @Override void ac$normal32(float x, float y, float z) {
        this.normal(x, y, z);
    }

    @Unique
    private void addVertex(float x, float y, float z, float u, float v) {
        int color = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN ? this.packedColor : Integer.reverseBytes(this.packedColor);
        VertexFormat.write(this.buffer, this.p * Integer.BYTES, x, y, z, u, v, color, this.normal);
        this.p += VertexFormat.STRIDE / Integer.BYTES;

        ++this.vertices;
        if (this.vertices % 4 == 0 && this.p >= this.size - 32) {
            this.end();
            this.tesselating = true;
        }
    }

    public @Override double getX() {
        return this.xo;
    }

    public @Override double getY() {
        return this.yo;
    }

    public @Override double getZ() {
        return this.zo;
    }
}
