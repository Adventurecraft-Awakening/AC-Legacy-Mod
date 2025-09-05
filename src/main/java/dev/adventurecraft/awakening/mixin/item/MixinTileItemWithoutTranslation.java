package dev.adventurecraft.awakening.mixin.item;

import dev.adventurecraft.awakening.extension.entity.player.ExPlayerEntity;
import net.minecraft.world.item.TileItemWithoutTranslation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.ItemInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TileItemWithoutTranslation.class)
public abstract class MixinTileItemWithoutTranslation {

    @Inject(
        method = "useOn",
        at = @At("HEAD"),
        cancellable = true
    )
    private void disableUsageInDebugMode(
        ItemInstance item,
        Player player,
        Level level,
        int x,
        int y,
        int z,
        int face,
        CallbackInfoReturnable<Boolean> cir
    ) {
        if (!((ExPlayerEntity) player).isDebugMode()) {
            cir.setReturnValue(false);
        }
    }
}
