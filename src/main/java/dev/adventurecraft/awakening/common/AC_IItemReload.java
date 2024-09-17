package dev.adventurecraft.awakening.common;

import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public interface AC_IItemReload {

	void reload(ItemInstance stack, Level world, Player player);
}
