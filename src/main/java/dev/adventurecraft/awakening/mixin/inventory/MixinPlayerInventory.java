package dev.adventurecraft.awakening.mixin.inventory;

import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.item.AC_IItemReload;
import dev.adventurecraft.awakening.item.AC_Items;
import dev.adventurecraft.awakening.item.AC_ISlotCallbackItem;
import dev.adventurecraft.awakening.extension.entity.player.ExPlayerEntity;
import dev.adventurecraft.awakening.extension.inventory.ExPlayerInventory;
import dev.adventurecraft.awakening.extension.item.ExArmorItem;
import dev.adventurecraft.awakening.extension.item.ExItemStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.LinkedList;
import java.util.List;

@Mixin(Inventory.class)
public abstract class MixinPlayerInventory implements ExPlayerInventory {

    @Shadow
    public ItemInstance[] items;
    @Shadow
    public int selected;

    @Shadow
    public Player player;
    @Shadow
    public ItemInstance[] armor;

    @Shadow
    protected abstract int getFreeSlot();

    @Shadow
    public abstract int getSlot(int i);

    @Shadow
    protected abstract int addResource(ItemInstance arg);

    public int offhandItem = 1;
    //public int[] consumeInventory = new int[36];

    @Override
    public int getOffhandItem() {
        return this.offhandItem;
    }

    @Override
    public void setOffhandItem(int value) {
        this.offhandItem = value;
    }

    public ItemInstance getOffhandItemStack() {
        return this.items[this.offhandItem];
    }

    public void swapOffhandWithMain() {
        int slot = this.selected;
        this.selected = this.offhandItem;
        this.offhandItem = slot;
    }

    @Environment(EnvType.CLIENT)
    @Overwrite
    public void swapPaint(int direction) {
        if (direction > 0) {
            direction = 1;
        }

        if (direction < 0) {
            direction = -1;
        }

        int slot = this.selected;

        this.selected -= direction;
        while (this.selected < 0) {
            this.selected += 9;
        }

        while (this.selected >= 9) {
            this.selected -= 9;
        }

        if (this.selected == this.offhandItem) {
            this.offhandItem = slot;
        }
    }

    private void onItemAddToSlot(int slot, ItemInstance stack) {
        if (Item.items[stack.id] instanceof AC_ISlotCallbackItem slotCallbackItem) {
            slotCallbackItem.onAddToSlot(this.player, slot, stack);
        }
    }

    private void onItemRemovedFromSlot(int slot, ItemInstance stack) {
        if (Item.items[stack.id] instanceof AC_ISlotCallbackItem slotCallbackItem) {
            slotCallbackItem.onRemovedFromSlot(this.player, slot, stack);
        }
    }

    @Inject(
        method = "addResource",
        at = @At(
            value = "NEW",
            target = "(III)Lnet/minecraft/world/ItemInstance;",
            shift = At.Shift.AFTER,
            ordinal = 0))
    private void onAddOnMerge(
        ItemInstance stack,
        CallbackInfoReturnable<Integer> cir,
        @Local(ordinal = 2) int slot) {
        this.onItemAddToSlot(slot, stack);
    }

    @Overwrite
    public void tick() {
        for (int slot = 0; slot < this.items.length; ++slot) {
            ItemInstance stack = this.items[slot];
            if (stack == null) {
                continue;
            }
            stack.inventoryTick(this.player.level, this.player, slot, this.selected == slot);

            var exItem = (ExItemStack) stack;
            if (exItem.getTimeLeft() > 0) {
                exItem.setTimeLeft(exItem.getTimeLeft() - 1);
            }

            if ((slot == this.selected || slot == this.offhandItem) &&
                exItem.getTimeLeft() == 0 &&
                exItem.getReloading()) {
                if (Item.items[stack.id] instanceof AC_IItemReload itemReload) {
                    itemReload.reload(stack, this.player.level, this.player);
                }
            }
        }
    }

    @Overwrite
    public boolean removeResource(int itemId) {
        int slot = this.getSlot(itemId);
        if (slot < 0) {
            return false;
        }

        ItemInstance stack = this.items[slot];
        if (--stack.count == 0) {
            this.items[slot] = null;
            this.onItemRemovedFromSlot(slot, stack);
        }

        return true;
    }

