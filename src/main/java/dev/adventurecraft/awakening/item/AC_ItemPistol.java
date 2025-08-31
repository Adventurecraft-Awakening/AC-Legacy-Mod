package dev.adventurecraft.awakening.item;

import dev.adventurecraft.awakening.common.AC_UtilBullet;
import dev.adventurecraft.awakening.extension.item.ExItemStack;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

class AC_ItemPistol extends Item implements AC_IItemReload, AC_IItemLight, AC_IUseDelayItem {

    private int itemUseDelay;

    public AC_ItemPistol(int id) {
        super(id);
        this.maxStackSize = 1;
        this.itemUseDelay = 1;
    }

    @Override
    public ItemInstance use(ItemInstance stack, Level world, Player player) {
        var exStack = (ExItemStack) stack;
        if (exStack.getTimeLeft() > 0 || exStack.getReloading()) {
            return stack;
        }

        if (stack.getAuxValue() == stack.getMaxDamage()) {
            exStack.setReloading(true);
            return stack;
        }

        exStack.setJustReloaded(false);
        world.playSound(player, "items.pistol.fire", 1.0F, 1.0F);
        AC_UtilBullet.fireBullet(world, player, 0.05F, 9);
        stack.setDamage(stack.getAuxValue() + 1);
        exStack.setTimeLeft(7);
        if (stack.getAuxValue() == stack.getMaxDamage()) {
            exStack.setReloading(true);
        }

        return stack;
    }

    @Override
    public boolean isLighting(Entity entity, ItemInstance stack) {
        var exStack = (ExItemStack) stack;
        return !exStack.getJustReloaded() && exStack.getTimeLeft() > 4;
    }

    @Override
    public boolean isMuzzleFlash(Entity entity, ItemInstance stack) {
        var exStack = (ExItemStack) stack;
        return !exStack.getJustReloaded() && exStack.getTimeLeft() > 4;
    }

    @Override
    public void reload(ItemInstance stack, Level world, Player player) {
        var exStack = (ExItemStack) stack;
        if (stack.getAuxValue() > 0 && player.inventory.removeResource(AC_Items.pistolAmmo.id)) {
            do {
                stack.setDamage(stack.getAuxValue() - 1);
            }
            while (stack.getAuxValue() > 0 && player.inventory.removeResource(AC_Items.pistolAmmo.id));

            exStack.setTimeLeft(32);
            world.playSound(player, "items.clipReload", 1.0F, 1.0F);
        } else {
            world.playSound(player, "items.dryFire", 1.0F, 1.0F);
            exStack.setTimeLeft(4);
        }

        exStack.setJustReloaded(true);
        exStack.setReloading(false);
    }

    @Override
    public int getItemUseDelay() {
        return this.itemUseDelay;
    }
}
