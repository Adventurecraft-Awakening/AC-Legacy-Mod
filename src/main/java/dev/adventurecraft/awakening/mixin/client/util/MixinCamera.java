package dev.adventurecraft.awakening.mixin.client.util;

import dev.adventurecraft.awakening.extension.client.util.ExCameraView;
import dev.adventurecraft.awakening.extension.client.util.ExFrustum;
import net.minecraft.client.renderer.culling.FrustumCuller;
import net.minecraft.client.renderer.culling.FrustumData;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(FrustumCuller.class)
public abstract class MixinCamera implements ExCameraView {

    @Shadow
    private FrustumData frustum;
    @Shadow
    private double x;
    @Shadow
    private double y;
    @Shadow
    private double z;

    public boolean isBoxInFrustumFully(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return ((ExFrustum) this.frustum).isBoxInFrustumFully(minX - this.x, minY - this.y, minZ - this.z, maxX - this.x, maxY - this.y, maxZ - this.z);
    }

    public boolean isBoundingBoxInFrustumFully(AABB var1) {
        return this.isBoxInFrustumFully(var1.x0, var1.y0, var1.z0, var1.x1, var1.y1, var1.z1);
    }
}
