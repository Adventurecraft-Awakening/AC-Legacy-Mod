package dev.adventurecraft.awakening.item;

import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.Entity;

public interface AC_IItemLight {

    boolean isLighting(Entity entity, ItemInstance stack);

    boolean isMuzzleFlash(Entity entity, ItemInstance stack);
}
