package dev.adventurecraft.awakening.common;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface AC_ILeftClickItem {

    default boolean onItemUseLeftClick(ItemStack stack, PlayerEntity player, World world, int x, int y, int z, int side) {
        return false;
    }

    default void onItemLeftClick(ItemStack stack, World world, PlayerEntity player) {
    }

    default boolean mainActionLeftClick() {
        return false;
    }
}
