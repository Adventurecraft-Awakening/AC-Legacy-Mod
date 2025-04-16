package dev.adventurecraft.awakening.item;

import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public interface AC_ILeftClickItem {

    default boolean onItemUseLeftClick(ItemInstance stack, Player player, Level world, int x, int y, int z, int side) {
        return false;
    }

    default void onItemLeftClick(ItemInstance stack, Level world, Player player) {
    }

    default boolean mainActionLeftClick() {
        return false;
    }
}
