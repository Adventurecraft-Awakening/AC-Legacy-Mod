package dev.adventurecraft.awakening.mixin.item;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.adventurecraft.awakening.common.AC_DebugMode;
import net.minecraft.world.item.RedStoneItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RedStoneItem.class)
public abstract class MixinRedstoneItem {

    @ModifyExpressionValue(
        method = "useOn",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/tile/Tile;mayPlace(Lnet/minecraft/world/level/Level;III)Z"))
    private boolean onlyPlaceInDebugMode(boolean value) {
        return AC_DebugMode.active && value;
    }
}
