package dev.adventurecraft.awakening.mixin.item;

import dev.adventurecraft.awakening.extension.entity.player.ExPlayerEntity;
import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyePowderItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DyePowderItem.class)
public abstract class MixinDyePowderItem {

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
        // Specifically Bonemeal
        if (item.getAuxValue() == 15) {
            if (!((ExPlayerEntity) player).isDebugMode() &&
                !((ExWorldProperties) level.levelData).getCanUseBonemeal()) {
                cir.setReturnValue(false);
            }
        }
    }
}
