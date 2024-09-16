package dev.adventurecraft.awakening.mixin.client.gui.screen.menu;

import dev.adventurecraft.awakening.client.gui.GuiWorldSettingsOF;
import dev.adventurecraft.awakening.client.gui.OptionTooltipProvider;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.locale.I18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionsScreen.class)
public abstract class MixinOptionsScreen extends Screen implements OptionTooltipProvider {

    @Inject(method = "init", at = @At("TAIL"))
    private void addWorldSettings(CallbackInfo ci) {
        I18n ts = I18n.getInstance();

        int x = this.width / 2 - 100;
        int y = this.height / 6 + 72 + 12;
        this.buttons.add(new Button(102, x, y, ts.get("options.of.world")));
    }

    @Inject(method = "buttonClicked", at = @At("HEAD"))
    private void onWorldButton(Button button, CallbackInfo ci) {
        if (!button.active) {
            return;
        }

        if (button.id == 102) {
            this.minecraft.options.save();
            GuiWorldSettingsOF menu = new GuiWorldSettingsOF(this, this.minecraft.options);
            this.minecraft.setScreen(menu);
        }
    }
}
