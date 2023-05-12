package dev.adventurecraft.awakening.mixin.client.gui.screen.ingame;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import dev.adventurecraft.awakening.common.ClipboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.ChatScreen;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public abstract class MixinChatScreen {

    @Shadow
    protected String getText;

    @Shadow
    private int ticksRan;

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void cancelFirstTick(CallbackInfo ci) {
        if (this.ticksRan < 1) {
            ci.cancel();
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void useClipboard(char character, int key, CallbackInfo ci) {
        if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) ||
            Keyboard.isKeyDown(Keyboard.KEY_RCONTROL) ||
            Keyboard.isKeyDown(Keyboard.KEY_LMETA) ||
            Keyboard.isKeyDown(Keyboard.KEY_RMETA)) {

            if (key == Keyboard.KEY_V) {
                this.getText = ClipboardHandler.getClipboard();
                ci.cancel();
            }

            if (key == Keyboard.KEY_C) {
                ClipboardHandler.setClipboard(this.getText);
                ci.cancel();
            }
        }
    }

    @WrapWithCondition(
        method = "keyPressed",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Minecraft;openScreen(Lnet/minecraft/client/gui/screen/Screen;)V",
            ordinal = 1))
    private boolean closeIfChat(Minecraft instance, Screen screen) {
        return instance.currentScreen instanceof ChatScreen;
    }
}
