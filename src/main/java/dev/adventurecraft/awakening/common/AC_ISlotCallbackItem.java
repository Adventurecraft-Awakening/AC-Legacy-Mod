package dev.adventurecraft.awakening.common;

import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.player.Player;

public interface AC_ISlotCallbackItem {

    void onAddToSlot(Player player, int slotId, ItemInstance stack);

    void onRemovedFromSlot(Player player, int slotId, ItemInstance stack);
}
