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
    private ItemInstance[] items;

    @Overwrite
    public ItemInstance removeItem(int slot, int count) {
        return this.takeInventoryItem(slot, count, false);
    }

    @Unique
    private ItemInstance takeInventoryItem(int slot, int count, boolean dispense) {
        if (this.items[slot] == null) {
            return null;
        }

        ItemInstance stack;
        if (this.items[slot].count <= count && !dispense) {
            stack = this.items[slot];
            this.items[slot] = null;
            this.setChanged();
        } else if (this.items[slot].count < 0) {
            stack = this.items[slot].copy();
            stack.count = 1;
        } else {
            stack = this.items[slot].shrink(count);
            if (this.items[slot].count == 0) {
                this.items[slot] = null;
            }
            this.setChanged();
        }
        return stack;
    }

    @Redirect(
        method = "removeRandomItem",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/tile/entity/DispenserTileEntity;removeItem(II)Lnet/minecraft/world/ItemInstance;"))
    private ItemInstance takeItemToDispense(DispenserTileEntity instance, int j, int i) {
        return ((MixinDispenserBlockEntity) (Object) instance).takeInventoryItem(j, i, true);
    }
}
