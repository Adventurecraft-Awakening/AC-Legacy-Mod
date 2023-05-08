package dev.adventurecraft.awakening.common;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface AC_IItemReload {

	void reload(ItemStack stack, World world, PlayerEntity player);
}
