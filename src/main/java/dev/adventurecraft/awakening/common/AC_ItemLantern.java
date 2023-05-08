package dev.adventurecraft.awakening.common;

import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

class AC_ItemLantern extends Item implements AC_IItemLight {

    public AC_ItemLantern(int var1) {
        super(var1);
        this.maxStackSize = 1;
    }

    @Override
    public boolean isLighting(ItemStack stack) {
        if (stack.getMeta() < stack.getDurability()) {
            stack.setMeta(stack.getMeta() + 1);
            return true;
        }
        // TODO: check entity holding item instead?
        if (stack.getMeta() == stack.getDurability() && Minecraft.instance.player.inventory.removeItem(AC_Items.oil.id)) {
            stack.setMeta(0);
            return true;
        }
        return false;
    }

    @Override
    public boolean isMuzzleFlash(ItemStack stack) {
        return false;
    }
}
