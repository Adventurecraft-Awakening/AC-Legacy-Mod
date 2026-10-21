package dev.adventurecraft.awakening.client.render;

import dev.adventurecraft.awakening.client.render.resource.RenderTexture;
import dev.adventurecraft.awakening.client.render.state.DrawParameters;
import dev.adventurecraft.awakening.common.TextRendererState;
import dev.adventurecraft.awakening.client.render.VertexSink;
import dev.adventurecraft.awakening.extension.client.render.ExTextRenderer;
import dev.adventurecraft.awakening.image.Rgba;
import dev.adventurecraft.awakening.layout.IntCorner;
import dev.adventurecraft.awakening.layout.Rect;
import net.minecraft.client.gui.Font;
import java.util.function.Function;

/** One explicit UI root, shared by translated and clipped descendants in painter order. */
public final class UiDrawContext implements AutoCloseable {
    private final UiDrawList list;
    private final DrawParameters parameters;
    private final Font font;
    private final Function<String, RenderTexture> textures;
    private final RenderTexture fontTexture;
    private final double x, y;
    private final boolean owner;

    public UiDrawContext(DrawParameters parameters, Font font, Function<String, RenderTexture> textures, RenderTexture fontTexture) {
        this(UiDrawList.begin(parameters), parameters, font, textures, fontTexture, 0, 0, true);
    }

    public UiDrawContext(DrawParameters parameters, Font font, Function<String, RenderTexture> textures,
                         RenderTexture fontTexture, UiDrawList.Submitter submitter) {
        this(UiDrawList.begin(parameters, submitter), parameters, font, textures, fontTexture, 0, 0, true);
    }

    private UiDrawContext(UiDrawList list, DrawParameters parameters, Font font,
                          Function<String, RenderTexture> textures, RenderTexture fontTexture, double x, double y, boolean owner) {
        this.list = list; this.parameters = parameters; this.font = font;
        this.textures = textures; this.fontTexture = fontTexture; this.x = x; this.y = y; this.owner = owner;
    }

    public DrawParameters parameters() { return this.parameters; }
    public Font font() { return this.font; }
    public RenderTexture fontTexture() { return this.fontTexture; }
    public RenderTexture texture(String name) { return this.textures.apply(name); }
    public double offsetX() { return this.x; }
    public double offsetY() { return this.y; }

    public UiDrawContext translated(double x, double y) {
        return new UiDrawContext(this.list, this.parameters, this.font, this.textures, this.fontTexture, this.x + x, this.y + y, false);
    }

    public UiDrawContext translated(double x, double y, double z) {
        var state = this.parameters.withObject(this.parameters.object().withModel(this.parameters.object().model().translated(0, 0, (float) z)));
        return new UiDrawContext(this.list, state, this.font, this.textures, this.fontTexture, this.x + x, this.y + y, false);
    }

    public UiDrawContext withParameters(DrawParameters parameters) {
        return new UiDrawContext(this.list, parameters, this.font, this.textures, this.fontTexture, this.x, this.y, false);
    }

    public UiDrawContext clip(Rect rectangle) {
        var projected = new CameraView(this.parameters).projectRect(rectangle.offset(this.x, this.y));
        var old = this.parameters.raster().scissor();
        if (old != null) projected = CameraView.intersect(projected, old);
        var clipped = this.parameters.withRaster(this.parameters.raster().withScissor(projected));
        return new UiDrawContext(this.list, clipped, this.font, this.textures, this.fontTexture, this.x, this.y, false);
    }

    public void image(RenderTexture texture, Rect rectangle, Rect uv, IntCorner colors) {
        if (rectangle.isEmpty()) return;
        this.list.state(this.parameters, texture);
        double left = rectangle.left() + this.x, right = rectangle.right() + this.x;
        double top = rectangle.top() + this.y, bottom = rectangle.bot() + this.y;
        this.list.vertex(left, bottom, 0, uv.left(), uv.bot(), colors.botLeft());
        this.list.vertex(right, bottom, 0, uv.right(), uv.bot(), colors.botRight());
        this.list.vertex(right, top, 0, uv.right(), uv.top(), colors.topRight());
        this.list.vertex(left, top, 0, uv.left(), uv.top(), colors.topLeft());
    }

    public void fill(Rect rectangle, int rgba) { this.fill(rectangle, new IntCorner(rgba)); }
    public void fill(Rect rectangle, IntCorner colors) { this.image(null, rectangle, new Rect(0, 0, 1, 1), colors); }

    public TextRendererState textState() {
        var state = ((ExTextRenderer) this.font).createState();
        state.begin(this);
        return state;
    }

    public void text(String text, float x, float y, int rgba, boolean shadow) {
        var state = ((ExTextRenderer) this.font).createState();
        state.setColor(rgba);
        if (shadow) state.setShadowToColor();
        state.setShadowOffset(1, 1);
        state.begin(this);
        state.drawText(text, x, y);
        state.end();
    }

    public VertexSink vertexWriter(RenderTexture texture) {
        return new VertexSink() {
            private int rgba = Rgba.WHITE;
            private float u, v;
            public void ac$vertex(float vx, float vy, float vz) { this.ac$vertexUV(vx, vy, vz, this.u, this.v); }
            public void ac$vertexUV(float vx, float vy, float vz, float u, float v) {
                list.state(parameters, texture);
                list.vertex(vx + x, vy + y, vz, u, v, this.rgba);
            }
            public void ac$tex(float u, float v) { this.u = u; this.v = v; }
            public void ac$color8(int rgba) { this.rgba = rgba; }
            public void ac$color32(float r, float g, float b, float a) {
                this.rgba = Rgba.fromRgba8(Rgba.saturateToByte(r), Rgba.saturateToByte(g), Rgba.saturateToByte(b), Rgba.saturateToByte(a));
            }
            public void ac$normal8(int xyz) { }
            public void ac$normal32(float x, float y, float z) { }
        };
    }

    public void flush() { this.list.flush(); }
    @Override public void close() { if (this.owner) this.list.close(); }
}
