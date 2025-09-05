package dev.adventurecraft.awakening.mixin.item;

import dev.adventurecraft.awakening.common.AC_DebugMode;
import dev.adventurecraft.awakening.extension.entity.player.ExPlayerEntity;
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

    @Inject(
        method = "useOn",
        at = @At("HEAD"),
        cancellable = true
    )
    private void disableUsageOutsideOfDebugMode(
        ItemInstance item,
        Player player,
        Level level,
        int x,
        int y,
        int z,
        int face,
        CallbackInfoReturnable<Boolean> cir
    ) {
        if (!((ExPlayerEntity) player).isDebugMode() && !((ExWorldProperties) level.levelData).getCanUseHoe()) {
            cir.setReturnValue(false);
        }
    }
}
