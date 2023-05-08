package dev.adventurecraft.awakening.common;

import net.minecraft.item.ItemStack;

public interface AC_IItemLight {

    boolean isLighting(ItemStack stack);

    boolean isMuzzleFlash(ItemStack stack);
}
