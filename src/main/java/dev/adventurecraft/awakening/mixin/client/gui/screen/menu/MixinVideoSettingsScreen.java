package dev.adventurecraft.awakening.mixin.client.gui.screen.menu;

import dev.adventurecraft.awakening.client.gui.*;
import dev.adventurecraft.awakening.client.options.OptionOF;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.menu.VideoSettingsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widgets.OptionButtonWidget;
import net.minecraft.client.gui.widgets.SliderWidget;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.Option;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.client.util.ScreenScaler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = VideoSettingsScreen.class, priority = 999)
public abstract class MixinVideoSettingsScreen extends Screen implements OptionTooltipProvider {

    @Shadow
    private Screen parent;
    @Shadow
    protected String title;
    @Shadow
    private GameOptions options;
    @Shadow
    private static Option[] OPTIONS;

    private int lastMouseX = 0;
    private int lastMouseY = 0;
    private long mouseStillTime = 0L;

    @Inject(method = "<clinit>", at = @At(value = "TAIL"))
    private static void init_rewriteOptions(CallbackInfo ci) {
        OPTIONS = new Option[]{
            Option.GRAPHICS_QUALITY, Option.RENDER_DISTANCE,
            OptionOF.AO_LEVEL, Option.LIMIT_FRAMERATE,
            Option.ANAGLYPH, Option.VIEW_BOBBING,
            Option.GUI_SCALE, Option.ADVANCED_OPENGL,
            OptionOF.AA_LEVEL, OptionOF.BRIGHTNESS};
    }

    public void initVanillaScreen() {
        TranslationStorage ts = TranslationStorage.getInstance();
        this.title = ts.translate("options.videoTitle");

        int index = 0;
        Option[] options = OPTIONS;

        for (Option option : options) {
            int x = this.width / 2 - 155 + index % 2 * 160;
            int y = this.height / 6 + 21 * (index / 2) - 10;

            int id = option.getId();
            String text = this.options.getTranslatedValue(option);

            if (!option.isSlider()) {
                this.buttons.add(new OptionButtonWidget(id, x, y, option, text));
            } else {
                this.buttons.add(new SliderWidget(id, x, y, option, text, this.options.getFloatValue(option)));
            }

            ++index;
        }

        int y = this.height / 6 + 21 * (index / 2) - 10;
        int x = this.width / 2 - 155;
        this.buttons.add(new ButtonWidget(100, x, y, 150, 20, ts.translate("options.of.textures")));

        x = this.width / 2 - 155 + 160;
        this.buttons.add(new ButtonWidget(101, x, y, 150, 20, ts.translate("options.of.details")));

        y += 21;
        x = this.width / 2 - 155;
        this.buttons.add(new ButtonWidget(103, x, y, 150, 20, ts.translate("options.of.other")));

        this.buttons.add(new ButtonWidget(200, this.width / 2 - 100, this.height / 6 + 168, ts.translate("gui.done")));
    }

    public void buttonClicked(ButtonWidget button) {
        if (!button.active) {
            return;
        }

        if (button.id < 100 && button instanceof OptionButtonWidget) {
            this.options.setIntOption(((OptionButtonWidget) button).getOption(), 1);
            button.text = this.options.getTranslatedValue(Option.getById(button.id));
        }

        if (button.id == 200) {
            this.client.options.saveOptions();
            this.client.openScreen(this.parent);
        }

        if (button.id == 100) {
            this.client.options.saveOptions();
            GuiTextureSettingsOF var2 = new GuiTextureSettingsOF(this, this.options);
            this.client.openScreen(var2);
        }

        if (button.id == 101) {
            this.client.options.saveOptions();
            GuiDetailSettingsOF var5 = new GuiDetailSettingsOF(this, this.options);
            this.client.openScreen(var5);
        }

        if (button.id == 103) {
            this.client.options.saveOptions();
            GuiOtherSettingsOF var7 = new GuiOtherSettingsOF(this, this.options);
            this.client.openScreen(var7);
        }

        if (button.id != OptionOF.BRIGHTNESS.ordinal() && button.id != OptionOF.AO_LEVEL.ordinal()) {
            ScreenScaler var8 = new ScreenScaler(this.client.options, this.client.actualWidth, this.client.actualHeight);
            int var3 = var8.getScaledWidth();
            int var4 = var8.getScaledHeight();
            this.init(this.client, var3, var4);
        }
    }

    public void render(int mouseX, int mouseY, float var3) {
        this.renderBackground();
        this.drawTextWithShadowCentred(this.textRenderer, this.title, this.width / 2, 20, 16777215);

        super.render(mouseX, mouseY, var3);

        if (Math.abs(mouseX - this.lastMouseX) > 5 ||
            Math.abs(mouseY - this.lastMouseY) > 5) {
            this.lastMouseX = mouseX;
            this.lastMouseY = mouseY;
            this.mouseStillTime = System.currentTimeMillis();
            return;
        }

        short hoverDelay = 700;
        if (System.currentTimeMillis() >= this.mouseStillTime + hoverDelay) {
            GuiBaseSettingsOF.renderTooltip(this, mouseX, mouseY);
        }
    }
}

