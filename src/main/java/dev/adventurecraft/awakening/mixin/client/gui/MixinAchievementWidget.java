package dev.adventurecraft.awakening.mixin.client.gui;

import net.minecraft.client.gui.AchievementGetComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AchievementGetComponent.class)
public abstract class MixinAchievementWidget {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void disable_renderBannerAndLicenseText(CallbackInfo ci) {
        ci.cancel();
    }
}
