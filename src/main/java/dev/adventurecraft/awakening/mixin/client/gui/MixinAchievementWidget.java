package dev.adventurecraft.awakening.mixin.client.gui;

import net.minecraft.client.gui.AchievementWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AchievementWidget.class)
public abstract class MixinAchievementWidget {

    @Inject(method = "renderBannerAndLicenseText", at = @At("HEAD"), cancellable = true)
    public void disable_renderBannerAndLicenseText(CallbackInfo ci) {
        ci.cancel();
    }
}
