package dev.adventurecraft.awakening.mixin.block;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.CactusTile;

@Mixin(CactusTile.class)
public abstract class MixinCactusBlock {

    @Inject(method = "onScheduledTick", at = @At("HEAD"), cancellable = true)
    private void disableTick(Level i, int j, int k, int random, Random par5, CallbackInfo ci) {
        ci.cancel();
    }
}
