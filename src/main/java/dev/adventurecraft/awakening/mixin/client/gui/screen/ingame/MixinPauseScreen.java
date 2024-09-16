package dev.adventurecraft.awakening.mixin.client.gui.screen.ingame;

import dev.adventurecraft.awakening.extension.client.ExMinecraft;
import dev.adventurecraft.awakening.extension.client.gui.ExInGameHud;
import dev.adventurecraft.awakening.extension.client.sound.ExSoundHelper;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
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
            target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V",
            shift = At.Shift.BEFORE,
            ordinal = 1))
    private void resetOnExit(Button button, CallbackInfo ci) {
        ((ExInGameHud) this.minecraft.gui).getScriptUI().clear();
        ((ExMinecraft) this.minecraft).setCameraActive(false);
        ((ExSoundHelper) this.minecraft.soundEngine).stopMusic();
    }
}

