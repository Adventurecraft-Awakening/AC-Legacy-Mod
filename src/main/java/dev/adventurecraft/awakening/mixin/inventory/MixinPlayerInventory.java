package dev.adventurecraft.awakening.mixin.inventory;

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
    public abstract int getMaxItemCount();

    @Shadow
    protected abstract int getIdenticalStackSlot(ItemStack arg);

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
        int var1 = this.selectedHotBarSlot;
        this.selectedHotBarSlot = this.offhandItem;
        this.offhandItem = var1;
    }

    @Environment(EnvType.CLIENT)
    @Overwrite
    public void scrollInHotBar(int var1) {
        if (var1 > 0) {
            var1 = 1;
        }

        if (var1 < 0) {
            var1 = -1;
        }

        int var2 = this.selectedHotBarSlot;

        this.selectedHotBarSlot -= var1;
        while (this.selectedHotBarSlot < 0) {
            this.selectedHotBarSlot += 9;
        }

        while (this.selectedHotBarSlot >= 9) {
            this.selectedHotBarSlot -= 9;
        }

        if (this.selectedHotBarSlot == this.offhandItem) {
            this.offhandItem = var2;
        }
    }


    private int mergeStacks(ItemStack var1) {
        int var2 = var1.itemId;
        int var3 = var1.count;
        int var4 = this.getIdenticalStackSlot(var1);
        if (var4 < 0) {
            var4 = this.getFirstEmptySlotIndex();
        }
        if (var4 < 0) {
            return var3;
        }

        if (this.main[var4] == null) {
            this.main[var4] = new ItemStack(var2, 0, var1.getMeta());
            ((ExItem) Item.byId[var2]).onAddToSlot(this.player, var4, var1.getMeta());
        }

        int var5 = Math.min(var3, this.main[var4].getMaxStackSize() - this.main[var4].count);
        var5 = Math.min(var5, this.getMaxItemCount() - this.main[var4].count);

        if (var5 != 0) {
            var3 -= var5;
            this.main[var4].count += var5;
            this.main[var4].cooldown = 5;
        }
        return var3;
    }

    @Overwrite
    public void tickInventory() {
        for (int var1 = 0; var1 < this.main.length; ++var1) {
            if (this.main[var1] != null) {
                ItemStack var2 = this.main[var1];
                var2.tick(this.player.world, this.player, var1, this.selectedHotBarSlot == var1);

                ExItemStack exItem = ((ExItemStack) (Object) var2);
                if (exItem.getTimeLeft() > 0) {
                    exItem.setTimeLeft(exItem.getTimeLeft() - 1);
                }

                if ((var1 == this.selectedHotBarSlot || var1 == this.offhandItem) && exItem.getTimeLeft() == 0 && exItem.getReloading()) {
                    AC_IItemReload var3 = (AC_IItemReload) Item.byId[var2.itemId];
                    var3.reload(var2, this.player.world, this.player);
                }

                if (var2.getMeta() > 0 && ((ExItem) Item.byId[var2.itemId]).getDecrementDamage()) {
                    var2.setMeta(var2.getMeta() - 1);
                }
            }
        }

    }

    @Overwrite
    public boolean removeItem(int var1) {
        int var2 = this.getSlotWithItem(var1);
        if (var2 < 0) {
            return false;
        } else {
            if (--this.main[var2].count == 0) {
                int var3 = this.main[var2].getMeta();
                this.main[var2] = null;
                ((ExItem) Item.byId[var1]).onRemovedFromSlot(this.player, var2, var3);
            }

            return true;
        }
    }

    @Overwrite
    public boolean addStack(ItemStack var1) {
        ExPlayerEntity exPlayer = (ExPlayerEntity) this.player;
        if (var1.itemId == AC_Items.heart.id) {
            var1.count = 0;
            this.player.addHealth(4);
            return true;
        } else if (var1.itemId == AC_Items.heartContainer.id) {
            var1.count = 0;
            exPlayer.setMaxHealth(exPlayer.getMaxHealth() + 4);
            this.player.addHealth(exPlayer.getMaxHealth());
            return true;
        } else if (var1.itemId == AC_Items.heartPiece.id) {
            var1.count = 0;
            exPlayer.setHeartPiecesCount(exPlayer.getHeartPiecesCount() + 1);
            if (exPlayer.getHeartPiecesCount() >= 4) {
                exPlayer.setHeartPiecesCount(0);
                exPlayer.setMaxHealth(exPlayer.getMaxHealth() + 4);
                this.player.addHealth(exPlayer.getMaxHealth());
            }
            return true;
        } else {
            if (!var1.isDamaged()) {
                var1.count = this.mergeStacks(var1);
                if (var1.count == 0) {
                    return true;
                }
            }

            int var2 = this.getFirstEmptySlotIndex();
            if (var2 >= 0) {
                this.main[var2] = var1.copy();
                this.main[var2].cooldown = 5;
                var1.count = 0;
                ((ExItem) Item.byId[var1.itemId]).onAddToSlot(this.player, var2, var1.getMeta());
                return true;
            } else {
                return false;
            }
        }
    }

    @Overwrite
    public ItemStack takeInventoryItem(int var1, int var2) {
        int var3 = var1;
        ItemStack[] var4 = this.main;
        if (var1 >= this.main.length) {
            var4 = this.armor;
            var1 -= this.main.length;
        }

        if (var4[var1] != null) {
            ItemStack var5;
            if (var4[var1].count <= var2) {
                var5 = var4[var1];
                var4[var1] = null;
                ((ExItem) Item.byId[var5.itemId]).onRemovedFromSlot(this.player, var3, var5.getMeta());
            } else {
                var5 = var4[var1].split(var2);
                if (var4[var1].count == 0) {
                    var4[var1] = null;
                    ((ExItem) Item.byId[var5.itemId]).onRemovedFromSlot(this.player, var3, var5.getMeta());
                }
            }
            return var5;
        } else {
            return null;
        }
    }

    @Overwrite
    public void setInventoryItem(int var1, ItemStack var2) {
        int var3 = var1;
        ItemStack[] var4 = this.main;
        if (var1 >= var4.length) {
            var1 -= var4.length;
            var4 = this.armor;
        }

        ItemStack var5 = var4[var1];
        var4[var1] = var2;
        if (var5 != null) {
            ((ExItem) Item.byId[var5.itemId]).onRemovedFromSlot(this.player, var3, var5.getMeta());
        }

        if (var2 != null) {
            ((ExItem) Item.byId[var2.itemId]).onAddToSlot(this.player, var3, var2.getMeta());
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
    public void damageArmor(int var1) {
        for (int var2 = 0; var2 < this.armor.length; ++var2) {
            if (this.armor[var2] != null && this.armor[var2].getItem() instanceof ArmorItem) {
                this.armor[var2].applyDamage(var1, this.player);
                if (this.armor[var2].count == 0) {
                    int var3 = this.armor[var2].itemId;
                    int var4 = this.armor[var2].getMeta();
                    this.armor[var2].unusedEmptyMethod1(this.player);
                    this.armor[var2] = null;
                    ((ExItem) Item.byId[var3]).onRemovedFromSlot(this.player, var2 + this.main.length, var4);
                }
            }
        }
    }

    @Overwrite
    public void dropInventory() {
        int var1;
        ItemStack var2;
        for (var1 = 0; var1 < this.main.length; ++var1) {
            if (this.main[var1] != null) {
                var2 = this.main[var1];
                this.player.dropItem(this.main[var1], true);
                this.main[var1] = null;
                ((ExItem) Item.byId[var2.itemId]).onRemovedFromSlot(this.player, var1, var2.getMeta());
            }
        }

        for (var1 = 0; var1 < this.armor.length; ++var1) {
            if (this.armor[var1] != null) {
                var2 = this.armor[var1];
                this.player.dropItem(this.armor[var1], true);
                this.armor[var1] = null;
                ((ExItem) Item.byId[var2.itemId]).onRemovedFromSlot(this.player, var1 + this.main.length, var2.getMeta());
            }
        }
    }

    @Override
    public boolean consumeItemAmount(int var1, int var2, int var3) {
        int var4 = 0;
        int var5 = var3;

        int var6;
        for (var6 = 0; var6 < 36; ++var6) {
            if (this.main[var6] != null && this.main[var6].itemId == var1 && this.main[var6].getMeta() == var2) {
                this.consumeInventory[var4++] = var6;
                var5 -= this.main[var6].count;
                if (var5 <= 0) {
                    break;
                }
            }
        }

        if (var5 > 0) {
            return false;
        } else {
            for (var6 = 0; var6 < var4; ++var6) {
                int var7 = this.consumeInventory[var6];
                if (this.main[var7].count > var3) {
                    this.main[var7].count -= var3;
                } else {
                    var3 -= this.main[var7].count;
                    this.main[var7].count = 0;
                    this.main[var7] = null;
                    ((ExItem) Item.byId[var1]).onRemovedFromSlot(this.player, var7, var2);
                }
            }

            return true;
        }
    }
}
