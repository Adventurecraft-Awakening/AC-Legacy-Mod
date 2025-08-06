package dev.adventurecraft.awakening.client.rendering;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.extension.client.render.ExTesselator;
import net.minecraft.client.renderer.Tesselator;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;

public final class MemoryTesselator extends Tesselator implements ExTesselator {

    public static final int BLOCK_SIZE = 1024 * 64;

    private static final int ATTR_STRIDE = 8;
    private static final int BYTE_STRIDE = ATTR_STRIDE * 4;

    private static final IntBuffer EMPTY_BLOCK = ByteBuffer
        .allocateDirect(0)
        .order(ByteOrder.nativeOrder())
        .asIntBuffer();

    private IntBuffer block;
    private ArrayList<IntBuffer> blocks;

    private float u;
    private float v;
    private int rgba;
    private int normal;

    private MemoryTesselator() {
        super(0);
    }

    private void init() {
        this.block = EMPTY_BLOCK;
        this.blocks = new ArrayList<>();
    }

    public static MemoryTesselator create() {
        try {
            // Easy way of skipping allocations of super constructor.
            var obj = (MemoryTesselator) ACMod.UNSAFE.allocateInstance(MemoryTesselator.class);
            obj.init();
            return obj;
        }
        catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    public long size() {
        return this.block.position() + (long) this.blocks.size() * BLOCK_SIZE;
    }

    public boolean isEmpty() {
        return this.block.position() == 0 && this.blocks.isEmpty();
    }

    public @Override void end() {
        if (!this.tesselating) {
            throw new IllegalStateException("Not tesselating!");
        }
        this.tesselating = false;
    }

    public @Override void clear() {
        this.block = EMPTY_BLOCK;
        this.blocks.clear();
    }

    public @Override void vertex(double x, double y, double z) {
        this.ac$vertex((float) x, (float) y, (float) z);
    }

    public @Override void vertexUV(double x, double y, double z, double u, double v) {
        this.ac$vertexUV((float) x, (float) y, (float) z, (float) u, (float) v);
    }

    public @Override void ac$vertex(float x, float y, float z) {
        this.ac$vertexUV(x, y, z, this.u, this.v);
    }

    public @Override void ac$tex(float u, float v) {
        this.u = u;
        this.v = v;
    }

    public @Override void ac$color(int rgba) {
        this.rgba = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN ? rgba : Integer.reverseBytes(rgba);
    }

    public @Override void ac$vertexUV(float x, float y, float z, float u, float v) {
        var a = this.reserve(ATTR_STRIDE);
        a.put(0, Float.floatToRawIntBits(x));
        a.put(1, Float.floatToRawIntBits(y));
        a.put(2, Float.floatToRawIntBits(z));
        a.put(3, Float.floatToRawIntBits(u));
        a.put(4, Float.floatToRawIntBits(v));
        a.put(5, this.rgba);
        a.put(6, this.normal);
        a.put(7, 0);
    }

    private IntBuffer reserve(int count) {
        IntBuffer b = this.block;
        int start = b.position();
        int end = start + count;
        if (end <= b.limit()) {
            var span = b.slice(start, count);
            b.position(end);
            return span;
        }
        return this.pushAndReserve(count);
    }

    private IntBuffer pushAndReserve(int count) {
        if (this.block != EMPTY_BLOCK) {
            assert this.block.limit() == BLOCK_SIZE;
            this.blocks.add(this.block);
        }

        IntBuffer b = ByteBuffer.allocateDirect(BLOCK_SIZE * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
        b.position(count);
        this.block = b;
        return b.slice(0, count);
    }

    public void render(final int target) {
        long size = this.size() << 2;
        // Re-allocate buffer for upload.
        GL15.glBufferData(target, size, GL15.GL_STATIC_DRAW);

        long offset = 0;
        for (IntBuffer block : this.blocks) {
            GL15.glBufferSubData(target, offset, block.flip());
            offset += Integer.toUnsignedLong(block.remaining()) << 2;
        }
        GL15.glBufferSubData(target, offset, this.block.flip());
        offset += Integer.toUnsignedLong(this.block.remaining()) << 2;
        assert offset == size;

        GL11.glVertexPointer(3, GL11.GL_FLOAT, BYTE_STRIDE, 0L);
        GL11.glTexCoordPointer(2, GL11.GL_FLOAT, BYTE_STRIDE, 12L);
        GL11.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, BYTE_STRIDE, 20L);
        GL11.glNormalPointer(GL11.GL_BYTE, BYTE_STRIDE, 24L);

        GL11.glDrawArrays(this.mode, 0, (int) (size / BYTE_STRIDE));
    }
}
