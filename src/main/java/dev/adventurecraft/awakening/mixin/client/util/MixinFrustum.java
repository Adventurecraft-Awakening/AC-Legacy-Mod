package dev.adventurecraft.awakening.mixin.client.util;

import dev.adventurecraft.awakening.extension.client.util.ExFrustum;
import net.minecraft.client.renderer.culling.FrustumData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(FrustumData.class)
public abstract class MixinFrustum implements ExFrustum {

    @Shadow public float[][] m_Frustum;

    @Overwrite
    public boolean cubeInFrustum(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        float[][] frustum = this.m_Frustum;
        for (int i = 0; i < 6; ++i) {
            float[] mat = frustum[i];
            double m0 = mat[0];
            double m1 = mat[1];
            double m2 = mat[2];
            double m3 = mat[3];

            double m0aX = m0 * minX;
            double m0bX = m0 * maxX;
            double m1aY = m1 * minY;
            double m1bY = m1 * maxY;
            double m2aZm3 = (m2 * minZ) + m3;
            double m2bZm3 = (m2 * maxZ) + m3;

            boolean b0 = m0aX + m1aY + m2aZm3 < 0.0;
            boolean b1 = m0bX + m1aY + m2aZm3 < 0.0;
            boolean b2 = m0aX + m1bY + m2aZm3 < 0.0;
            boolean b3 = m0bX + m1bY + m2aZm3 < 0.0;
            boolean b4 = m0aX + m1aY + m2bZm3 < 0.0;
            boolean b5 = m0bX + m1aY + m2bZm3 < 0.0;
            boolean b6 = m0aX + m1bY + m2bZm3 < 0.0;
            boolean b7 = m0bX + m1bY + m2bZm3 < 0.0;

            if (b0 && b1 && b2 && b3 && b4 && b5 && b6 && b7) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isBoxInFrustumFully(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        float[][] frustum = this.m_Frustum;
        for (int i = 0; i < 6; ++i) {
            float[] mat = frustum[i];
            double m0 = mat[0];
            double m1 = mat[1];
            double m2 = mat[2];
            double m3 = mat[3];

            double m0aX = m0 * minX;
            double m0bX = m0 * maxX;
            double m1aY = m1 * minY;
            double m1bY = m1 * maxY;
            double m2aZm3 = (m2 * minZ) + m3;
            double m2bZm3 = (m2 * maxZ) + m3;

            boolean b0 = m0aX + m1aY + m2aZm3 <= 0.0;
            boolean b1 = m0bX + m1aY + m2aZm3 <= 0.0;
            boolean b2 = m0aX + m1bY + m2aZm3 <= 0.0;
            boolean b3 = m0bX + m1bY + m2aZm3 <= 0.0;
            boolean b4 = m0aX + m1aY + m2bZm3 <= 0.0;
            boolean b5 = m0bX + m1aY + m2bZm3 <= 0.0;
            boolean b6 = m0aX + m1bY + m2bZm3 <= 0.0;
            boolean b7 = m0bX + m1bY + m2bZm3 <= 0.0;

            if (i < 4) {
                if (b0 || b1 || b2 || b3 || b4 || b5 || b6 || b7) {
                    return false;
                }
            }
            else if (b0 && b1 && b2 && b3 && b4 && b5 && b6 && b7) {
                return false;
            }
        }
        return true;
    }
}
