package dev.adventurecraft.awakening.extension.inventory;

import net.minecraft.item.ItemStack;

public interface ExPlayerInventory {

    boolean consumeItemAmount(int var1, int var2, int var3);

    int getOffhandItem();

    void setOffhandItem(int value);

    ItemStack getOffhandItemStack();

    void swapOffhandWithMain();
}
