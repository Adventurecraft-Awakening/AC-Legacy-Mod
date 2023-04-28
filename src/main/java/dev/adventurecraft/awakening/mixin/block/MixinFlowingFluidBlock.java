package dev.adventurecraft.awakening.mixin.block;

import dev.adventurecraft.awakening.common.AC_DebugMode;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(FlowingFluidBlock.class)
public abstract class MixinFlowingFluidBlock {

    @Inject(method = "onScheduledTick", at = @At("HEAD"), cancellable = true)
    private void disableTickInDebugMode(World i, int j, int k, int random, Random par5, CallbackInfo ci) {
        if (AC_DebugMode.active) {
            ci.cancel();
        }
    }
}
