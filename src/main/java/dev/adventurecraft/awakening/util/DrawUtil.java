package dev.adventurecraft.awakening.util;

import dev.adventurecraft.awakening.layout.Border;
import dev.adventurecraft.awakening.layout.IntBorder;
import dev.adventurecraft.awakening.layout.IntCorner;
import dev.adventurecraft.awakening.layout.Rect;
import net.minecraft.client.renderer.Tesselator;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;

public final class DrawUtil {

    public static void beginFill(Tesselator ts) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        ts.begin();
    }

    public static void endFill(Tesselator ts) {
        ts.end();
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    public static void shadowRect(
        Tesselator ts,
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

    public static void drawRect(Tesselator ts, Rect rect, Border padding, IntBorder color) {
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

    public static void fillRect(Tesselator ts, Rect rect, IntCorner color, @Nullable Rect uv) {
        if (rect.isEmpty()) {
            return;
        }

        color(ts, color.botLeft());
        if (uv != null) {
            ts.tex(uv.left(), uv.bot());
        }
        ts.vertex(rect.left(), rect.bot(), 0.0);

        color(ts, color.botRight());
        if (uv != null) {
            ts.tex(uv.right(), uv.bot());
        }
        ts.vertex(rect.right(), rect.bot(), 0.0);

        color(ts, color.topRight());
        if (uv != null) {
            ts.tex(uv.right(), uv.top());
        }
        ts.vertex(rect.right(), rect.top(), 0.0);

        color(ts, color.topLeft());
        if (uv != null) {
            ts.tex(uv.left(), uv.top());
        }
        ts.vertex(rect.left(), rect.top(), 0.0);
    }

    public static void fillRect(Tesselator ts, Rect rect, int color) {
        if (rect.isEmpty()) {
            return;
        }

        color(ts, color);
        ts.vertex(rect.left(), rect.bot(), 0.0);
        ts.vertex(rect.right(), rect.bot(), 0.0);
        ts.vertex(rect.right(), rect.top(), 0.0);
        ts.vertex(rect.left(), rect.top(), 0.0);
    }

    private static void color(Tesselator ts, int rgba) {
        ts.color(rgba, rgba >>> 24);
    }
}
