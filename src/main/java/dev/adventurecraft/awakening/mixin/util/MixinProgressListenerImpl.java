package dev.adventurecraft.awakening.mixin.util;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.util.ProgressListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ProgressListenerImpl.class)
public abstract class MixinProgressListenerImpl {

    @ModifyArg(
        method = "notifyIgnoreGameRunning",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/ProgressListenerImpl;notify(Ljava/lang/String;)V"))
    private String useArgForMessage(String string, @Local(argsOnly = true) String message) {
        return message;
    }
}
