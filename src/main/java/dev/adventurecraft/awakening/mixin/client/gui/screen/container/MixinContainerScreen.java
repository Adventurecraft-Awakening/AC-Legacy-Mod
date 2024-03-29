package dev.adventurecraft.awakening.mixin.client.gui.screen.container;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.adventurecraft.awakening.extension.container.slot.ExSlot;
import dev.adventurecraft.awakening.mixin.client.gui.screen.MixinScreen;
import net.minecraft.client.gui.screen.container.ContainerScreen;
import net.minecraft.container.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ContainerScreen.class)
public abstract class MixinContainerScreen extends MixinScreen {

    @WrapOperation(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screen/container/ContainerScreen;isMouseOverSlot(Lnet/minecraft/container/slot/Slot;II)Z"))
    private boolean wrapSlotHover(ContainerScreen instance, Slot slot, int x, int y, Operation<Boolean> original) {
        if (((ExSlot) slot).getEnabled()) {
            return original.call(instance, slot, x, y);
        }
        return false;
    }
}
