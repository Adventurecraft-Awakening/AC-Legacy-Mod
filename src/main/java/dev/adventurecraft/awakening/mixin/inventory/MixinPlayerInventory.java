package dev.adventurecraft.awakening.mixin.inventory;

import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.common.AC_IItemReload;
import dev.adventurecraft.awakening.common.AC_Items;
import dev.adventurecraft.awakening.extension.entity.player.ExPlayerEntity;
import dev.adventurecraft.awakening.extension.inventory.ExPlayerInventory;
import dev.adventurecraft.awakening.extension.item.ExArmorItem;
import dev.adventurecraft.awakening.extension.item.ExItem;
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
        @Local(ordinal = 0) int id,
        @Local(ordinal = 2) int slot) {
        ((ExItem) Item.byId[id]).onAddToSlot(this.player, slot, stack.getMeta());
    }

    @Overwrite
    public void tickInventory() {
        for (int slot = 0; slot < this.main.length; ++slot) {
            ItemStack stack = this.main[slot];
            if (stack == null) {
                continue;
            }
            stack.tick(this.player.world, this.player, slot, this.selectedHotBarSlot == slot);

            ExItemStack exItem = ((ExItemStack) (Object) stack);
            if (exItem.getTimeLeft() > 0) {
                exItem.setTimeLeft(exItem.getTimeLeft() - 1);
            }

            if ((slot == this.selectedHotBarSlot || slot == this.offhandItem) &&
                exItem.getTimeLeft() == 0 &&
                exItem.getReloading()) {
                var itemReload = (AC_IItemReload) Item.byId[stack.itemId];
                itemReload.reload(stack, this.player.world, this.player);
            }

            if (stack.getMeta() > 0 &&
                ((ExItem) Item.byId[stack.itemId]).getDecrementDamage()) {
                stack.setMeta(stack.getMeta() - 1);
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
            int meta = stack.getMeta();
            this.main[slot] = null;
            ((ExItem) Item.byId[itemId]).onRemovedFromSlot(this.player, slot, meta);
        }

        return true;
    }

    @Overwrite
    public boolean addStack(ItemStack stack) {
        ExPlayerEntity exPlayer = (ExPlayerEntity) this.player;
        if (stack.itemId == AC_Items.heart.id) {
            stack.count = 0;
            this.player.addHealth(4);
            return true;
        } else if (stack.itemId == AC_Items.heartContainer.id) {
            stack.count = 0;
            exPlayer.setMaxHealth(exPlayer.getMaxHealth() + 4);
            this.player.addHealth(exPlayer.getMaxHealth());
            return true;
        } else if (stack.itemId == AC_Items.heartPiece.id) {
            stack.count = 0;
            exPlayer.setHeartPiecesCount(exPlayer.getHeartPiecesCount() + 1);
            if (exPlayer.getHeartPiecesCount() >= 4) {
                exPlayer.setHeartPiecesCount(0);
                exPlayer.setMaxHealth(exPlayer.getMaxHealth() + 4);
                this.player.addHealth(exPlayer.getMaxHealth());
            }
            return true;
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
            ((ExItem) Item.byId[stack.itemId]).onAddToSlot(this.player, emptySlot, stack.getMeta());
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
            ((ExItem) Item.byId[stack.itemId]).onRemovedFromSlot(this.player, originalSlot, stack.getMeta());
        } else {
            stack = stack.split(count);
            if (stack.count == 0) {
                stacks[slot] = null;
                ((ExItem) Item.byId[stack.itemId]).onRemovedFromSlot(this.player, originalSlot, stack.getMeta());
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
            ((ExItem) Item.byId[originalStack.itemId]).onRemovedFromSlot(this.player, originalSlot, originalStack.getMeta());
        }

        if (stack != null) {
            ((ExItem) Item.byId[stack.itemId]).onAddToSlot(this.player, originalSlot, stack.getMeta());
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
            ItemStack item = this.armor[slot];
            if (item != null && item.getItem() instanceof ArmorItem) {
                item.applyDamage(damage, this.player);
                if (item.count == 0) {
                    int id = item.itemId;
                    int meta = item.getMeta();
                    item.unusedEmptyMethod1(this.player);
                    this.armor[slot] = null;
                    ((ExItem) Item.byId[id]).onRemovedFromSlot(this.player, slot + this.main.length, meta);
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
                ((ExItem) Item.byId[stack.itemId]).onRemovedFromSlot(this.player, slot, stack.getMeta());
            }
        }

        for (int slot = 0; slot < this.armor.length; ++slot) {
            ItemStack stack = this.armor[slot];
            if (stack != null) {
                this.player.dropItem(stack, true);
                this.armor[slot] = null;
                ((ExItem) Item.byId[stack.itemId]).onRemovedFromSlot(this.player, slot + this.main.length, stack.getMeta());
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
                ((ExItem) Item.byId[id]).onRemovedFromSlot(this.player, cSlot, meta);
            }
        }

        return true;
    }
}
