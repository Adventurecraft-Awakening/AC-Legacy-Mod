package dev.adventurecraft.awakening.mixin.block;

import dev.adventurecraft.awakening.common.AC_DebugMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.FireTile;

@Mixin(FireTile.class)
public abstract class MixinFireBlock {

    @Inject(method = "onScheduledTick", at = @At("HEAD"), cancellable = true)
    private void disableTickInDebugMode(Level i, int j, int k, int random, Random par5, CallbackInfo ci) {
        if (AC_DebugMode.active) {
            ci.cancel();
        }
    }
}
