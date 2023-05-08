package dev.adventurecraft.awakening.extension.client.model;

import net.minecraft.client.render.QuadPoint;
import net.minecraft.client.render.TexturedQuad;

public interface ExTexturedQuad {

    static TexturedQuad create(QuadPoint[] var1, int var2, int var3, int var4, int var5, int var6, int var7) {
        var quad = new TexturedQuad(var1);
        float var8 = 0.0015625F;
        float var9 = 0.003125F;
        if (var4 < var2) {
            var8 = -var8;
        }

        if (var5 < var3) {
            var9 = -var9;
        }

        var1[0] = var1[0].method_983((float) var4 / (float) var6 - var8, (float) var3 / (float) var7 + var9);
        var1[1] = var1[1].method_983((float) var2 / (float) var6 + var8, (float) var3 / (float) var7 + var9);
        var1[2] = var1[2].method_983((float) var2 / (float) var6 + var8, (float) var5 / (float) var7 - var9);
        var1[3] = var1[3].method_983((float) var4 / (float) var6 - var8, (float) var5 / (float) var7 - var9);
        return quad;
    }

    static TexturedQuad create(QuadPoint[] var1, int var2, int var3, int var4, int var5) {
        return create(var1, var2, var3, var4, var5, 64, 32);
    }
}
