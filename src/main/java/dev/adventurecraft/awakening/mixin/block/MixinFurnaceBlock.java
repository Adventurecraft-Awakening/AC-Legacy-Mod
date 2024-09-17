package dev.adventurecraft.awakening.mixin.block;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.FurnaceTile;
import net.minecraft.world.level.tile.entity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FurnaceTile.class)
public abstract class MixinFurnaceBlock {

    @Inject(method = "setLit", at = @At("TAIL"))
    private static void validateAfterUpdate(
        boolean var1, Level var2, int var3, int var4, int var5, CallbackInfo ci,
        @Local TileEntity var6) {

        var6.clearRemoved();
    }
}
