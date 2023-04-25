package dev.adventurecraft.awakening.mixin.client.gui.screen;

import dev.adventurecraft.awakening.common.AC_GuiMapSelect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SelectWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.TranslationStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SelectWorldScreen.class)
public abstract class MixinSelectWorldScreen extends Screen {

    @Shadow
    private ButtonWidget selectButton;

    @Shadow
    private ButtonWidget deleteButton;

    //@Overwrite
    public void addButtonsX() {
        TranslationStorage var1 = TranslationStorage.getInstance();
        this.buttons.add(this.selectButton = new ButtonWidget(1, this.width / 2 - 152, this.height - 28, 100, 20, "Load Save"));
        this.buttons.add(this.deleteButton = new ButtonWidget(2, this.width / 2 - 50, this.height - 28, 100, 20, var1.translate("selectWorld.delete")));
        this.buttons.add(new ButtonWidget(0, this.width / 2 + 52, this.height - 28, 100, 20, var1.translate("gui.cancel")));
        this.selectButton.active = false;
        this.deleteButton.active = false;
    }

    @Redirect(method = "buttonClicked", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Minecraft;openScreen(Lnet/minecraft/client/gui/screen/Screen;)V",
            ordinal = 1))
    private void openAcMapSelect(Minecraft instance, Screen screen) {
        instance.openScreen(new AC_GuiMapSelect(this, ""));
    }

    @Redirect(method = "loadWorld", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Minecraft;openScreen(Lnet/minecraft/client/gui/screen/Screen;)V",
            ordinal = 1))
    private void disableOpenScreen(Minecraft instance, Screen screen) {
    }
}
