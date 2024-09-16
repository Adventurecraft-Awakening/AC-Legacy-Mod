package dev.adventurecraft.awakening.mixin.entity.block;

import net.minecraft.world.ItemInstance;
import net.minecraft.world.level.tile.entity.DispenserTileEntity;
import net.minecraft.world.level.tile.entity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(DispenserTileEntity.class)
public abstract class MixinDispenserBlockEntity extends TileEntity {

    @Shadow
    private ItemInstance[] contents;

    @Overwrite
    public ItemInstance takeInventoryItem(int slot, int count) {
        return this.takeInventoryItem(slot, count, false);
    }

    @Unique
    private ItemInstance takeInventoryItem(int slot, int count, boolean dispense) {
        if (this.contents[slot] == null) {
            return null;
        }

        ItemInstance stack;
        if (this.contents[slot].count <= count && !dispense) {
            stack = this.contents[slot];
            this.contents[slot] = null;
            this.setChanged();
        } else if (this.contents[slot].count < 0) {
            stack = this.contents[slot].copy();
            stack.count = 1;
        } else {
            stack = this.contents[slot].shrink(count);
            if (this.contents[slot].count == 0) {
                this.contents[slot] = null;
            }
            this.setChanged();
        }
        return stack;
    }

    @Redirect(
        method = "getItemToDispense",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/block/DispenserBlockEntity;takeInventoryItem(II)Lnet/minecraft/item/ItemStack;"))
    private ItemInstance takeItemToDispense(DispenserTileEntity instance, int j, int i) {
        return ((MixinDispenserBlockEntity) (Object) instance).takeInventoryItem(j, i, true);
    }
}
