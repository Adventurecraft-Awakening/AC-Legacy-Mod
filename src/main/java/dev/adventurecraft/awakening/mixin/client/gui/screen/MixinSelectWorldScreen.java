package dev.adventurecraft.awakening.mixin.client.gui.screen;

import dev.adventurecraft.awakening.client.gamemode.AdventureGameMode;
import dev.adventurecraft.awakening.common.gui.AC_GuiMapSelect;
import dev.adventurecraft.awakening.extension.client.gui.widget.ExScrollableBaseWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gamemode.SurvivalGameMode;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SelectWorldScreen;
import net.minecraft.locale.I18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SelectWorldScreen.class)
public abstract class MixinSelectWorldScreen extends Screen {

    @Shadow private Button selectButton;

    @Shadow private Button deleteButton;

    @Shadow private SelectWorldScreen.WorldSelectionList list;

    //@Overwrite TODO: is this needed?
    public void addButtonsX() {
        I18n var1 = I18n.getInstance();
        this.buttons.add(this.selectButton = new Button(
            1,
            this.width / 2 - 152,
            this.height - 28,
            100,
            20,
            "Load Save"
        ));
        this.buttons.add(this.deleteButton = new Button(
            2,
            this.width / 2 - 50,
            this.height - 28,
            100,
            20,
            var1.get("selectWorld.delete")
        ));
        this.buttons.add(new Button(0, this.width / 2 + 52, this.height - 28, 100, 20, var1.get("gui.cancel")));
        this.selectButton.active = false;
        this.deleteButton.active = false;
    }

    @Redirect(
        method = "buttonClicked",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V",
            ordinal = 1
        )
    )
    private void openAcMapSelect(Minecraft instance, Screen screen) {
        instance.setScreen(new AC_GuiMapSelect(this, ""));
    }

    @Redirect(
        method = "worldSelected",
        at = @At(
            value = "NEW",
            target = "(Lnet/minecraft/client/Minecraft;)Lnet/minecraft/client/gamemode/SurvivalGameMode;"
        )
    )
    private SurvivalGameMode useCustomGameMode(Minecraft minecraft) {
        return new AdventureGameMode(minecraft);
    }

    @Redirect(
        method = "worldSelected",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V",
            ordinal = 1
        )
    )
    private void disableOpenScreen(Minecraft instance, Screen screen) {
    }

    @Override
    public void mouseEvent() {
        super.mouseEvent();
        if (this.list instanceof ExScrollableBaseWidget scrollable) {
            scrollable.onMouseEvent();
        }
    }
}
