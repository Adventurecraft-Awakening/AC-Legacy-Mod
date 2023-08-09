package dev.adventurecraft.awakening.mixin.inventory;

import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.common.AC_IItemReload;
import dev.adventurecraft.awakening.common.AC_Items;
import dev.adventurecraft.awakening.common.AC_ISlotCallbackItem;
import dev.adventurecraft.awakening.extension.entity.player.ExPlayerEntity;
import dev.adventurecraft.awakening.extension.inventory.ExPlayerInventory;
import dev.adventurecraft.awakening.extension.item.ExArmorItem;
import dev.adventurecraft.awakening.extension.item.ExItemStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.PlayerInventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInventory.class)
public abstract class MixinPlayerInventory implements ExPlayerInventory {

    @Shadow
    public ItemStack[] main;
    @Shadow
    public int selectedHotBarSlot;

    @Shadow
    public PlayerEntity player;
    @Shadow
    public ItemStack[] armor;

    @Shadow
    protected abstract int getFirstEmptySlotIndex();

    @Shadow
    public abstract int getSlotWithItem(int i);

    @Shadow
    protected abstract int mergeStacks(ItemStack arg);

    public int offhandItem = 1;
    public int[] consumeInventory = new int[36];

    @Override
    public int getOffhandItem() {
        return this.offhandItem;
    }

    @Override
    public void setOffhandItem(int value) {
        this.offhandItem = value;
    }

    public ItemStack getOffhandItemStack() {
        return this.main[this.offhandItem];
    }

    public void swapOffhandWithMain() {
        int slot = this.selectedHotBarSlot;
        this.selectedHotBarSlot = this.offhandItem;
        this.offhandItem = slot;
    }

    @Environment(EnvType.CLIENT)
    @Overwrite
    public void scrollInHotBar(int direction) {
        if (direction > 0) {
            direction = 1;
        }

        if (direction < 0) {
            direction = -1;
        }

        int slot = this.selectedHotBarSlot;

        this.selectedHotBarSlot -= direction;
        while (this.selectedHotBarSlot < 0) {
            this.selectedHotBarSlot += 9;
        }

        while (this.selectedHotBarSlot >= 9) {
            this.selectedHotBarSlot -= 9;
        }

        if (this.selectedHotBarSlot == this.offhandItem) {
            this.offhandItem = slot;
        }
    }

    private void onItemAddToSlot(int slot, ItemStack stack) {
        if (Item.byId[stack.itemId] instanceof AC_ISlotCallbackItem slotCallbackItem) {
            slotCallbackItem.onAddToSlot(this.player, slot, stack);
        }
    }

    private void onItemRemovedFromSlot(int slot, ItemStack stack) {
        if (Item.byId[stack.itemId] instanceof AC_ISlotCallbackItem slotCallbackItem) {
            slotCallbackItem.onRemovedFromSlot(this.player, slot, stack);
        }
    }

    @Inject(
        method = "mergeStacks",
        at = @At(
            value = "NEW",
            target = "(III)Lnet/minecraft/item/ItemStack;",
            shift = At.Shift.AFTER,
            ordinal = 0))
    private void onAddOnMerge(
        ItemStack stack,
        CallbackInfoReturnable<Integer> cir,
        @Local(ordinal = 2) int slot) {
        this.onItemAddToSlot(slot, stack);
    }

    @Overwrite
    public void tickInventory() {
        for (int slot = 0; slot < this.main.length; ++slot) {
            ItemStack stack = this.main[slot];
            if (stack == null) {
                continue;
            }
            stack.tick(this.player.world, this.player, slot, this.selectedHotBarSlot == slot);

            var exItem = (ExItemStack) stack;
            if (exItem.getTimeLeft() > 0) {
                exItem.setTimeLeft(exItem.getTimeLeft() - 1);
            }

            if ((slot == this.selectedHotBarSlot || slot == this.offhandItem) &&
                exItem.getTimeLeft() == 0 &&
                exItem.getReloading()) {
                if (Item.byId[stack.itemId] instanceof AC_IItemReload itemReload) {
                    itemReload.reload(stack, this.player.world, this.player);
                }
            }
        }
    }

    @Overwrite
    public boolean removeItem(int itemId) {
        int slot = this.getSlotWithItem(itemId);
        if (slot < 0) {
            return false;
        }

        ItemStack stack = this.main[slot];
        if (--stack.count == 0) {
            this.main[slot] = null;
            this.onItemRemovedFromSlot(slot, stack);
        }

        return true;
    }

