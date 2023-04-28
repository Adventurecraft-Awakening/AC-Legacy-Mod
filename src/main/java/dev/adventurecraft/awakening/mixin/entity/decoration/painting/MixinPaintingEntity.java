package dev.adventurecraft.awakening.mixin.entity.decoration.painting;

import net.minecraft.entity.decoration.painting.PaintingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PaintingEntity.class)
public abstract class MixinPaintingEntity {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void disableTick(CallbackInfo ci) {
        ci.cancel();
    }
}
