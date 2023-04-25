package dev.adventurecraft.awakening.client.gui;

import dev.adventurecraft.awakening.client.options.OptionOF;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widgets.OptionButtonWidget;
import net.minecraft.client.gui.widgets.SliderWidget;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.Option;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.client.util.ScreenScaler;

public class GuiAnimationSettingsOF extends Screen {
    private Screen prevScreen;
    protected String title = "Animation Settings";
    private GameOptions settings;
    private static Option[] enumOptions = new Option[]{OptionOF.ANIMATED_WATER, OptionOF.ANIMATED_LAVA, OptionOF.ANIMATED_FIRE, OptionOF.ANIMATED_PORTAL, OptionOF.ANIMATED_REDSTONE, OptionOF.ANIMATED_EXPLOSION, OptionOF.ANIMATED_FLAME, OptionOF.ANIMATED_SMOKE};

    public GuiAnimationSettingsOF(Screen var1, GameOptions var2) {
        this.prevScreen = var1;
        this.settings = var2;
    }

    public void initVanillaScreen() {
        TranslationStorage var1 = TranslationStorage.getInstance();
        int var2 = 0;
        Option[] var3 = enumOptions;
        int var4 = var3.length;

        for (Option var6 : var3) {
            int var7 = this.width / 2 - 155 + var2 % 2 * 160;
            int var8 = this.height / 6 + 21 * (var2 / 2) - 10;
            if (!var6.isSlider()) {
                this.buttons.add(new OptionButtonWidget(var6.getId(), var7, var8, var6, this.settings.getTranslatedValue(var6)));
            } else {
                this.buttons.add(new SliderWidget(var6.getId(), var7, var8, var6, this.settings.getTranslatedValue(var6), this.settings.getFloatValue(var6)));
            }

            ++var2;
        }

        this.buttons.add(new ButtonWidget(200, this.width / 2 - 100, this.height / 6 + 168 + 11, var1.translate("gui.done")));
    }

    protected void buttonClicked(ButtonWidget var1) {
        if (var1.active) {
            if (var1.id < 100 && var1 instanceof OptionButtonWidget) {
                this.settings.setIntOption(((OptionButtonWidget) var1).getOption(), 1);
                var1.text = this.settings.getTranslatedValue(Option.getById(var1.id));
            }

            if (var1.id == 200) {
                this.client.options.saveOptions();
                this.client.openScreen(this.prevScreen);
            }

            if (var1.id != OptionOF.CLOUD_HEIGHT.ordinal()) {
                ScreenScaler var2 = new ScreenScaler(this.client.options, this.client.actualWidth, this.client.actualHeight);
                int var3 = var2.getScaledWidth();
                int var4 = var2.getScaledHeight();
                this.init(this.client, var3, var4);
            }

        }
    }

    public void render(int var1, int var2, float var3) {
        this.renderBackground();
        this.drawTextWithShadowCentred(this.textRenderer, this.title, this.width / 2, 20, 16777215);
        super.render(var1, var2, var3);
    }
}
