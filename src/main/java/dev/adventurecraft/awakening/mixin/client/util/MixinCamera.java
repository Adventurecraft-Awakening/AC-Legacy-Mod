package dev.adventurecraft.awakening.mixin.client.util;

import dev.adventurecraft.awakening.extension.client.util.ExCameraView;
import dev.adventurecraft.awakening.extension.client.util.ExFrustum;
import net.minecraft.client.util.Camera;
import net.minecraft.client.util.Frustum;
import net.minecraft.util.math.AxixAlignedBoundingBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Camera.class)
public abstract class MixinCamera implements ExCameraView {

    @Shadow
    private Frustum frustum;
    @Shadow
    private double x;
    @Shadow
    private double y;
    @Shadow
    private double z;

    public boolean isBoxInFrustumFully(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return ((ExFrustum) this.frustum).isBoxInFrustumFully(minX - this.x, minY - this.y, minZ - this.z, maxX - this.x, maxY - this.y, maxZ - this.z);
    }

    public boolean isBoundingBoxInFrustumFully(AxixAlignedBoundingBox var1) {
        return this.isBoxInFrustumFully(var1.minX, var1.minY, var1.minZ, var1.maxX, var1.maxY, var1.maxZ);
    }
}
