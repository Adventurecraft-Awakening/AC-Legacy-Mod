package dev.adventurecraft.awakening.client.rendering;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.client.renderer.BlockAllocator;
import dev.adventurecraft.awakening.client.renderer.MemoryMesh;
import dev.adventurecraft.awakening.extension.client.render.ExTesselator;
import dev.adventurecraft.awakening.util.GLUtil;
import dev.adventurecraft.awakening.util.UnsafeUtil;
import net.minecraft.client.renderer.Tesselator;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public final class MemoryTesselator extends Tesselator implements ExTesselator {

    public static final int BYTE_STRIDE = 7 * 4;

    private static final ByteBuffer EMPTY_BLOCK = ByteBuffer.allocateDirect(0).order(ByteOrder.nativeOrder());

    private BlockAllocator allocator;
    private ByteBuffer block;
    private ArrayList<ByteBuffer> blocks;

    private float u;
    private float v;
    private int rgba;
    private int normal;

    private float xo;
    private float yo;
    private float zo;

    private MemoryTesselator() {
        super(0);
    }

    private void init(BlockAllocator allocator) {
        this.allocator = allocator;
        this.block = EMPTY_BLOCK;
        this.blocks = new ArrayList<>();
    }

    public static MemoryTesselator create(BlockAllocator allocator) {
        // Easy way of skipping allocations of super constructor.
        var obj = UnsafeUtil.allocateInstance(MemoryTesselator.class);
        obj.init(allocator);
        return obj;
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

    public @Override void ac$color8(int rgba) {
        // TODO: is endian correction here needed?
        this.rgba = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN ? rgba : Integer.reverseBytes(rgba);
    }

    public @Override void ac$color32(float r, float g, float b, float a) {
        this.color(r, g, b, a);
    }

    public @Override void ac$vertexUV(float x, float y, float z, float u, float v) {
        var a = this.reserve(BYTE_STRIDE);
        a.putInt(Float.floatToRawIntBits(x + this.xo));
        a.putInt(Float.floatToRawIntBits(y + this.yo));
        a.putInt(Float.floatToRawIntBits(z + this.zo));
        a.putInt(Float.floatToRawIntBits(u));
        a.putInt(Float.floatToRawIntBits(v));
        a.putInt(this.rgba);
        a.putInt(this.normal);
    }

    public @Override void normal(float x, float y, float z) {
        this.ac$normal8(GLUtil.packByteNormal(x, y, z));
    }

    public @Override void ac$normal8(int xyz) {
        this.normal = xyz;
    }

    public @Override void ac$normal32(float x, float y, float z) {
        this.normal(x, y, z);
    }

    public @Override void offset(double x, double y, double z) {
        this.xo = (float) x;
        this.yo = (float) y;
        this.zo = (float) z;
    }

    public @Override void addOffset(float xo, float yo, float zo) {
        this.xo += xo;
        this.yo += yo;
        this.zo += zo;
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
        if (count > this.allocator.blockSize()) {
            throw new IllegalArgumentException();
        }

        if (this.block != EMPTY_BLOCK) {
            var fullBlock = this.block;
            this.block = EMPTY_BLOCK;

            if (fullBlock.limit() != this.allocator.blockSize()) {
                throw new AssertionError("incorrect block size");
            }
            this.blocks.add(fullBlock);
        }

        // TODO: allocate (smaller) blocks from mapped-buffer Arena?
        ByteBuffer b = this.allocator.newBlock();
        this.block = b;
        return b;
    }

    public MemoryMesh takeMesh() {
        var mesh = new MemoryMesh(this.allocator);
        for (ByteBuffer block : this.blocks) {
            mesh.vertexBlocks.add(block.flip());
        }
        this.blocks.clear();

        var lastBlock = this.block;
        this.block = EMPTY_BLOCK;
        mesh.vertexBlocks.add(lastBlock.flip());

        return mesh;
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
