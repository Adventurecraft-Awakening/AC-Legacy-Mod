package dev.adventurecraft.awakening.mixin.entity.decoration.painting;

import dev.adventurecraft.awakening.common.AC_DebugMode;
import net.minecraft.world.entity.item.Painting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Painting.class)
public abstract class MixinPaintingEntity {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void disableTick(CallbackInfo ci) {
        ci.cancel();
    }

    // Only breakable in debug mode!
    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void disableHurt(CallbackInfoReturnable<Boolean> ci) {
        if (!AC_DebugMode.active) {
            ci.setReturnValue(false);
        }
    }
}
