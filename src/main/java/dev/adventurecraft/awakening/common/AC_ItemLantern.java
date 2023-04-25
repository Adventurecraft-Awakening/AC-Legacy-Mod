package dev.adventurecraft.awakening.common;

import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

class AC_ItemLantern extends Item {
    public AC_ItemLantern(int var1) {
        super(var1);
        this.maxStackSize = 1;
    }

    public boolean isLighting(ItemStack var1) {
        if (var1.getMeta() < var1.getDurability()) {
            var1.setMeta(var1.getMeta() + 1);
            return true;
        } else if (var1.getMeta() == var1.getDurability() && Minecraft.instance.player.inventory.removeItem(AC_Items.oil.id)) {
            var1.setMeta(0);
            return true;
        } else {
            return false;
        }
    }
}
