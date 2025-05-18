package dev.adventurecraft.awakening.mixin.item;

import dev.adventurecraft.awakening.common.AC_DebugMode;
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

    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void disableUsageInDebugMode(ItemInstance var1, Player var2, Level var3, int var4, int var5, int var6, int var7, CallbackInfoReturnable<Boolean> cir) {
        if (!AC_DebugMode.active) {
            cir.setReturnValue(false);
        }
    }
}
