package dev.adventurecraft.awakening.mixin.client.util;

import dev.adventurecraft.awakening.extension.client.util.ExFrustum;
import net.minecraft.client.util.Frustum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Frustum.class)
public abstract class MixinFrustum implements ExFrustum {

    @Shadow
    public float[][] matrix;

    @Override
    public boolean isBoxInFrustumFully(double var1, double var3, double var5, double var7, double var9, double var11) {
        for (int var13 = 0; var13 < 6; ++var13) {
            float var14 = (float) var1;
            float var15 = (float) var3;
            float var16 = (float) var5;
            float var17 = (float) var7;
            float var18 = (float) var9;
            float var19 = (float) var11;
            if (var13 < 4) {
                if (this.matrix[var13][0] * var14 + this.matrix[var13][1] * var15 + this.matrix[var13][2] * var16 + this.matrix[var13][3] <= 0.0F || this.matrix[var13][0] * var17 + this.matrix[var13][1] * var15 + this.matrix[var13][2] * var16 + this.matrix[var13][3] <= 0.0F || this.matrix[var13][0] * var14 + this.matrix[var13][1] * var18 + this.matrix[var13][2] * var16 + this.matrix[var13][3] <= 0.0F || this.matrix[var13][0] * var17 + this.matrix[var13][1] * var18 + this.matrix[var13][2] * var16 + this.matrix[var13][3] <= 0.0F || this.matrix[var13][0] * var14 + this.matrix[var13][1] * var15 + this.matrix[var13][2] * var19 + this.matrix[var13][3] <= 0.0F || this.matrix[var13][0] * var17 + this.matrix[var13][1] * var15 + this.matrix[var13][2] * var19 + this.matrix[var13][3] <= 0.0F || this.matrix[var13][0] * var14 + this.matrix[var13][1] * var18 + this.matrix[var13][2] * var19 + this.matrix[var13][3] <= 0.0F || this.matrix[var13][0] * var17 + this.matrix[var13][1] * var18 + this.matrix[var13][2] * var19 + this.matrix[var13][3] <= 0.0F) {
                    return false;
                }
            } else if (this.matrix[var13][0] * var14 + this.matrix[var13][1] * var15 + this.matrix[var13][2] * var16 + this.matrix[var13][3] <= 0.0F && this.matrix[var13][0] * var17 + this.matrix[var13][1] * var15 + this.matrix[var13][2] * var16 + this.matrix[var13][3] <= 0.0F && this.matrix[var13][0] * var14 + this.matrix[var13][1] * var18 + this.matrix[var13][2] * var16 + this.matrix[var13][3] <= 0.0F && this.matrix[var13][0] * var17 + this.matrix[var13][1] * var18 + this.matrix[var13][2] * var16 + this.matrix[var13][3] <= 0.0F && this.matrix[var13][0] * var14 + this.matrix[var13][1] * var15 + this.matrix[var13][2] * var19 + this.matrix[var13][3] <= 0.0F && this.matrix[var13][0] * var17 + this.matrix[var13][1] * var15 + this.matrix[var13][2] * var19 + this.matrix[var13][3] <= 0.0F && this.matrix[var13][0] * var14 + this.matrix[var13][1] * var18 + this.matrix[var13][2] * var19 + this.matrix[var13][3] <= 0.0F && this.matrix[var13][0] * var17 + this.matrix[var13][1] * var18 + this.matrix[var13][2] * var19 + this.matrix[var13][3] <= 0.0F) {
                return false;
            }
        }

        return true;
    }
}
