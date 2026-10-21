package dev.adventurecraft.awakening.util;

import dev.adventurecraft.awakening.client.render.VertexSink;
import dev.adventurecraft.awakening.layout.Border;
import dev.adventurecraft.awakening.layout.IntBorder;
import dev.adventurecraft.awakening.layout.IntCorner;
import dev.adventurecraft.awakening.layout.Rect;
import net.minecraft.client.renderer.Tesselator;

import javax.annotation.Nullable;

public final class DrawUtil {

    public static void shadowRect(
        VertexSink ts,
        Rect rect,
        Border padding,
        IntCorner frontColor,
        IntCorner backColor,
        @Nullable Rect frontUv,
        @Nullable Rect backUv
    ) {
        // Only shrink border for negative values i.e. expand.
        fillRect(ts, rect.shrink(padding.min(Border.zero)), frontColor, frontUv);

        // Only shrink background for positive values.
        fillRect(ts, rect.shrink(padding.max(Border.zero)), backColor, backUv);
    }

    public static void drawRect(VertexSink ts, Rect rect, Border padding, IntBorder color) {
        var offset = new Border(padding.left, padding.right, 0, 0);

        Rect left = rect.alongLeft(padding.left);
        Rect top = rect.alongTop(padding.top).shrink(offset);
        Rect right = rect.alongRight(padding.right);
        Rect bot = rect.alongBot(padding.bot).shrink(offset);

        fillRect(ts, left, color.left);
        fillRect(ts, top, color.top);
        fillRect(ts, right, color.right);
        fillRect(ts, bot, color.bot);
    }

    public static void fillRect(VertexSink ts, Rect rect, IntCorner color, @Nullable Rect uv) {
        if (rect.isEmpty()) {
            return;
        }

        color(ts, color.botLeft());
        if (uv != null) {
            ts.ac$tex((float) uv.left(), (float) uv.bot());
        }
        vertex(ts, rect.left(), rect.bot(), 0.0);

        color(ts, color.botRight());
        if (uv != null) {
            ts.ac$tex((float) uv.right(), (float) uv.bot());
        }
        vertex(ts, rect.right(), rect.bot(), 0.0);

        color(ts, color.topRight());
        if (uv != null) {
            ts.ac$tex((float) uv.right(), (float) uv.top());
        }
        vertex(ts, rect.right(), rect.top(), 0.0);

        color(ts, color.topLeft());
        if (uv != null) {
            ts.ac$tex((float) uv.left(), (float) uv.top());
        }
        vertex(ts, rect.left(), rect.top(), 0.0);
    }

    public static void fillRect(VertexSink ts, Rect rect, int color) {
        if (rect.isEmpty()) {
            return;
        }

        color(ts, color);
        vertex(ts, rect.left(), rect.bot(), 0.0);
        vertex(ts, rect.right(), rect.bot(), 0.0);
        vertex(ts, rect.right(), rect.top(), 0.0);
        vertex(ts, rect.left(), rect.top(), 0.0);
    }

    private static void color(VertexSink ts, int rgba) {
        ts.ac$color8(rgba);
    }

    private static void vertex(VertexSink sink, double x, double y, double z) {
        sink.ac$vertex((float) x, (float) y, (float) z);
    }

    /** Boundary overloads for inherited tessellator callbacks. */
    public static void shadowRect(Tesselator ts, Rect rect, Border padding, IntCorner frontColor,
                                  IntCorner backColor, @Nullable Rect frontUv, @Nullable Rect backUv) {
        shadowRect((VertexSink) ts, rect, padding, frontColor, backColor, frontUv, backUv);
    }

    public static void drawRect(Tesselator ts, Rect rect, Border padding, IntBorder color) {
        drawRect((VertexSink) ts, rect, padding, color);
    }

    public static void fillRect(Tesselator ts, Rect rect, IntCorner color, @Nullable Rect uv) {
        fillRect((VertexSink) ts, rect, color, uv);
    }

    public static void fillRect(Tesselator ts, Rect rect, int color) {
        fillRect((VertexSink) ts, rect, color);
    }

    public static void fillCube(Tesselator ts, double x0, double y0, double z0, double x1, double y1, double z1) {
        fillCube((VertexSink) ts, x0, y0, z0, x1, y1, z1);
    }

    public static void fillCube(VertexSink ts, double x0, double y0, double z0, double x1, double y1, double z1) {
        vertex(ts, x0, y1, z0);
        vertex(ts, x1, y1, z0);
        vertex(ts, x1, y0, z0);
        vertex(ts, x0, y0, z0);

        vertex(ts, x0, y0, z1);
        vertex(ts, x1, y0, z1);
        vertex(ts, x1, y1, z1);
        vertex(ts, x0, y1, z1);

        vertex(ts, x0, y0, z0);
        vertex(ts, x1, y0, z0);
        vertex(ts, x1, y0, z1);
        vertex(ts, x0, y0, z1);

        vertex(ts, x0, y1, z1);
        vertex(ts, x1, y1, z1);
        vertex(ts, x1, y1, z0);
        vertex(ts, x0, y1, z0);

        vertex(ts, x0, y0, z1);
        vertex(ts, x0, y1, z1);
        vertex(ts, x0, y1, z0);
        vertex(ts, x0, y0, z0);

        vertex(ts, x1, y0, z0);
        vertex(ts, x1, y1, z0);
        vertex(ts, x1, y1, z1);
        vertex(ts, x1, y0, z1);
    }
}
