package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.extension.inventory.ExPlayerInventory;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.player.Inventory;

@SuppressWarnings("unused")
public class ScriptInventoryPlayer extends ScriptInventory {

    private Inventory playerInventory;

    ScriptInventoryPlayer(Inventory inventory) {
        super(inventory);
        this.playerInventory = inventory;
    }

    /**
     * Searched in the Item & Armor Slots the desired Item by only the @itemId
     *
     * @param itemId Item ID
     * @return returns the inventory slot (0-35 item slots, 36-39 armor slots), else returns -1 for no item found
     */
    public int getSlotContainingItem(int itemId) {
        //Search Inventory slots 0 - 35
        int inventorySlot = this.playerInventory.getSlot(itemId);
        if (inventorySlot == -1) {
            //Search Armor Slots 36 - 40
            ExPlayerInventory playerInventory = (ExPlayerInventory) inv;
            inventorySlot = playerInventory.getArmorSlot(itemId);
            if (inventorySlot != -1) {
                inventorySlot += this.playerInventory.items.length; //default length = 36;
            }
        }
        return inventorySlot;
    }

    /**
     * Searched in the Item & Armor Slots the desired Item by its @itemId and @itemDamage
     *
     * @param itemId Item ID
     * @param itemDamage Item Damage Value or Sub Id
     * @return returns the inventory slot (0-35 item slots, 36-39 armor slots), else returns -1 for no item found
     */
    public int getSlotContainingItemDamage(int itemId, int itemDamage) {
        ExPlayerInventory playerInventory = (ExPlayerInventory) inv;

        //Search Inventory slots 0 - 35
        int inventorySlot = playerInventory.getSlot(itemId, itemDamage);
        if (inventorySlot == -1) {
            //Search Armor Slots 36 - 40
            inventorySlot = playerInventory.getArmorSlot(itemId, itemDamage);
            if (inventorySlot != -1) {
                inventorySlot += this.playerInventory.items.length; //default length = 36;
            }
        }
        return inventorySlot; // return -1, if the desired item could'nt be found
    }

    public void changeCurrentItem(int var1) {
        this.playerInventory.swapPaint(var1);
    }

    public boolean consumeItem(int var1) {
        return this.playerInventory.removeResource(var1);
    }

    public boolean consumeItemAmount(int var1, int var2, int var3) {
        return ((ExPlayerInventory) this.playerInventory).consumeItemAmount(var1, var2, var3);
    }

    public int getArmorValue() {
        return this.playerInventory.getArmorValue();
    }

    public void dropAllItems() {
        this.playerInventory.dropAll();
    }

    public ScriptItem getCurrentItem() {
        ItemInstance var1 = this.playerInventory.getSelected();
        return var1 != null && var1.id != 0 ? new ScriptItem(var1) : null;
    }

    public void setCurrentItem(int var1) {
        this.playerInventory.grabTexture(var1, false);
    }

    public ScriptItem getOffhandItem() {
        ItemInstance var1 = ((ExPlayerInventory) this.playerInventory).getOffhandItemStack();
        return var1 != null && var1.id != 0 ? new ScriptItem(var1) : null;
    }

    public void swapOffhandWithMain() {
        ((ExPlayerInventory) this.playerInventory).swapOffhandWithMain();
    }

    public void setMainhandSlot(int slot) {
        if(slot >= 0 && slot < 9) {
            ((ExPlayerInventory) this.playerInventory).setMainhandSlot(slot);
        }
    }

    public void setOffhandSlot(int slot) {
        if(slot >= 0 && slot < 9) {
            ((ExPlayerInventory) this.playerInventory).setOffhandSlot(slot);
        }
    }

    public boolean addItem(ScriptItem var1) {
        return this.playerInventory.add(var1.item);
    }

    public ScriptItem getCursorItem() {
        ItemInstance var1 = this.playerInventory.getCarried();
        return var1 != null && var1.id != 0 ? new ScriptItem(var1) : null;
    }

    public void setCursorItem(ScriptItem var1) {
        if (var1 == null) {
            this.playerInventory.setCarried(null);
        }
        else {
            this.playerInventory.setCarried(var1.item);
        }
    }
}
