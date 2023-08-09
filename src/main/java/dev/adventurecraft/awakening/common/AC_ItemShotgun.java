package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.item.ExItemStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

class AC_ItemShotgun extends Item implements AC_IItemReload, AC_IItemLight, AC_IUseDelayItem {

    private int itemUseDelay;

    public AC_ItemShotgun(int id) {
        super(id);
        this.maxStackSize = 1;
        this.itemUseDelay = 1;
    }

    @Override
    public ItemStack use(ItemStack stack, World world, PlayerEntity player) {
        var exStack = (ExItemStack) stack;
        if (exStack.getTimeLeft() > 0) {
            if (exStack.getReloading() && stack.getMeta() > 0) {
                exStack.setReloading(false);
            }
            return stack;
        }

        if (stack.getMeta() == stack.getDurability()) {
            exStack.setReloading(true);
            exStack.setTimeLeft(0);
            return stack;
        }

        world.playSound(player, "items.shotgun.fire_and_pump", 1.0F, 1.0F);

        for (int i = 0; i < 14; ++i) {
            AC_UtilBullet.fireBullet(world, player, 0.12F, 2);
        }

        stack.setMeta(stack.getMeta() + 1);
        exStack.setTimeLeft(40);
        if (stack.getMeta() == stack.getDurability()) {
            exStack.setReloading(true);
        }
        return stack;
    }

    @Override
    public boolean isLighting(ItemStack stack) {
        return ((ExItemStack) stack).getTimeLeft() > 42;
    }

    @Override
    public boolean isMuzzleFlash(ItemStack stack) {
        return ((ExItemStack) stack).getTimeLeft() > 35;
    }

    @Override
    public void reload(ItemStack stack, World world, PlayerEntity player) {
        var exStack = (ExItemStack) stack;
        if (stack.getMeta() > 0 && player.inventory.removeItem(AC_Items.shotgunAmmo.id)) {
            stack.setMeta(stack.getMeta() - 1);
            exStack.setTimeLeft(20);
            world.playSound(player, "items.shotgun.reload", 1.0F, 1.0F);
            if (stack.getMeta() == 0) {
                exStack.setReloading(false);
            }
        } else {
            exStack.setReloading(false);
        }
    }


    @Override
    public int getItemUseDelay() {
        return this.itemUseDelay;
    }
}
