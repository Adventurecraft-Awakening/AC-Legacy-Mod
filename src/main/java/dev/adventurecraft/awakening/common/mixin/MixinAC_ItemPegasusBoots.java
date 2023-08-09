package dev.adventurecraft.awakening.common.mixin;

import dev.adventurecraft.awakening.common.AC_ItemPegasusBoots;
import dev.adventurecraft.awakening.common.AC_ISlotCallbackItem;
import dev.adventurecraft.awakening.extension.entity.player.ExPlayerEntity;
import dev.adventurecraft.awakening.mixin.item.MixinItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AC_ItemPegasusBoots.class)
public abstract class MixinAC_ItemPegasusBoots extends MixinItem implements AC_ISlotCallbackItem {

    @Override
    public void onAddToSlot(PlayerEntity player, int slot, ItemStack stack) {
        super.onAddToSlot(player, slot, stack);
        if (slot == 36) {
            var exPlayer = (ExPlayerEntity) player;
            exPlayer.setCanWallJump(true);
            exPlayer.setTimesCanJumpInAir(1);
        }
    }

    @Override
    public void onRemovedFromSlot(PlayerEntity player, int slot, ItemStack stack) {
        super.onRemovedFromSlot(player, slot, stack);
        if (slot == 36) {
            var exPlayer = (ExPlayerEntity) player;
            exPlayer.setCanWallJump(false);
            exPlayer.setTimesCanJumpInAir(0);
        }
    }
}
