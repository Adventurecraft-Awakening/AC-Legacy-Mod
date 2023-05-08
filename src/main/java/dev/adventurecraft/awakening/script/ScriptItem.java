package dev.adventurecraft.awakening.script;

import net.minecraft.item.ItemStack;

@SuppressWarnings("unused")
public class ScriptItem {

    public ItemStack item;

    public ScriptItem(ItemStack var1) {
        this.item = var1;
    }

    public ScriptItem(int var1) {
        this.item = new ItemStack(var1, 1, 0);
    }

    public ScriptItem(int var1, int var2) {
        this.item = new ItemStack(var1, var2, 0);
    }

    public ScriptItem(int var1, int var2, int var3) {
        this.item = new ItemStack(var1, var2, var3);
    }

    public int getItemID() {
        return this.item.itemId;
    }

    public void setItemID(int var1) {
        this.item.itemId = var1;
    }

    public int getQuantity() {
        return this.item.count;
    }

    public void setQuantity(int var1) {
        this.item.count = var1;
    }

    public int getDamage() {
        return this.item.getMeta();
    }

    public void setDamage(int var1) {
        this.item.setMeta(var1);
    }

    public int getMaxDamage() {
        return this.item.getDurability();
    }

    public ScriptItem copy() {
        return new ScriptItem(this.item.copy());
    }
}
