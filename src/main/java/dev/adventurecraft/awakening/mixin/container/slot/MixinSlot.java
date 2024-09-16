package dev.adventurecraft.awakening.mixin.container.slot;

import dev.adventurecraft.awakening.extension.container.slot.ExSlot;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class MixinSlot implements ExSlot {

    private boolean enabled = true;

    @Override
    public boolean getEnabled() {
        return this.enabled;
    }

    @Override
    public void setEnabled(boolean value) {
        this.enabled = value;
    }

    @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
    private void cancelCanInsert(ItemInstance item, CallbackInfoReturnable<Boolean> cir) {
        if (!this.enabled) {
            cir.setReturnValue(false);
        }
    }
}