    @Overwrite
    public boolean add(ItemInstance stack) {
        var exPlayer = (ExPlayerEntity) this.player;
        if (stack.count > 0) {
            if (stack.id == AC_Items.heart.id) {
                int heal = stack.count * 4;
                stack.count = 0;

                this.player.heal(heal);
                return true;
            }
            if (stack.id == AC_Items.heartContainer.id) {
                int extraHealth = stack.count * 4;
                stack.count = 0;

                exPlayer.setMaxHealth(exPlayer.getMaxHealth() + extraHealth);
                this.player.heal(exPlayer.getMaxHealth());
                return true;
            }
            if (stack.id == AC_Items.heartPiece.id) {
                int pieces = exPlayer.getHeartPiecesCount() + stack.count;
                stack.count = 0;

                int extraHearts = pieces / 4;
                exPlayer.setHeartPiecesCount(pieces % 4);

                if (extraHearts > 0) {
                    exPlayer.setMaxHealth(exPlayer.getMaxHealth() + extraHearts * 4);
                    this.player.heal(exPlayer.getMaxHealth());
                }
                return true;
            }
        }

        if (!stack.isDamaged()) {
            stack.count = this.addResource(stack);
            if (stack.count == 0) {
                return true;
            }
        }

        int emptySlot = this.getFreeSlot();
        if (emptySlot >= 0) {
            this.items[emptySlot] = stack.copy();
            this.items[emptySlot].popTime = 5;
            stack.count = 0;
            this.onItemAddToSlot(emptySlot, stack);
            return true;
        } else {
            return false;
        }
    }

    @Overwrite
    public ItemInstance removeItem(int slot, int count) {
        int originalSlot = slot;
        ItemInstance[] stacks = this.items;
        if (slot >= this.items.length) {
            stacks = this.armor;
            slot -= this.items.length;
        }

        ItemInstance stack = stacks[slot];
        if (stack == null) {
            return null;
        }

        if (stack.count <= count) {
            stacks[slot] = null;
            this.onItemRemovedFromSlot(originalSlot, stack);
        } else {
            stack = stack.shrink(count);
            if (stack.count == 0) {
                stacks[slot] = null;
                onItemRemovedFromSlot(originalSlot, stack);
            }
        }
        return stack;
    }

    @Overwrite
    public void setItem(int slot, ItemInstance stack) {
        int originalSlot = slot;
        ItemInstance[] stackArray = this.items;
        if (slot >= stackArray.length) {
            slot -= stackArray.length;
            stackArray = this.armor;
        }

        ItemInstance originalStack = stackArray[slot];
        stackArray[slot] = stack;
        if (originalStack != null) {
            this.onItemRemovedFromSlot(originalSlot, originalStack);
        }

        if (stack != null) {
            this.onItemAddToSlot(originalSlot, stack);
        }
    }

    @Overwrite
    public int getArmorValue() {
        float maxDamage = 0.0F;
        int remaining = 0;
        int durability = 0;

        for (ItemInstance item : this.armor) {
            if (item != null && item.getItem() instanceof ArmorItem) {
                int itemDurability = item.getMaxDamage();
                int itemDamage = item.getDamageValue();
                int itemRemaining = itemDurability - itemDamage;
                remaining += itemRemaining;
                durability += itemDurability;
                maxDamage += ((ExArmorItem) item.getItem()).getMaxDamage();
            }
        }

        if (durability == 0) {
            return 0;
        } else {
            return (int) ((maxDamage - 1.0F) * (float) remaining) / durability + 1;
        }
    }

    @Overwrite
    public void hurtArmor(int damage) {
        for (int slot = 0; slot < this.armor.length; ++slot) {
            ItemInstance stack = this.armor[slot];
            if (stack != null && stack.getItem() instanceof ArmorItem) {
                stack.hurtAndBreak(damage, this.player);
                if (stack.count == 0) {
                    stack.snap(this.player);
                    this.armor[slot] = null;
                    this.onItemRemovedFromSlot(slot + this.items.length, stack);
                }
            }
        }
    }

    @Overwrite
    public void dropAll() {
        for (int slot = 0; slot < this.items.length; ++slot) {
            ItemInstance stack = this.items[slot];
            if (stack != null) {
                this.player.drop(stack, true);
                this.items[slot] = null;
                this.onItemRemovedFromSlot(slot, stack);
            }
        }

        for (int slot = 0; slot < this.armor.length; ++slot) {
            ItemInstance stack = this.armor[slot];
            if (stack != null) {
                this.player.drop(stack, true);
                this.armor[slot] = null;
                this.onItemRemovedFromSlot(slot + this.items.length, stack);
            }
        }
    }

    @Override
    public boolean consumeItemAmount(int id, int meta, int count) {
        int remaining = count;
        List<Integer> slots = new LinkedList<>();
        for(int i = 0; i < this.items.length; i++) {
            ItemInstance itemStack = this.items[i];

            if (itemStack == null) {
                continue;
            }
            if (itemStack.id != id){
                continue;
            }
            if(itemStack.getAuxValue() != meta){
                continue;
            }
            slots.add(i);
            remaining -= itemStack.count;
            if(remaining <= 0){
                break;
            }
        }

        if(remaining > 0){
            return false;
        }
        for(int slot : slots) {
            ItemInstance itemStack = this.items[slot];

            if (itemStack.count > count) {
                itemStack.count -= count;
                break;
            }
            count -= itemStack.count;
            itemStack.count = 0;
            this.items[slot] = null;
            this.onItemRemovedFromSlot(slot,itemStack);
        }
        return true;
    }
}
