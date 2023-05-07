package dev.adventurecraft.awakening.mixin.container;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import net.minecraft.client.Minecraft;
import net.minecraft.container.PlayerContainer;
import net.minecraft.container.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(PlayerContainer.class)
public abstract class MixinPlayerContainer {

    @WrapWithCondition(
        method = "<init>(Lnet/minecraft/inventory/PlayerInventory;Z)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/container/PlayerContainer;addSlot(Lnet/minecraft/container/slot/Slot;)V"),
        slice = @Slice(
            to = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/container/PlayerContainer;addSlot(Lnet/minecraft/container/slot/Slot;)V",
                ordinal = 1)))
    private boolean conditionalCraftingSlots(PlayerContainer instance, Slot slot) {
        return ((ExWorldProperties) Minecraft.instance.world.properties).isInventoryCraftingAllowed();
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
}
