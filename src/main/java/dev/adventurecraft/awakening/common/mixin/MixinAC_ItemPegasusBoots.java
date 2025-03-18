package dev.adventurecraft.awakening.common.mixin;

import dev.adventurecraft.awakening.item.AC_ItemPegasusBoots;
import dev.adventurecraft.awakening.item.AC_ISlotCallbackItem;
import dev.adventurecraft.awakening.extension.entity.player.ExPlayerEntity;
import dev.adventurecraft.awakening.mixin.item.MixinItem;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AC_ItemPegasusBoots.class)
public abstract class MixinAC_ItemPegasusBoots extends MixinItem implements AC_ISlotCallbackItem {

    @Override
    public void onAddToSlot(Player player, int slot, ItemInstance stack) {
        super.onAddToSlot(player, slot, stack);
        if (slot == 36) {
            var exPlayer = (ExPlayerEntity) player;
            exPlayer.setCanWallJump(true);
            exPlayer.setTimesCanJumpInAir(1);
        }
    }

    @Override
    public void onRemovedFromSlot(Player player, int slot, ItemInstance stack) {
        super.onRemovedFromSlot(player, slot, stack);
        if (slot == 36) {
            var exPlayer = (ExPlayerEntity) player;
            exPlayer.setCanWallJump(false);
            exPlayer.setTimesCanJumpInAir(0);
        }
    }
}
