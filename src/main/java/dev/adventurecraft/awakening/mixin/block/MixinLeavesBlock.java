package dev.adventurecraft.awakening.mixin.block;

import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.LeafTile;

@Mixin(LeafTile.class)
public abstract class MixinLeavesBlock {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void disableDecay(Level world, int x, int y, int z, Random rand, CallbackInfo ci) {
        if (!((ExWorldProperties) world.levelData).getLeavesDecay()) {
            ci.cancel();
        }
    }
}
