package dev.adventurecraft.awakening.item;

import net.minecraft.world.ItemInstance;

public interface AC_IItemLight {

    boolean isLighting(ItemInstance stack);

    boolean isMuzzleFlash(ItemInstance stack);
}
