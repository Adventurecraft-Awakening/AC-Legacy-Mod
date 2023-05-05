package dev.adventurecraft.awakening.mixin.client.gui.screen.menu;

import dev.adventurecraft.awakening.client.gui.GuiWorldSettingsOF;
import dev.adventurecraft.awakening.client.gui.OptionTooltipProvider;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.menu.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.TranslationStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionsScreen.class)
public abstract class MixinOptionsScreen extends Screen implements OptionTooltipProvider {

    @Inject(method = "initVanillaScreen", at = @At("TAIL"))
    private void addWorldSettings(CallbackInfo ci) {
        TranslationStorage ts = TranslationStorage.getInstance();

        int x = this.width / 2 - 100;
        int y = this.height / 6 + 72 + 12;
        this.buttons.add(new ButtonWidget(102, x, y, ts.translate("options.of.world")));
    }

    @Inject(method = "buttonClicked", at = @At("HEAD"))
    private void onWorldButton(ButtonWidget button, CallbackInfo ci) {
        if (!button.active) {
            return;
        }

        if (button.id == 102) {
            this.client.options.saveOptions();
            GuiWorldSettingsOF menu = new GuiWorldSettingsOF(this, this.client.options);
            this.client.openScreen(menu);
        }
    }
}
