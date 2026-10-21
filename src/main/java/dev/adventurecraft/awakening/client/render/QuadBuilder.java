package dev.adventurecraft.awakening.client.render;

import dev.adventurecraft.awakening.image.Rgba;
import dev.adventurecraft.awakening.client.render.vulkan.VulkanRenderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/** Vertex writer with reusable CPU storage or direct mapped storage owned by the active frame. */
public final class QuadBuilder implements VertexSink {
    public static final int STRIDE = VertexFormat.STRIDE;
    private ByteBuffer vertices;
    private final boolean frameOwned;
    private final int initialVertexCount;
    private VulkanRenderer.StreamReservation reservation;
    private boolean committed;
    private int vertexCount;
    private final java.util.List<Segment> segments;
    private record Segment(VulkanRenderer.StreamReservation reservation,int vertices) { }
    private int color = -1;
    private int normal = VertexPacking.packByteNormal(0, 0, 1);
    private double offsetX, offsetY, offsetZ;
    private float u, v;

    public QuadBuilder(int initialVertexCount) { this(initialVertexCount,false); }

    private QuadBuilder(int initialVertexCount,boolean frameOwned) {
        if(initialVertexCount<0) throw new IllegalArgumentException("Vertex capacity must not be negative");
        this.initialVertexCount=frameOwned?Math.toIntExact(((long)Math.max(1,initialVertexCount)+3)/4*4):Math.max(1,initialVertexCount);
        this.frameOwned=frameOwned; this.segments=frameOwned?new java.util.ArrayList<>():java.util.List.of();
        if(!frameOwned) this.vertices=ByteBuffer.allocateDirect(Math.multiplyExact(initialVertexCount,STRIDE)).order(ByteOrder.nativeOrder());
    }

    /** Native UI writers reserve mapped frame storage lazily, without an intermediate CPU buffer. */
    public static QuadBuilder frameOwned(int initialVertexCount) { return new QuadBuilder(initialVertexCount,true); }
    public boolean isFrameOwned() { return frameOwned; }
    /** Emits ordered mapped packets; full segments are never recopied during growth. */
    public void streamQuads(java.util.function.Consumer<VulkanRenderer.StreamGeometry> submit) { stream(submit,true); }
    public void streamLines(java.util.function.Consumer<VulkanRenderer.StreamGeometry> submit) { stream(submit,false); }
    private void stream(java.util.function.Consumer<VulkanRenderer.StreamGeometry> submit,boolean quads) {
        if(!frameOwned) throw new IllegalStateException("Builder uses CPU storage");
        if(committed || isEmpty() || vertexCount%(quads?4:2)!=0) throw new IllegalStateException("Incomplete or already submitted geometry");
        committed=true;
        for(var segment:segments) submit.accept(quads?segment.reservation.quads(segment.vertices):
            segment.reservation.mesh(segment.vertices,VulkanRenderer.PrimitiveTopology.LINES));
        int count=vertices.position()/STRIDE;
        submit.accept(quads?reservation.quads(count):reservation.mesh(count,VulkanRenderer.PrimitiveTopology.LINES));
    }

    public void clear() {
        if(frameOwned) { vertices=null; reservation=null; committed=false; segments.clear(); }
        else this.vertices.clear();
        vertexCount=0;
        this.color = -1;
        this.normal = VertexPacking.packByteNormal(0, 0, 1);
        this.offsetX = this.offsetY = this.offsetZ = 0;
        this.u = this.v = 0;
    }

    public void color(float r, float g, float b, float a) {
        this.color = Rgba.fromRgba8(channel(r), channel(g), channel(b), channel(a));
    }

    private static int channel(float value) {
        return Math.clamp((int) (value * 255), 0, 255);
    }

    public void color(int rgba) { this.color = rgba; }

    public void normal(float x, float y, float z) {
        this.normal = VertexPacking.packByteNormal(x, y, z);
    }

    public void offset(double x, double y, double z) {
        this.offsetX = x;
        this.offsetY = y;
        this.offsetZ = z;
    }

    public void vertexUV(double x, double y, double z, double u, double v) {
        if(committed) throw new IllegalStateException("Clear the submitted frame builder before writing again");
        if(frameOwned && reservation!=null) reservation.checkWritable();
        if(vertices==null) {
            reservation=RenderSystem.renderer().reserveStream(initialVertexCount);
            vertices=reservation.vertices();
        }
        if(this.vertices.remaining()<STRIDE) {
            int capacity=Math.max(STRIDE*4,Math.multiplyExact(vertices.capacity(),2));
            ByteBuffer larger;
            if(frameOwned) {
                var next=RenderSystem.renderer().reserveStream(capacity/STRIDE);
                larger=next.vertices();
                segments.add(new Segment(reservation,vertices.position()/STRIDE));
                reservation=next;
            } else {
                larger=ByteBuffer.allocateDirect(capacity).order(ByteOrder.nativeOrder());
                larger.put(vertices.duplicate().flip());
            }
            vertices=larger;
        }
        int at = this.vertices.position();
        VertexFormat.write(this.vertices, at, (float) (x + this.offsetX), (float) (y + this.offsetY),
            (float) (z + this.offsetZ), (float) u, (float) v, this.color, this.normal);
        this.vertices.position(at + STRIDE);
        vertexCount++;
    }

    @Override public void ac$vertex(float x, float y, float z) { this.vertexUV(x, y, z, this.u, this.v); }
    @Override public void ac$vertexUV(float x, float y, float z, float u, float v) { this.vertexUV(x, y, z, (double) u, v); }
    @Override public void ac$tex(float u, float v) { this.u = u; this.v = v; }
    @Override public void ac$color8(int rgba) { this.color(rgba); }
    @Override public void ac$color32(float r, float g, float b, float a) { this.color(r, g, b, a); }
    @Override public void ac$normal8(int xyz) { this.normal = xyz; }
    @Override public void ac$normal32(float x, float y, float z) { this.normal(x, y, z); }

    public boolean isEmpty() {
        return vertexCount==0;
    }

    public ByteBuffer records() {
        if(!segments.isEmpty()) throw new IllegalStateException("Submit frame segments through a native pass");
        if(vertices==null) return ByteBuffer.allocate(0).asReadOnlyBuffer();
        return this.vertices.duplicate().order(ByteOrder.nativeOrder()).flip();
    }

    public ByteBuffer vertices() {
        if(!segments.isEmpty()) throw new IllegalStateException("Submit frame segments through a native pass");
        if (vertices!=null && this.vertices.position() % (4 * STRIDE) != 0) {
            throw new IllegalStateException("Incomplete quad");
        }
        if(vertices==null) return ByteBuffer.allocate(0).asReadOnlyBuffer();
        return this.vertices.duplicate().order(ByteOrder.nativeOrder()).flip();
    }
}
