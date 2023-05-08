package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.extension.inventory.ExPlayerInventory;
import net.minecraft.inventory.PlayerInventory;
import net.minecraft.item.ItemStack;

@SuppressWarnings("unused")
public class ScriptInventoryPlayer extends ScriptInventory {

    PlayerInventory invPlayer;

    ScriptInventoryPlayer(PlayerInventory var1) {
        super(var1);
        this.invPlayer = var1;
    }

    public int getSlotContainingItem(int var1) {
        int var2 = this.invPlayer.getSlotWithItem(var1);
        if (var2 == -1) {
            for (int var3 = 36; var3 < 40; ++var3) {
                ItemStack var4 = this.invPlayer.getInventoryItem(var3);
                if (var4 != null && var4.itemId == var1) {
                    return var3;
                }
            }
        }

        return var2;
    }

    public int getSlotContainingItemDamage(int var1, int var2) {
        for (int var3 = 0; var3 < this.invPlayer.getInventorySize(); ++var3) {
            ItemStack var4 = this.invPlayer.getInventoryItem(var3);
            if (var4 != null && var4.itemId == var1 && var4.getMeta() == var2) {
                return var3;
            }
        }

        return -1;
    }

    public void setCurrentItem(int var1) {
        this.invPlayer.setSelectedItemWithID(var1, false);
    }

    public void changeCurrentItem(int var1) {
        this.invPlayer.scrollInHotBar(var1);
    }

    public boolean consumeItem(int var1) {
        return this.invPlayer.removeItem(var1);
    }

    public boolean consumeItemAmount(int var1, int var2, int var3) {
        return ((ExPlayerInventory) this.invPlayer).consumeItemAmount(var1, var2, var3);
    }

    public int getArmorValue() {
        return this.invPlayer.getArmorValue();
    }

    public void dropAllItems() {
        this.invPlayer.dropInventory();
    }

    public ScriptItem getCurrentItem() {
        ItemStack var1 = this.invPlayer.getHeldItem();
        return var1 != null && var1.itemId != 0 ? new ScriptItem(var1) : null;
    }

    public ScriptItem getOffhandItem() {
        ItemStack var1 = ((ExPlayerInventory) this.invPlayer).getOffhandItemStack();
        return var1 != null && var1.itemId != 0 ? new ScriptItem(var1) : null;
    }

    public void swapOffhandWithMain() {
        ((ExPlayerInventory) this.invPlayer).swapOffhandWithMain();
    }

    public boolean addItem(ScriptItem var1) {
        return this.invPlayer.addStack(var1.item);
    }

    public ScriptItem getCursorItem() {
        ItemStack var1 = this.invPlayer.getCursorItem();
        return var1 != null && var1.itemId != 0 ? new ScriptItem(var1) : null;
    }

    public void setCursorItem(ScriptItem var1) {
        if (var1 == null) {
            this.invPlayer.setCursorItem(null);
        } else {
            this.invPlayer.setCursorItem(var1.item);
        }
    }
}
