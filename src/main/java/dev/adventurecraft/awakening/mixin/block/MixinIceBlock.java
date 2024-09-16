package dev.adventurecraft.awakening.mixin.block;

import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.IceTile;

@Mixin(IceTile.class)
public abstract class MixinIceBlock {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void disableDecay(Level var1, int var2, int var3, int var4, Random var5, CallbackInfo ci) {
        if (!((ExWorldProperties) var1.levelData).getIceMelts()) {
            ci.cancel();
        }
    }
}
