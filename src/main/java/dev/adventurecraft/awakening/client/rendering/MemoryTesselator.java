package dev.adventurecraft.awakening.client.rendering;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.client.renderer.MemoryMesh;
import dev.adventurecraft.awakening.extension.client.render.ExTesselator;
import dev.adventurecraft.awakening.util.GLUtil;
import net.minecraft.client.renderer.Tesselator;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public final class MemoryTesselator extends Tesselator implements ExTesselator {

    public static final int BLOCK_SIZE = 1024 * 64 * 4;

    public static final int BYTE_STRIDE = 7 * 4;

    private static final ByteBuffer EMPTY_BLOCK = ByteBuffer.allocateDirect(0).order(ByteOrder.nativeOrder());

    private ByteBuffer block;
    private ArrayList<ByteBuffer> blocks;

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
        var a = this.reserve(BYTE_STRIDE);
        a.putInt(Float.floatToRawIntBits(x));
        a.putInt(Float.floatToRawIntBits(y));
        a.putInt(Float.floatToRawIntBits(z));
        a.putInt(Float.floatToRawIntBits(u));
        a.putInt(Float.floatToRawIntBits(v));
        a.putInt(this.rgba);
        a.putInt(this.normal);
    }

    public @Override void normal(float x, float y, float z) {
        this.normal = GLUtil.packByteNormal(x, y, z);
    }

    private ByteBuffer reserve(int count) {
        ByteBuffer b = this.block;
        int start = b.position();
        int end = start + count;
        if (end <= b.limit()) {
            return b;
        }
        return this.pushAndReserve(count);
    }

    private ByteBuffer pushAndReserve(int count) {
        if (count > BLOCK_SIZE) {
            throw new IllegalArgumentException();
        }

        if (this.block != EMPTY_BLOCK) {
            var fullBlock = this.block;
            this.block = EMPTY_BLOCK;

            assert fullBlock.limit() == BLOCK_SIZE;
            this.blocks.add(fullBlock);
        }

        // TODO: allocate (smaller) blocks from Arena?
        ByteBuffer b = ByteBuffer.allocateDirect(BLOCK_SIZE).order(ByteOrder.nativeOrder());
        this.block = b;
        return b;
    }

    public MemoryMesh takeMesh() {
        var mesh = new MemoryMesh();
        for (ByteBuffer block : this.blocks) {
            mesh.vertexBlocks.add(block.flip());
        }
        this.blocks.clear();

        var lastBlock = this.block;
        this.block = EMPTY_BLOCK;
        mesh.vertexBlocks.add(lastBlock.flip());

        return mesh;
    }
}
