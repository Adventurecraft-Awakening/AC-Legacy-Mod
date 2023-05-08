package dev.adventurecraft.awakening.script;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

@SuppressWarnings("unused")
public class ScriptInventory {

    Inventory inv;

    ScriptInventory(Inventory var1) {
        this.inv = var1;
    }

    public int getSizeInventory() {
        return this.inv.getInventorySize();
    }

    public String getName() {
        return this.inv.getContainerName();
    }

    public int getStackLimit() {
        return this.inv.getMaxItemCount();
    }

    public ScriptItem getItemInSlot(int var1) {
        ItemStack var2 = this.inv.getInventoryItem(var1);
        return var2 != null && var2.itemId != 0 ? new ScriptItem(var2) : null;
    }

    public ScriptItem decrementItem(int var1, int var2) {
        ItemStack var3 = this.inv.takeInventoryItem(var1, var2);
        return var3 != null && var3.itemId != 0 ? new ScriptItem(var3) : null;
    }

    public void setSlot(int var1, ScriptItem var2) {
        this.inv.setInventoryItem(var1, var2.item);
    }

    public void emptySlot(int var1) {
        this.inv.setInventoryItem(var1, null);
    }
}
