package dev.adventurecraft.awakening.mixin.item;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.extension.entity.player.ExPlayerEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.RedStoneItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RedStoneItem.class)
public abstract class MixinRedstoneItem {

    @ModifyExpressionValue(
        method = "useOn",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/tile/Tile;mayPlace(Lnet/minecraft/world/level/Level;III)Z"
        )
    )
    private boolean onlyPlaceInDebugMode(boolean value, @Local(argsOnly = true) Player player) {
        return ((ExPlayerEntity) player).isDebugMode() && value;
    }
}
