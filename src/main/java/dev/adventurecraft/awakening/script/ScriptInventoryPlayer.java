package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.extension.inventory.ExPlayerInventory;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.player.Inventory;

@SuppressWarnings("unused")
public class ScriptInventoryPlayer extends ScriptInventory {

    Inventory invPlayer;

    ScriptInventoryPlayer(Inventory var1) {
        super(var1);
        this.invPlayer = var1;
    }

    public int getSlotContainingItem(int var1) {
        int var2 = this.invPlayer.getSlot(var1);
        if (var2 == -1) {
            for (int var3 = 36; var3 < 40; ++var3) {
                ItemInstance var4 = this.invPlayer.getItem(var3);
                if (var4 != null && var4.id == var1) {
                    return var3;
                }
            }
        }

        return var2;
    }

    public int getSlotContainingItemDamage(int var1, int var2) {
        for (int var3 = 0; var3 < this.invPlayer.getContainerSize(); ++var3) {
            ItemInstance var4 = this.invPlayer.getItem(var3);
            if (var4 != null && var4.id == var1 && var4.getAuxValue() == var2) {
                return var3;
            }
        }

        return -1;
    }

    public void setCurrentItem(int var1) {
        this.invPlayer.grabTexture(var1, false);
    }

    public void changeCurrentItem(int var1) {
        this.invPlayer.swapPaint(var1);
    }

    public boolean consumeItem(int var1) {
        return this.invPlayer.removeResource(var1);
    }

    public boolean consumeItemAmount(int var1, int var2, int var3) {
        return ((ExPlayerInventory) this.invPlayer).consumeItemAmount(var1, var2, var3);
    }

    public int getArmorValue() {
        return this.invPlayer.getArmorValue();
    }

    public void dropAllItems() {
        this.invPlayer.dropAll();
    }

    public ScriptItem getCurrentItem() {
        ItemInstance var1 = this.invPlayer.getSelected();
        return var1 != null && var1.id != 0 ? new ScriptItem(var1) : null;
    }

    public ScriptItem getOffhandItem() {
        ItemInstance var1 = ((ExPlayerInventory) this.invPlayer).getOffhandItemStack();
        return var1 != null && var1.id != 0 ? new ScriptItem(var1) : null;
    }

    public void swapOffhandWithMain() {
        ((ExPlayerInventory) this.invPlayer).swapOffhandWithMain();
    }

    public boolean addItem(ScriptItem var1) {
        return this.invPlayer.add(var1.item);
    }

    public ScriptItem getCursorItem() {
        ItemInstance var1 = this.invPlayer.getCarried();
        return var1 != null && var1.id != 0 ? new ScriptItem(var1) : null;
    }

    public void setCursorItem(ScriptItem var1) {
        if (var1 == null) {
            this.invPlayer.setCarried(null);
        } else {
            this.invPlayer.setCarried(var1.item);
        }
    }
}
