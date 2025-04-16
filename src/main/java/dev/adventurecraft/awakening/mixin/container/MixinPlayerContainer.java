package dev.adventurecraft.awakening.mixin.container;

import dev.adventurecraft.awakening.extension.container.ExPlayerContainer;
import dev.adventurecraft.awakening.extension.container.slot.ExSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

import java.util.ArrayList;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;

@Mixin(InventoryMenu.class)
public abstract class MixinPlayerContainer extends AbstractContainerMenu implements ExPlayerContainer {

    private boolean allowsCrafting = true;
    private final ArrayList<Slot> craftingSlots = new ArrayList<>();

    @Redirect(
        method = "<init>(Lnet/minecraft/world/entity/player/Inventory;Z)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/inventory/InventoryMenu;addSlot(Lnet/minecraft/world/inventory/Slot;)V"),
        slice = @Slice(
            to = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/world/inventory/InventoryMenu;addSlot(Lnet/minecraft/world/inventory/Slot;)V",
                ordinal = 1)))
    private void recordCraftingSlots(InventoryMenu instance, Slot slot) {
        instance.addSlot(slot);
        this.craftingSlots.add(slot);
    }

    @ModifyArg(
        method = "<init>(Lnet/minecraft/world/entity/player/Inventory;Z)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/inventory/ResultSlot;<init>(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/Container;Lnet/minecraft/world/Container;III)V",
            ordinal = 0),
        index = 5)
    private int modifyCraftingResultSlotY(int y) {
        return y + 16;
    }

    @ModifyArg(
        method = "<init>(Lnet/minecraft/world/entity/player/Inventory;Z)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/inventory/Slot;<init>(Lnet/minecraft/world/Container;III)V",
            ordinal = 0),
        index = 3)
    private int modifyCraftingSlotY(int y) {
        return y + 16;
    }

    @Override
    public boolean getAllowsCrafting() {
        return this.allowsCrafting;
    }

    @Override
    public void setAllowsCrafting(boolean value) {
        if (this.allowsCrafting != value) {
            this.allowsCrafting = value;

            for (Slot slot : this.craftingSlots) {
                ((ExSlot) slot).setEnabled(this.allowsCrafting);
            }
        }
    }
}
