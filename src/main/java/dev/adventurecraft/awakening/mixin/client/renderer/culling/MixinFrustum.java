package dev.adventurecraft.awakening.mixin.client.renderer.culling;

import dev.adventurecraft.awakening.mixin.client.util.MixinFrustumData;
import net.minecraft.client.renderer.culling.Frustum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.adventurecraft.awakening.client.util.FrustumConstants.*;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

@Mixin(Frustum.class)
public abstract class MixinFrustum extends MixinFrustumData {

    @Inject(
        method = "calculateFrustum",
        at = @At("TAIL")
    )
    private void flattenFrustum(CallbackInfo ci) {
        var ty = ValueLayout.JAVA_FLOAT;
        for (int i = 0; i < PLANES; i++) {
            long dst = i * DIMS * ty.byteSize();
            MemorySegment.copy(this.m_Frustum[i], 0, this.frustumPlanes, ty, dst, DIMS);
        }
    }
}
