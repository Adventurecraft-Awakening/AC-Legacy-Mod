package dev.adventurecraft.awakening.client.render;

import dev.adventurecraft.awakening.client.render.state.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** One ordered debug overlay collector shared by entity renderers. */
public final class DebugDraw implements AutoCloseable {
    private static final ThreadLocal<DebugDraw> CURRENT = new ThreadLocal<>();
    private static final ThreadLocal<java.util.ArrayDeque<QuadBuilder>> BUILDERS = ThreadLocal.withInitial(java.util.ArrayDeque::new);
    private final DebugDraw previous;
    private final DrawParameters parameters;
    private QuadBuilder vertices;
    private float width = 1;
    private boolean depthWrite;
    private boolean closed;

    private DebugDraw(DrawParameters parameters) {
        this.parameters = parameters.withObject(parameters.object().withTint(Color4.WHITE)).withMaterial(MaterialState.UNLIT).withRaster(parameters.raster()
            .withBlend(BlendState.ALPHA).withCull(CullMode.NONE));
        this.depthWrite = parameters.raster().depthWrite();
        this.previous = CURRENT.get();
        CURRENT.set(this);
    }

    public static DebugDraw begin(DrawParameters parameters) { return new DebugDraw(parameters); }
    public static DebugDraw current() { return CURRENT.get(); }
    public static void with(Supplier<DrawParameters> entryState, Consumer<DebugDraw> draw) {
        var current = CURRENT.get();
        if (current != null) draw.accept(current);
        else try (var root = begin(entryState.get())) { draw.accept(root); }
    }

    public void line(double x0, double y0, double z0, double x1, double y1, double z1,
                     int rgba0, int rgba1, float width, boolean depthWrite) {
        if (this.closed) throw new IllegalStateException("Debug collector is closed");
        if (this.width != width || this.depthWrite != depthWrite) {
            this.flush(); this.width = width; this.depthWrite = depthWrite;
        }
        if (this.vertices == null) {
            this.vertices = BUILDERS.get().pollFirst();
            if (this.vertices == null) this.vertices = new QuadBuilder(256);
            this.vertices.clear();
        }
        this.vertices.color(rgba0); this.vertices.vertexUV(x0, y0, z0, 0, 0);
        this.vertices.color(rgba1); this.vertices.vertexUV(x1, y1, z1, 0, 0);
    }

    public void box(double x0, double y0, double z0, double x1, double y1, double z1,
                    int rgba, float width, boolean depthWrite) {
        double[][] points = {{x0,y0,z0},{x1,y0,z0},{x1,y1,z0},{x0,y1,z0},
            {x0,y0,z1},{x1,y0,z1},{x1,y1,z1},{x0,y1,z1}};
        int[] edges = {0,1,1,2,2,3,3,0,4,5,5,6,6,7,7,4,0,4,1,5,2,6,3,7};
        for (int i = 0; i < edges.length; i += 2) {
            double[] a = points[edges[i]], b = points[edges[i + 1]];
            this.line(a[0],a[1],a[2],b[0],b[1],b[2],rgba,rgba,width,depthWrite);
        }
    }

    public void flush() {
        if (this.vertices == null || this.vertices.isEmpty()) return;
        var state = this.parameters.withRaster(this.parameters.raster()
            .withDepth(this.parameters.raster().depthTest(), this.depthWrite).withLineWidth(this.width));
        try (var pass = NativeRenderer.pass(state)) { pass.drawLines(this.vertices.records(), null); }
        this.vertices.clear();
    }

    @Override public void close() {
        if (this.closed) return;
        try { this.flush(); }
        finally {
            this.closed = true; CURRENT.set(this.previous);
            if (this.vertices != null) BUILDERS.get().addFirst(this.vertices);
        }
    }
}
