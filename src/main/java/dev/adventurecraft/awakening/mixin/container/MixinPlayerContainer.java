package dev.adventurecraft.awakening.mixin.container;

import dev.adventurecraft.awakening.extension.container.ExPlayerContainer;
import dev.adventurecraft.awakening.extension.container.slot.ExSlot;
import net.minecraft.container.Container;
import net.minecraft.container.PlayerContainer;
import net.minecraft.container.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

import java.util.ArrayList;

@Mixin(PlayerContainer.class)
public abstract class MixinPlayerContainer extends Container implements ExPlayerContainer {

    private boolean allowsCrafting = true;
    private final ArrayList<Slot> craftingSlots = new ArrayList<>();

    @Redirect(
        method = "<init>(Lnet/minecraft/inventory/PlayerInventory;Z)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/container/PlayerContainer;addSlot(Lnet/minecraft/container/slot/Slot;)V"),
        slice = @Slice(
            to = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/container/PlayerContainer;addSlot(Lnet/minecraft/container/slot/Slot;)V",
                ordinal = 1)))
    private void recordCraftingSlots(PlayerContainer instance, Slot slot) {
        instance.addSlot(slot);
        this.craftingSlots.add(slot);
    }

    @ModifyArg(
        method = "<init>(Lnet/minecraft/inventory/PlayerInventory;Z)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/container/slot/CraftingResultSlot;<init>(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/inventory/Inventory;III)V",
            ordinal = 0),
        index = 5)
    private int modifyCraftingResultSlotY(int y) {
        return y + 16;
    }

    @ModifyArg(
        method = "<init>(Lnet/minecraft/inventory/PlayerInventory;Z)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/container/slot/Slot;<init>(Lnet/minecraft/inventory/Inventory;III)V",
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
