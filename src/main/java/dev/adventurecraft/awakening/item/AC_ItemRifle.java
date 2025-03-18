package dev.adventurecraft.awakening.item;

import dev.adventurecraft.awakening.common.AC_UtilBullet;
import dev.adventurecraft.awakening.extension.item.ExItemStack;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

class AC_ItemRifle extends Item implements AC_IItemReload, AC_IItemLight, AC_IUseDelayItem {

    private int itemUseDelay;

    public AC_ItemRifle(int id) {
        super(id);
        this.maxStackSize = 1;
        this.itemUseDelay = 1;
    }

    @Override
    public ItemInstance use(ItemInstance stack, Level world, Player player) {
        var exStack = (ExItemStack) stack;
        if (exStack.getTimeLeft() > 3 || exStack.getReloading()) {
            return stack;
        }

        if (stack.getAuxValue() == stack.getMaxDamage()) {
            exStack.setReloading(true);
            return stack;
        }

        exStack.setJustReloaded(false);
        world.playSound(player, "items.rifle.fire", 1.0F, 1.0F);
        AC_UtilBullet.fireBullet(world, player, 0.04F * (float) exStack.getTimeLeft() + 0.03F, 10);
        stack.setDamage(stack.getAuxValue() + 1);
        exStack.setTimeLeft(6);
        if (player.xRot > -90.0F) {
            --player.xRot;
        }

        if (stack.getAuxValue() == stack.getMaxDamage()) {
            exStack.setReloading(true);
        }

        return stack;
    }

    @Override
    public boolean isLighting(ItemInstance stack) {
        var exStack = (ExItemStack) stack;
        return !exStack.getJustReloaded() && exStack.getTimeLeft() > 3;
    }

    @Override
    public boolean isMuzzleFlash(ItemInstance stack) {
        var exStack = (ExItemStack) stack;
        return !exStack.getJustReloaded() && exStack.getTimeLeft() > 3;
    }

    @Override
    public void reload(ItemInstance stack, Level world, Player player) {
        var exStack = (ExItemStack) stack;
        if (stack.getAuxValue() > 0 && player.inventory.removeResource(AC_Items.rifleAmmo.id)) {
            stack.setDamage(stack.getAuxValue() - 1);

            while (stack.getAuxValue() > 0 && player.inventory.removeResource(AC_Items.rifleAmmo.id)) {
                stack.setDamage(stack.getAuxValue() - 1);
            }

            exStack.setTimeLeft(32);
            world.playSound(player, "items.clipReload", 1.0F, 1.0F);
        } else {
            world.playSound(player, "items.dryFire", 1.0F, 1.0F);
            exStack.setTimeLeft(4);
        }

        exStack.setReloading(false);
        exStack.setJustReloaded(true);
    }

    @Override
    public int getItemUseDelay() {
        return this.itemUseDelay;
    }
}
