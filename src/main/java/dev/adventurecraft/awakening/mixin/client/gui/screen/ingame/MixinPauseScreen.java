package dev.adventurecraft.awakening.mixin.client.gui.screen.ingame;

import dev.adventurecraft.awakening.extension.client.ExMinecraft;
import dev.adventurecraft.awakening.extension.client.sound.ExSoundHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.PauseScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PauseScreen.class)
public abstract class MixinPauseScreen extends Screen {

    @Inject(
        method = "buttonClicked",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Minecraft;openScreen(Lnet/minecraft/client/gui/screen/Screen;)V",
            shift = At.Shift.BEFORE,
            ordinal = 1))
    private void resetOnExit(ButtonWidget button, CallbackInfo ci) {
        //((ExInGameHud) this.client.overlay).getScriptUI().clear(); TODO
        ((ExMinecraft) this.client).setCameraActive(false);
        ((ExSoundHelper) this.client.soundHelper).stopMusic();
    }
}

