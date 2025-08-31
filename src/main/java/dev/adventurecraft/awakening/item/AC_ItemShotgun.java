package dev.adventurecraft.awakening.item;

import dev.adventurecraft.awakening.common.AC_UtilBullet;
import dev.adventurecraft.awakening.extension.item.ExItemStack;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

class AC_ItemShotgun extends Item implements AC_IItemReload, AC_IItemLight, AC_IUseDelayItem {

    private int itemUseDelay;

    public AC_ItemShotgun(int id) {
        super(id);
        this.maxStackSize = 1;
        this.itemUseDelay = 1;
    }

    @Override
    public ItemInstance use(ItemInstance stack, Level world, Player player) {
        var exStack = (ExItemStack) stack;
        if (exStack.getTimeLeft() > 0) {
            if (exStack.getReloading() && stack.getAuxValue() > 0) {
                exStack.setReloading(false);
            }
            return stack;
        }

        if (stack.getAuxValue() == stack.getMaxDamage()) {
            exStack.setReloading(true);
            exStack.setTimeLeft(0);
            return stack;
        }

        world.playSound(player, "items.shotgun.fire_and_pump", 1.0F, 1.0F);

        for (int i = 0; i < 14; ++i) {
            AC_UtilBullet.fireBullet(world, player, 0.12F, 2);
        }

        stack.setDamage(stack.getAuxValue() + 1);
        exStack.setTimeLeft(40);
        if (stack.getAuxValue() == stack.getMaxDamage()) {
            exStack.setReloading(true);
        }
        return stack;
    }

    @Override
    public boolean isLighting(Entity entity, ItemInstance stack) {
        return ((ExItemStack) stack).getTimeLeft() > 42;
    }

    @Override
    public boolean isMuzzleFlash(Entity entity, ItemInstance stack) {
        return ((ExItemStack) stack).getTimeLeft() > 35;
    }

    @Override
    public void reload(ItemInstance stack, Level world, Player player) {
        var exStack = (ExItemStack) stack;
        if (stack.getAuxValue() > 0 && player.inventory.removeResource(AC_Items.shotgunAmmo.id)) {
            stack.setDamage(stack.getAuxValue() - 1);
            exStack.setTimeLeft(20);

            world.playSound(player, "items.shotgun.reload", 1.0F, 1.0F);
            if (stack.getAuxValue() == 0) {
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
