package dev.adventurecraft.awakening.script;

import net.minecraft.world.ItemInstance;

@SuppressWarnings("unused")
public class ScriptItem {

    public ItemInstance item;

    public ScriptItem(ItemInstance var1) {
        this.item = var1;
    }

    public ScriptItem(int var1) {
        this.item = new ItemInstance(var1, 1, 0);
    }

    public ScriptItem(int var1, int var2) {
        this.item = new ItemInstance(var1, var2, 0);
    }

    public ScriptItem(int var1, int var2, int var3) {
        this.item = new ItemInstance(var1, var2, var3);
    }

    public int getItemID() {
        return this.item.id;
    }

    public void setItemID(int var1) {
        this.item.id = var1;
    }

    public int getQuantity() {
        return this.item.count;
    }

    public void setQuantity(int var1) {
        this.item.count = var1;
    }

    public int getDamage() {
        return this.item.getAuxValue();
    }

    public void setDamage(int var1) {
        this.item.setDamage(var1);
    }

    public int getMaxDamage() {
        return this.item.getMaxDamage();
    }

    public ScriptItem copy() {
        return new ScriptItem(this.item.copy());
    }
}
