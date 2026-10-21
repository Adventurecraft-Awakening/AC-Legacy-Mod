package dev.adventurecraft.awakening.client.render;

import dev.adventurecraft.awakening.client.render.resource.RenderTexture;
import dev.adventurecraft.awakening.client.render.state.DrawParameters;
import java.util.ArrayDeque;

/** Ordered native UI geometry. Only adjacent commands sharing material and clip are combined. */
public final class UiDrawList implements AutoCloseable {
    private static final ThreadLocal<ArrayDeque<QuadBuilder>> BUILDERS = ThreadLocal.withInitial(ArrayDeque::new);
    private static final ThreadLocal<ArrayDeque<QuadBuilder>> FRAME_BUILDERS = ThreadLocal.withInitial(ArrayDeque::new);
    @FunctionalInterface public interface Submitter {
        void submit(DrawParameters parameters, RenderTexture texture, java.nio.ByteBuffer vertices);
    }
    private final Submitter submitter;
    private final QuadBuilder vertices;
    private DrawParameters parameters;
    private NativeRenderer.MeshPass pass;
    private RenderTexture texture;
    private boolean closed;

    private UiDrawList(DrawParameters parameters, QuadBuilder vertices, Submitter submitter) {
        this.vertices = vertices;
        this.submitter = submitter;
        this.vertices.clear();
        this.parameters = parameters;
    }

    public static UiDrawList begin(DrawParameters parameters) {
        return begin(parameters, null);
    }

    public static UiDrawList begin(DrawParameters parameters, Submitter submitter) {
        var pool=submitter==null?FRAME_BUILDERS.get():BUILDERS.get();
        QuadBuilder vertices=pool.pollFirst();
        return new UiDrawList(parameters,vertices!=null?vertices:submitter==null?QuadBuilder.frameOwned(256):new QuadBuilder(256),submitter);
    }

    public void state(DrawParameters parameters, RenderTexture texture) {
        if (this.closed) throw new IllegalStateException("UI draw list is closed");
        if (!this.parameters.equals(parameters) || this.texture != texture) {
            this.flush();
            if (!this.parameters.equals(parameters)) {
                if (this.pass != null) this.pass.close();
                this.pass = null;
                this.parameters = parameters;
            }
            this.texture = texture;
        }
    }

    public void vertex(double x, double y, double z, double u, double v, int rgba) {
        this.vertices.color(rgba);
        this.vertices.vertexUV(x, y, z, u, v);
    }

    public void image(RenderTexture texture, double x, double y, double width, double height, double z,
                      float u0, float v0, float u1, float v1, float r, float g, float b, float a) {
        this.state(this.parameters, texture);
        this.vertices.color(r, g, b, a);
        this.vertices.vertexUV(x, y + height, z, u0, v1);
        this.vertices.vertexUV(x + width, y + height, z, u1, v1);
        this.vertices.vertexUV(x + width, y, z, u1, v0);
        this.vertices.vertexUV(x, y, z, u0, v0);
    }

    public void fill(double x, double y, double width, double height, float r, float g, float b, float a) {
        this.image(null, x, y, width, height, 0, 0, 0, 1, 1, r, g, b, a);
    }

    public void flush() {
        if (!this.vertices.isEmpty()) {
            if (this.submitter != null) this.submitter.submit(this.parameters, this.texture, this.vertices.vertices());
            else {
                if (this.pass == null) this.pass = NativeRenderer.pass(this.parameters);
                this.pass.drawQuads(this.vertices,this.texture);
            }
            this.vertices.clear();
        }
    }

    @Override public void close() {
        if (this.closed) return;
        try { this.flush(); }
        finally {
            this.closed = true;
            if (this.pass != null) this.pass.close();
            (submitter==null?FRAME_BUILDERS.get():BUILDERS.get()).addFirst(this.vertices);
        }
    }
}
