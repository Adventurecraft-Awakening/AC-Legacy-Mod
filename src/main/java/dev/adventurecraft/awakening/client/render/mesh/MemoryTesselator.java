package dev.adventurecraft.awakening.client.render.mesh;

import dev.adventurecraft.awakening.client.render.QuadBuilder;
import dev.adventurecraft.awakening.client.render.VertexFormat;
import dev.adventurecraft.awakening.client.render.VertexPacking;
import dev.adventurecraft.awakening.client.render.VertexSink;
import dev.adventurecraft.awakening.image.Rgba;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public final class MemoryTesselator implements VertexSink {

    public static final int BYTE_STRIDE = QuadBuilder.STRIDE;

    private static final ByteBuffer EMPTY_BLOCK = ByteBuffer.allocateDirect(0).order(ByteOrder.nativeOrder());

    private BlockAllocator allocator;
    private ByteBuffer block;
    private ArrayList<ByteBuffer> blocks;

    public boolean tesselating;

    private float u;
    private float v;
    private int rgba;
    private int normal;
    private int material;
    private int skyLight = -1;
    private int surfaceFlags;

    private float xo;
    private float yo;
    private float zo;

    private MemoryTesselator() {
        this.block = EMPTY_BLOCK;
        this.blocks = new ArrayList<>();
    }

    public static MemoryTesselator create() {
        return new MemoryTesselator();
    }

    public void setAllocator(BlockAllocator allocator) {
        this.clear();
        this.allocator = allocator;
    }

    public boolean isEmpty() {
        return this.block == EMPTY_BLOCK && this.blocks.isEmpty();
    }

    /** Written bytes before ownership is transferred by takeMesh(). */
    public long sizeInBytes() {
        long size = this.block.position();
        for (ByteBuffer buffer : this.blocks) size += buffer.position();
        return size;
    }

    public void begin() {
        if (this.tesselating) throw new IllegalStateException("Already building a mesh");
        this.tesselating = true;
        this.material = 0;
        this.skyLight = -1;
        this.surfaceFlags = 0;
    }

    public void end() {
        if (!this.tesselating) {
            throw new IllegalStateException("Not tesselating!");
        }
        this.tesselating = false;
    }

    public void clear() {
        this.skyLight = -1;
        this.surfaceFlags = 0;
        if (this.allocator == null) {
            return;
        }

        this.blocks.forEach(this.allocator::returnBlock);
        this.blocks.clear();

        ByteBuffer lastBlock = this.block;
        this.block = EMPTY_BLOCK;
        if (lastBlock != EMPTY_BLOCK) {
            this.allocator.returnBlock(lastBlock);
        }
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
        this.ac$color8(Rgba.saturateFromRgba8((int)(r*255),(int)(g*255),(int)(b*255),(int)(a*255)));
    }

    public @Override void ac$vertexUV(float x, float y, float z, float u, float v) {
        ByteBuffer a = this.reserve(BYTE_STRIDE);
        int p = a.position();
        a.position(p + BYTE_STRIDE);

        int color = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN ? this.rgba : Integer.reverseBytes(this.rgba);
        VertexFormat.write(a, p, x + this.xo, y + this.yo, z + this.zo, u, v, color,
            this.normal, this.material, this.skyLight, this.surfaceFlags);
    }

    public @Override void ac$material(int material) {
        this.material = material;
    }

    public @Override void ac$skyLight(int skyLight) {
        this.skyLight = skyLight;
    }

    public @Override void ac$surfaceFlags(int flags) {
        this.surfaceFlags = flags;
    }

    public @Override void normal(float x, float y, float z) {
        this.ac$normal8(VertexPacking.packByteNormal(x, y, z));
    }

    public @Override void ac$normal8(int xyz) {
        this.normal = xyz;
    }

    public @Override void ac$normal32(float x, float y, float z) {
        this.normal(x, y, z);
    }

    public void offset(double x, double y, double z) {
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

        ByteBuffer fullBlock = this.block;
        this.block = EMPTY_BLOCK;
        if (fullBlock != EMPTY_BLOCK) {
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

        ByteBuffer lastBlock = this.block;
        this.block = EMPTY_BLOCK;
        if (lastBlock != EMPTY_BLOCK) {
            mesh.vertexBlocks.add(lastBlock.flip());
        }
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
