package dev.adventurecraft.awakening.mixin.client.gui.screen.ingame;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.DeathScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(DeathScreen.class)
public abstract class MixinDeathScreen {

    @Redirect(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screen/ingame/DeathScreen;drawTextWithShadowCentred(Lnet/minecraft/client/render/TextRenderer;Ljava/lang/String;III)V",
            ordinal = 1))
    private void removeScoreText(DeathScreen instance, Font textRenderer, String var1, int var2, int var3, int var4) {
    }
}
