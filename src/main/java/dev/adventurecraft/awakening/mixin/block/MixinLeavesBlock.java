package dev.adventurecraft.awakening.mixin.block;

import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import net.minecraft.block.LeavesBlock;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(LeavesBlock.class)
public abstract class MixinLeavesBlock {

    @Inject(method = "onScheduledTick", at = @At("HEAD"), cancellable = true)
    private void disableDecay(World world, int x, int y, int z, Random rand, CallbackInfo ci) {
        if (!((ExWorldProperties) world.properties).getLeavesDecay()) {
            ci.cancel();
        }
    }
}
