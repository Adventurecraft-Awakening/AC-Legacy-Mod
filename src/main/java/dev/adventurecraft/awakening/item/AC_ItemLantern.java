package dev.adventurecraft.awakening.item;

import net.minecraft.client.Minecraft;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

class AC_ItemLantern extends Item implements AC_IItemLight {

    public AC_ItemLantern(int var1) {
        super(var1);
        this.maxStackSize = 1;
    }

    @Override
    public boolean isLighting(Entity entity, ItemInstance stack) {
        if (stack.getAuxValue() < stack.getMaxDamage()) {
            stack.setDamage(stack.getAuxValue() + 1);
            return true;
        }
        if (entity instanceof Player player) {
            if (stack.getAuxValue() == stack.getMaxDamage() && player.inventory.removeResource(AC_Items.oil.id)) {
                stack.setDamage(0);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isMuzzleFlash(Entity entity, ItemInstance stack) {
        return false;
    }
}
