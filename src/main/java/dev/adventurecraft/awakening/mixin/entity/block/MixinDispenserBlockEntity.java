package dev.adventurecraft.awakening.mixin.entity.block;

import net.minecraft.entity.BlockEntity;
import net.minecraft.entity.block.DispenserBlockEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(DispenserBlockEntity.class)
public abstract class MixinDispenserBlockEntity extends BlockEntity {

    @Shadow
    private ItemStack[] contents;

    @Overwrite
    public ItemStack takeInventoryItem(int slot, int count) {
        return this.takeInventoryItem(slot, count, false);
    }

    @Unique
    private ItemStack takeInventoryItem(int slot, int count, boolean dispense) {
        if (this.contents[slot] == null) {
            return null;
        }

        ItemStack stack;
        if (this.contents[slot].count <= count && !dispense) {
            stack = this.contents[slot];
            this.contents[slot] = null;
            this.markDirty();
        } else if (this.contents[slot].count < 0) {
            stack = this.contents[slot].copy();
            stack.count = 1;
        } else {
            stack = this.contents[slot].split(count);
            if (this.contents[slot].count == 0) {
                this.contents[slot] = null;
            }
            this.markDirty();
        }
        return stack;
    }

    @Redirect(
        method = "getItemToDispense",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/block/DispenserBlockEntity;takeInventoryItem(II)Lnet/minecraft/item/ItemStack;"))
    private ItemStack takeItemToDispense(DispenserBlockEntity instance, int j, int i) {
        return ((MixinDispenserBlockEntity) (Object) instance).takeInventoryItem(j, i, true);
    }
}
