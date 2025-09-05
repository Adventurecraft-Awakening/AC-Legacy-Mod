package dev.adventurecraft.awakening.mixin.block;

import dev.adventurecraft.awakening.extension.world.ExWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.LiquidTileDynamic;

@Mixin(LiquidTileDynamic.class)
public abstract class MixinFlowingFluidBlock {

    @Inject(
        method = "tick",
        at = @At("HEAD"),
        cancellable = true
    )
    private void disableTickInDebugMode(Level level, int j, int k, int random, Random par5, CallbackInfo ci) {
        if (((ExWorld) level).isDebugMode()) {
            ci.cancel();
        }
    }
}
