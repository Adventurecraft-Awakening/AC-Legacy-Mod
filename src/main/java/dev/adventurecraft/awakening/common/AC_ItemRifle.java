package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.item.ExItem;
import dev.adventurecraft.awakening.extension.item.ExItemStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

class AC_ItemRifle extends Item implements AC_IItemReload, AC_IItemLight {
    public AC_ItemRifle(int var1) {
        super(var1);
        this.maxStackSize = 1;
        ((ExItem) this).setItemUseDelay(1);
    }

    @Override
    public ItemStack use(ItemStack stack, World world, PlayerEntity player) {
        var exStack = (ExItemStack) stack;
        if (exStack.getTimeLeft() > 3 || exStack.getReloading()) {
            return stack;
        }

        if (stack.getMeta() == stack.getDurability()) {
            exStack.setReloading(true);
            return stack;
        }

        exStack.setJustReloaded(false);
        world.playSound(player, "items.rifle.fire", 1.0F, 1.0F);
        AC_UtilBullet.fireBullet(world, player, 0.04F * (float) exStack.getTimeLeft() + 0.03F, 10);
        stack.setMeta(stack.getMeta() + 1);
        exStack.setTimeLeft(6);
        if (player.pitch > -90.0F) {
            --player.pitch;
        }

        if (stack.getMeta() == stack.getDurability()) {
            exStack.setReloading(true);
        }

        return stack;
    }

    @Override
    public boolean isLighting(ItemStack stack) {
        var exStack = (ExItemStack) stack;
        return !exStack.getJustReloaded() && exStack.getTimeLeft() > 3;
    }

    @Override
    public boolean isMuzzleFlash(ItemStack stack) {
        var exStack = (ExItemStack) stack;
        return !exStack.getJustReloaded() && exStack.getTimeLeft() > 3;
    }

    @Override
    public void reload(ItemStack stack, World world, PlayerEntity player) {
        var exStack = (ExItemStack) stack;
        if (stack.getMeta() > 0 && player.inventory.removeItem(AC_Items.rifleAmmo.id)) {
            stack.setMeta(stack.getMeta() - 1);

            while (stack.getMeta() > 0 && player.inventory.removeItem(AC_Items.rifleAmmo.id)) {
                stack.setMeta(stack.getMeta() - 1);
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
}
