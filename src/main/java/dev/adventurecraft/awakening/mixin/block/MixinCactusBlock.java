package dev.adventurecraft.awakening.mixin.block;

import net.minecraft.block.CactusBlock;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(CactusBlock.class)
public abstract class MixinCactusBlock {

    @Inject(method = "onScheduledTick", at = @At("HEAD"), cancellable = true)
    private void disableTick(World i, int j, int k, int random, Random par5, CallbackInfo ci) {
        ci.cancel();
    }
}
