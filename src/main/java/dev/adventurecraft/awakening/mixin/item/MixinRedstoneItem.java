package dev.adventurecraft.awakening.mixin.item;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.adventurecraft.awakening.common.AC_DebugMode;
import net.minecraft.item.RedstoneItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RedstoneItem.class)
public abstract class MixinRedstoneItem {

    @ModifyExpressionValue(
        method = "useOnBlock",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/Block;canPlaceAt(Lnet/minecraft/world/World;III)Z"))
    private boolean onlyPlaceInDebugMode(boolean value) {
        return AC_DebugMode.active && value;
    }
}
