package dev.adventurecraft.awakening.item;

import net.minecraft.client.Minecraft;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.item.Item;

class AC_ItemLantern extends Item implements AC_IItemLight {

    public AC_ItemLantern(int var1) {
        super(var1);
        this.maxStackSize = 1;
    }

    @Override
    public boolean isLighting(ItemInstance stack) {
        if (stack.getAuxValue() < stack.getMaxDamage()) {
            stack.setDamage(stack.getAuxValue() + 1);
            return true;
        }
        // TODO: check entity holding item instead?
        if (stack.getAuxValue() == stack.getMaxDamage() && Minecraft.instance.player.inventory.removeResource(AC_Items.oil.id)) {
            stack.setDamage(0);
            return true;
        }
        return false;
    }

    @Override
    public boolean isMuzzleFlash(ItemInstance stack) {
        return false;
    }
}
