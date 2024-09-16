package dev.adventurecraft.awakening.script;

import net.minecraft.world.Container;
import net.minecraft.world.ItemInstance;

@SuppressWarnings("unused")
public class ScriptInventory {

    Container inv;

    ScriptInventory(Container var1) {
        this.inv = var1;
    }

    public int getSizeInventory() {
        return this.inv.getContainerSize();
    }

    public String getName() {
        return this.inv.getName();
    }

    public int getStackLimit() {
        return this.inv.getMaxStackSize();
    }

    public ScriptItem getItemInSlot(int var1) {
        ItemInstance var2 = this.inv.getItem(var1);
        return var2 != null && var2.id != 0 ? new ScriptItem(var2) : null;
    }

    public ScriptItem decrementItem(int var1, int var2) {
        ItemInstance var3 = this.inv.removeItem(var1, var2);
        return var3 != null && var3.id != 0 ? new ScriptItem(var3) : null;
    }

    public void setSlot(int var1, ScriptItem var2) {
        this.inv.setItem(var1, var2.item);
    }

    public void emptySlot(int var1) {
        this.inv.setItem(var1, null);
    }
}
