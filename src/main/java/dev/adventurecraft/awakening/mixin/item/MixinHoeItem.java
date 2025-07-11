package dev.adventurecraft.awakening.mixin.item;

import dev.adventurecraft.awakening.common.AC_DebugMode;
import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HoeItem.class)
public abstract class MixinHoeItem {

    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void disableUsageOutsideOfDebugMode(ItemInstance var1, Player var2, Level var3, int var4, int var5, int var6, int var7, CallbackInfoReturnable<Boolean> cir) {
        if (!AC_DebugMode.active && !((ExWorldProperties)var3.levelData).getCanUseHoe()) {
            cir.setReturnValue(false);
        }
    }
}
