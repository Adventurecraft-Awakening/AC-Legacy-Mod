package dev.adventurecraft.awakening.extension.inventory;

import net.minecraft.world.ItemInstance;

public interface ExPlayerInventory {

    boolean consumeItemAmount(int id, int meta, int count);

    int getSlot(int id, int meta);

    void selectSlot(int index);

    int getOffhandSlot();

    void setOffhandSlot(int value);

    int getMainhandSlot();

    void setMainhandSlot(int value);

    ItemInstance getOffhandItemStack();

    void swapOffhandWithMain();
}
