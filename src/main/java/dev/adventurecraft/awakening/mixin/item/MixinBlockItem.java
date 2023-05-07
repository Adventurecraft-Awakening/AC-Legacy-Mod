package dev.adventurecraft.awakening.mixin.item;

import dev.adventurecraft.awakening.common.AC_DebugMode;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public abstract class MixinBlockItem {

    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    private void disableUsageInDebugMode(ItemStack var1, PlayerEntity var2, World var3, int var4, int var5, int var6, int var7, CallbackInfoReturnable<Boolean> cir) {
        if (!AC_DebugMode.active) {
            cir.setReturnValue(false);
        }
    }
}
