package dev.adventurecraft.awakening.common;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public interface AC_ISlotCallbackItem {

    void onAddToSlot(PlayerEntity player, int slotId, ItemStack stack);

    void onRemovedFromSlot(PlayerEntity player, int slotId, ItemStack stack);
}
