package dev.adventurecraft.awakening.extension.inventory;

import net.minecraft.world.ItemInstance;

public interface ExPlayerInventory {

    boolean consumeItemAmount(int var1, int var2, int var3);

    int getOffhandItem();

    void setOffhandItem(int value);

    ItemInstance getOffhandItemStack();

    void swapOffhandWithMain();
}
