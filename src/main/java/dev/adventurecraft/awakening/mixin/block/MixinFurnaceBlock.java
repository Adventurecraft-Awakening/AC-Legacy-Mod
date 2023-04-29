package dev.adventurecraft.awakening.mixin.block;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.FurnaceBlock;
import net.minecraft.entity.BlockEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FurnaceBlock.class)
public abstract class MixinFurnaceBlock {

    @Inject(method = "updateFurnaceState", at = @At("TAIL"))
    private static void validateAfterUpdate(
        boolean var1, World var2, int var3, int var4, int var5, CallbackInfo ci,
        @Local BlockEntity var6) {

        var6.validate();
    }
}
