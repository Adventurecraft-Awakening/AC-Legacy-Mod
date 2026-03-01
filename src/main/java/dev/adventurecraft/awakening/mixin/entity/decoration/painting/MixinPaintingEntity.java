package dev.adventurecraft.awakening.mixin.entity.decoration.painting;

import dev.adventurecraft.awakening.common.AC_DebugMode;
import dev.adventurecraft.awakening.common.Coord;
import dev.adventurecraft.awakening.extension.world.entity.item.ExPainting;
import net.minecraft.world.entity.item.Painting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Painting.class)
public abstract class MixinPaintingEntity implements ExPainting {

    @Shadow public int xTile;
    @Shadow public int yTile;
    @Shadow public int zTile;

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

    @Override
    public Coord getTilePos() {
        return new Coord(this.xTile, this.yTile, this.zTile);
    }
}
