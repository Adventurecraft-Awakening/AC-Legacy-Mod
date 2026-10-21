package dev.adventurecraft.awakening.client.render;

import dev.adventurecraft.awakening.client.render.vulkan.VulkanRenderer;
import java.nio.ByteBuffer;

/** Retained atlas extrusion geometry; item colors and poses remain per-draw inputs. */
public final class HeldItemMeshes {
    private static final RetainedMeshCache<Key> MESHES = new RetainedMeshCache<>();
    private HeldItemMeshes() { }

    public static VulkanRenderer.Mesh sprite(int sprite, int atlasWidth, int atlasHeight) {
        return MESHES.quads(new Key(sprite, atlasWidth, atlasHeight), () -> build(sprite, atlasWidth, atlasHeight));
    }

    public static ByteBuffer build(int sprite, int atlasWidth, int atlasHeight) {
        if (atlasWidth < 16 || atlasHeight < 16) throw new IllegalArgumentException("Item atlases need at least 16 pixels per axis");
        var size = new dev.adventurecraft.awakening.layout.Size(atlasWidth, atlasHeight);
        int w = size.w / 16;
        int h = size.h / 16;
        double rcpW = 0.5 / size.w;
        double rcpH = 0.5 / size.h;

        int texture = sprite;
        int texX = texture % 16 * 16;
        int texY = texture / 16 * 16;
        double u0 = (texX + 0.0) / 256.0;
        double u1 = (texX + 16.0) / 256.0;
        double v0 = (texY + 0.0) / 256.0;
        double v1 = (texY + 16.0) / 256.0;

        float x1 = 1.0F;
        float y1 = 0.3F;
        double z1 = -1.0 / 16.0;
        var ts = new QuadBuilder(8 + (w + h) * 8);

        ts.normal(0.0F, 0.0F, 1.0F);
        ts.vertexUV(0.0D, 0.0D, 0.0D, u1, v1);
        ts.vertexUV(x1, 0.0D, 0.0D, u0, v1);
        ts.vertexUV(x1, 1.0D, 0.0D, u0, v0);
        ts.vertexUV(0.0D, 1.0D, 0.0D, u1, v0);

        ts.normal(0.0F, 0.0F, -1.0F);
        ts.vertexUV(0.0D, 1.0D, z1, u1, v0);
        ts.vertexUV(x1, 1.0D, z1, u0, v0);
        ts.vertexUV(x1, 0.0D, z1, u0, v1);
        ts.vertexUV(0.0D, 0.0D, z1, u1, v1);

        ts.normal(-1.0F, 0.0F, 0.0F);
        for (int x = 0; x < w; ++x) {
            double x2 = x / (double) w;
            double u2 = u1 + (u0 - u1) * x2 - rcpW;
            double x0 = x1 * x2;
            ts.vertexUV(x0, 0.0D, z1, u2, v1);
            ts.vertexUV(x0, 0.0D, 0.0D, u2, v1);
            ts.vertexUV(x0, 1.0D, 0.0D, u2, v0);
            ts.vertexUV(x0, 1.0D, z1, u2, v0);
        }

        ts.normal(1.0F, 0.0F, 0.0F);
        for (int x = 0; x < w; ++x) {
            double x2 = x / (double) w;
            double u2 = u1 + (u0 - u1) * x2 - rcpW;
            double x0 = x1 * x2 + 1.0 / w;
            ts.vertexUV(x0, 1.0D, z1, u2, v0);
            ts.vertexUV(x0, 1.0D, 0.0D, u2, v0);
            ts.vertexUV(x0, 0.0D, 0.0D, u2, v1);
            ts.vertexUV(x0, 0.0D, z1, u2, v1);
        }

        ts.normal(0.0F, 1.0F, 0.0F);
        for (int y = 0; y < h; ++y) {
            double y2 = y / (double) h;
            double v2 = v1 + (v0 - v1) * y2 - rcpH;
            double y0 = x1 * y2 + 1.0 / h;
            ts.vertexUV(0.0D, y0, 0.0D, u1, v2);
            ts.vertexUV(x1, y0, 0.0D, u0, v2);
            ts.vertexUV(x1, y0, z1, u0, v2);
            ts.vertexUV(0.0D, y0, z1, u1, v2);
        }

        ts.normal(0.0F, -1.0F, 0.0F);
        for (int y = 0; y < h; ++y) {
            double y2 = y / (double) h;
            double v2 = v1 + (v0 - v1) * y2 - rcpH;
            double y0 = x1 * y2;
            ts.vertexUV(x1, y0, 0.0D, u0, v2);
            ts.vertexUV(0.0D, y0, 0.0D, u1, v2);
            ts.vertexUV(0.0D, y0, z1, u1, v2);
            ts.vertexUV(x1, y0, z1, u0, v2);
        }


        return ts.vertices();
    }
    private record Key(int sprite, int width, int height) { }
}
