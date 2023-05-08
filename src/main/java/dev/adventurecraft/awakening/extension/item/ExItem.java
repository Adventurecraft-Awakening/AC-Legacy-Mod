package dev.adventurecraft.awakening.extension.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface ExItem {

    boolean getDecrementDamage();

    void setDecrementDamage(boolean value);

    int getItemUseDelay();

    void setItemUseDelay(int value);

    boolean onItemUseLeftClick(ItemStack var1, PlayerEntity var2, World var3, int var4, int var5, int var6, int var7);

    void onItemLeftClick(ItemStack var1, World var2, PlayerEntity var3);

    boolean mainActionLeftClick();

    void onAddToSlot(PlayerEntity var1, int var2, int var3);

    void onRemovedFromSlot(PlayerEntity var1, int var2, int var3);
}