    @Overwrite
    public boolean addStack(ItemStack stack) {
        var exPlayer = (ExPlayerEntity) this.player;
        if (stack.count > 0) {
            if (stack.itemId == AC_Items.heart.id) {
                int heal = stack.count * 4;
                stack.count = 0;

                this.player.addHealth(heal);
                return true;
            }
            if (stack.itemId == AC_Items.heartContainer.id) {
                int extraHealth = stack.count * 4;
                stack.count = 0;

                exPlayer.setMaxHealth(exPlayer.getMaxHealth() + extraHealth);
                this.player.addHealth(exPlayer.getMaxHealth());
                return true;
            }
            if (stack.itemId == AC_Items.heartPiece.id) {
                int pieces = exPlayer.getHeartPiecesCount() + stack.count;
                stack.count = 0;

                int extraHearts = pieces / 4;
                exPlayer.setHeartPiecesCount(pieces % 4);

                if (extraHearts > 0) {
                    exPlayer.setMaxHealth(exPlayer.getMaxHealth() + extraHearts * 4);
                    this.player.addHealth(exPlayer.getMaxHealth());
                }
                return true;
            }
        }

        if (!stack.isDamaged()) {
            stack.count = this.mergeStacks(stack);
            if (stack.count == 0) {
                return true;
            }
        }

        int emptySlot = this.getFirstEmptySlotIndex();
        if (emptySlot >= 0) {
            this.main[emptySlot] = stack.copy();
            this.main[emptySlot].cooldown = 5;
            stack.count = 0;
            this.onItemAddToSlot(emptySlot, stack);
            return true;
        } else {
            return false;
        }
    }

    @Overwrite
    public ItemStack takeInventoryItem(int slot, int count) {
        int originalSlot = slot;
        ItemStack[] stacks = this.main;
        if (slot >= this.main.length) {
            stacks = this.armor;
            slot -= this.main.length;
        }

        ItemStack stack = stacks[slot];
        if (stack == null) {
            return null;
        }

        if (stack.count <= count) {
            stacks[slot] = null;
            this.onItemRemovedFromSlot(originalSlot, stack);
        } else {
            stack = stack.split(count);
            if (stack.count == 0) {
                stacks[slot] = null;
                onItemRemovedFromSlot(originalSlot, stack);
            }
        }
        return stack;
    }

    @Overwrite
    public void setInventoryItem(int slot, ItemStack stack) {
        int originalSlot = slot;
        ItemStack[] stackArray = this.main;
        if (slot >= stackArray.length) {
            slot -= stackArray.length;
            stackArray = this.armor;
        }

        ItemStack originalStack = stackArray[slot];
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

        for (ItemStack item : this.armor) {
            if (item != null && item.getItem() instanceof ArmorItem) {
                int itemDurability = item.getDurability();
                int itemDamage = item.getDamage();
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
    public void damageArmor(int damage) {
        for (int slot = 0; slot < this.armor.length; ++slot) {
            ItemStack stack = this.armor[slot];
            if (stack != null && stack.getItem() instanceof ArmorItem) {
                stack.applyDamage(damage, this.player);
                if (stack.count == 0) {
                    stack.unusedEmptyMethod1(this.player);
                    this.armor[slot] = null;
                    this.onItemRemovedFromSlot(slot + this.main.length, stack);
                }
            }
        }
    }

    @Overwrite
    public void dropInventory() {
        for (int slot = 0; slot < this.main.length; ++slot) {
            ItemStack stack = this.main[slot];
            if (stack != null) {
                this.player.dropItem(stack, true);
                this.main[slot] = null;
                this.onItemRemovedFromSlot(slot, stack);
            }
        }

        for (int slot = 0; slot < this.armor.length; ++slot) {
            ItemStack stack = this.armor[slot];
            if (stack != null) {
                this.player.dropItem(stack, true);
                this.armor[slot] = null;
                this.onItemRemovedFromSlot(slot + this.main.length, stack);
            }
        }
    }

    @Override
    public boolean consumeItemAmount(int id, int meta, int count) {
        int searchedSlots = 0;
        int remaining = count;

        for (int slot = 0; slot < 36; ++slot) {
            ItemStack stack = this.main[slot];
            if (stack != null &&
                stack.itemId == id &&
                stack.getMeta() == meta) {
                this.consumeInventory[searchedSlots++] = slot;
                remaining -= stack.count;
                if (remaining <= 0) {
                    break;
                }
            }
        }

        if (remaining > 0) {
            return false;
        }

        for (int slot = 0; slot < searchedSlots; ++slot) {
            int cSlot = this.consumeInventory[slot];
            ItemStack stack = this.main[cSlot];
            if (stack.count > count) {
                stack.count -= count;
            } else {
                count -= stack.count;
                stack.count = 0;
                this.main[cSlot] = null;
                this.onItemRemovedFromSlot(cSlot, stack);
            }
        }

        return true;
    }
}
