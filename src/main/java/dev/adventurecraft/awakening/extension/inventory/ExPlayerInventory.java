package dev.adventurecraft.awakening.extension.inventory;

import net.minecraft.world.ItemInstance;

public interface ExPlayerInventory {

    boolean consumeItemAmount(int var1, int var2, int var3);

    int getOffhandSlot();

    void setOffhandSlot(int value);

    int getMainhandSlot();

    void setMainhandSlot(int value);

    ItemInstance getOffhandItemStack();

    void swapOffhandWithMain();
}
