package dev.adventurecraft.awakening.mixin.container;

import dev.adventurecraft.awakening.mixin.container.slot.MixinSlot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.container.PlayerContainer$1")
public abstract class MixinPlayerContainerSlot extends MixinSlot {

    @Inject(method = "canInsert", at = @At("HEAD"), cancellable = true)
    private void cancelCanInsert(ItemStack item, CallbackInfoReturnable<Boolean> cir) {
        if (!this.getEnabled()) {
            cir.setReturnValue(false);
        }
    }
}
