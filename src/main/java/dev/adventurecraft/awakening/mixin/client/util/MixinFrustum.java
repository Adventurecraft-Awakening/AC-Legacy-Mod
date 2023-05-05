package dev.adventurecraft.awakening.mixin.client.util;

import dev.adventurecraft.awakening.extension.client.util.ExFrustum;
import net.minecraft.client.util.Frustum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Frustum.class)
public abstract class MixinFrustum implements ExFrustum {

    @Shadow
    public float[][] matrix;

    @Overwrite
    public boolean isInside(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        float aX = (float) minX;
        float aY = (float) minY;
        float aZ = (float) minZ;
        float bX = (float) maxX;
        float bY = (float) maxY;
        float bZ = (float) maxZ;

        for (int i = 0; i < 6; ++i) {
            float[] mat = this.matrix[i];
            float m0 = mat[0];
            float m1 = mat[1];
            float m2 = mat[2];
            float m3 = mat[3];

            float m0aX = m0 * aX;
            float m0bX = m0 * bX;
            float m1aY = m1 * aY;
            float m1bY = m1 * bY;
            float m2aZm3 = Math.fma(m2, aZ, m3);
            float m2bZm3 = Math.fma(m2, bZ, m3);

            boolean b0 = m0aX + m1aY + m2aZm3 < 0.0F;
            boolean b1 = m0bX + m1aY + m2aZm3 < 0.0F;
            boolean b2 = m0aX + m1bY + m2aZm3 < 0.0F;
            boolean b3 = m0bX + m1bY + m2aZm3 < 0.0F;
            boolean b4 = m0aX + m1aY + m2bZm3 < 0.0F;
            boolean b5 = m0bX + m1aY + m2bZm3 < 0.0F;
            boolean b6 = m0aX + m1bY + m2bZm3 < 0.0F;
            boolean b7 = m0bX + m1bY + m2bZm3 < 0.0F;

            if (b0 && b1 && b2 && b3 && b4 && b5 && b6 && b7) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isBoxInFrustumFully(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        float aX = (float) minX;
        float aY = (float) minY;
        float aZ = (float) minZ;
        float bX = (float) maxX;
        float bY = (float) maxY;
        float bZ = (float) maxZ;

        for (int i = 0; i < 6; ++i) {
            float[] mat = this.matrix[i];
            float m0 = mat[0];
            float m1 = mat[1];
            float m2 = mat[2];
            float m3 = mat[3];

            float m0aX = m0 * aX;
            float m0bX = m0 * bX;
            float m1aY = m1 * aY;
            float m1bY = m1 * bY;
            float m2aZm3 = Math.fma(m2, aZ, m3);
            float m2bZm3 = Math.fma(m2, bZ, m3);

            boolean b0 = m0aX + m1aY + m2aZm3 <= 0.0F;
            boolean b1 = m0bX + m1aY + m2aZm3 <= 0.0F;
            boolean b2 = m0aX + m1bY + m2aZm3 <= 0.0F;
            boolean b3 = m0bX + m1bY + m2aZm3 <= 0.0F;
            boolean b4 = m0aX + m1aY + m2bZm3 <= 0.0F;
            boolean b5 = m0bX + m1aY + m2bZm3 <= 0.0F;
            boolean b6 = m0aX + m1bY + m2bZm3 <= 0.0F;
            boolean b7 = m0bX + m1bY + m2bZm3 <= 0.0F;

            if (i < 4) {
                if (b0 || b1 || b2 || b3 || b4 || b5 || b6 || b7) {
                    return false;
                }
            } else if (b0 && b1 && b2 && b3 && b4 && b5 && b6 && b7) {
                return false;
            }
        }

        return true;
    }
}
