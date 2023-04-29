package dev.adventurecraft.awakening.mixin.block;

import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import net.minecraft.block.IceBlock;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(IceBlock.class)
public abstract class MixinIceBlock {

    @Inject(method = "onScheduledTick", at = @At("HEAD"), cancellable = true)
    private void disableDecay(World var1, int var2, int var3, int var4, Random var5, CallbackInfo ci) {
        if (!((ExWorldProperties) var1.properties).getIceMelts()) {
            ci.cancel();
        }
    }
}
