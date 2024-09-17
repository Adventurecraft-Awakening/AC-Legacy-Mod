package dev.adventurecraft.awakening.common;

import net.minecraft.world.ItemInstance;

public interface AC_IItemLight {

    boolean isLighting(ItemInstance stack);

    boolean isMuzzleFlash(ItemInstance stack);
}
