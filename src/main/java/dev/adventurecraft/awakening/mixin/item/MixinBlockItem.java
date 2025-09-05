package dev.adventurecraft.awakening.mixin.item;

import dev.adventurecraft.awakening.common.AC_DebugMode;
import dev.adventurecraft.awakening.extension.entity.player.ExPlayerEntity;
import dev.adventurecraft.awakening.extension.item.ExTileItem;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TileItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TileItem.class)
public abstract class MixinBlockItem implements ExTileItem {

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

    @ModifyConstant(
        method = "useOn",
        constant = @Constant(intValue = 127)
    )
    public int allowBuildAtMaxY(int constant) {
        return 128;
    }
}
