package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.item.ExItemStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

class AC_ItemPistol extends Item implements AC_IItemReload, AC_IItemLight, AC_IUseDelayItem {

    private int itemUseDelay;

    public AC_ItemPistol(int id) {
        super(id);
        this.maxStackSize = 1;
        this.itemUseDelay = 1;
    }

    @Override
    public ItemStack use(ItemStack stack, World world, PlayerEntity player) {
        var exStack = (ExItemStack) stack;
        if (exStack.getTimeLeft() > 0 || exStack.getReloading()) {
            return stack;
        }

        if (stack.getMeta() == stack.getDurability()) {
            exStack.setReloading(true);
            return stack;
        }

        exStack.setJustReloaded(false);
        world.playSound(player, "items.pistol.fire", 1.0F, 1.0F);
        AC_UtilBullet.fireBullet(world, player, 0.05F, 9);
        stack.setMeta(stack.getMeta() + 1);
        exStack.setTimeLeft(7);
        if (stack.getMeta() == stack.getDurability()) {
            exStack.setReloading(true);
        }

        return stack;
    }

    @Override
    public boolean isLighting(ItemStack stack) {
        var exStack = (ExItemStack) stack;
        return !exStack.getJustReloaded() && exStack.getTimeLeft() > 4;
    }

    @Override
    public boolean isMuzzleFlash(ItemStack stack) {
        var exStack = (ExItemStack) stack;
        return !exStack.getJustReloaded() && exStack.getTimeLeft() > 4;
    }

    @Override
    public void reload(ItemStack stack, World world, PlayerEntity player) {
        var exStack = (ExItemStack) stack;
        if (stack.getMeta() > 0 && player.inventory.removeItem(AC_Items.pistolAmmo.id)) {
            stack.setMeta(stack.getMeta() - 1);

            while (stack.getMeta() > 0 && player.inventory.removeItem(AC_Items.pistolAmmo.id)) {
                stack.setMeta(stack.getMeta() - 1);
            }

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
