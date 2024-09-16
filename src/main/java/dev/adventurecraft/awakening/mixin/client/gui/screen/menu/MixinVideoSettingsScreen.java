package dev.adventurecraft.awakening.mixin.client.gui.screen.menu;

import dev.adventurecraft.awakening.client.gui.*;
import dev.adventurecraft.awakening.client.options.OptionOF;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.ScreenSizeCalculator;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionButton;
import net.minecraft.client.gui.components.SliderButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.VideoSettingsScreen;
import net.minecraft.locale.I18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = VideoSettingsScreen.class, priority = 999)
public abstract class MixinVideoSettingsScreen extends Screen implements OptionTooltipProvider {

    @Shadow
    private Screen lastScreen;
    @Shadow
    protected String title;
    @Shadow
    private Options config;
    @Shadow
    private static Option[] OPTIONS;

    @Unique
    private static final String[] extraOptions = {
        "options.of.textures",
        "options.of.details",
        "options.of.other"
    };

    private int lastMouseX = 0;
    private int lastMouseY = 0;
    private long mouseStillTime = 0L;

    @Inject(method = "<clinit>", at = @At(value = "TAIL"))
    private static void init_rewriteOptions(CallbackInfo ci) {
        OPTIONS = new Option[]{
            Option.GRAPHICS, Option.RENDER_DISTANCE,
            OptionOF.AO_LEVEL, Option.FRAMERATE_LIMIT,
            Option.ANAGLYPH, Option.VIEW_BOBBING,
            Option.GUI_SCALE, Option.ADVANCED_OPENGL,
            OptionOF.AA_LEVEL, OptionOF.BRIGHTNESS};
    }

    @Override
    public void init() {
        I18n ts = I18n.getInstance();
        this.title = ts.get("options.videoTitle");

        int index = 0;
        Option[] options = OPTIONS;

        for (Option option : options) {
            int x = this.width / 2 - 155 + index % 2 * 160;
            int y = this.height / 6 + 24 * (index / 2);

            int id = option.getId();
            String text = this.config.getMessage(option);

            if (!option.isProgress()) {
                this.buttons.add(new OptionButton(id, x, y, option, text));
            } else {
                this.buttons.add(new SliderButton(id, x, y, option, text, this.config.getProgressValue(option)));
            }

            ++index;
        }

        for (int i = 0; i < extraOptions.length; i++) {
            int x = this.width / 2 - 155 + index % 2 * 160;
            int y = this.height / 6 + 24 * (index / 2);

            this.buttons.add(new Button(100 + i, x, y, 150, 20, ts.get(extraOptions[i])));
            index++;
        }

        this.buttons.add(new Button(200, this.width / 2 - 100, this.height / 6 + 168, ts.get("gui.done")));
    }

    @Override
    public void buttonClicked(Button button) {
        if (!button.active) {
            return;
        }

        if (button.id < 100 && button instanceof OptionButton) {
            this.config.toggle(((OptionButton) button).getOption(), 1);
            button.message = this.config.getMessage(Option.getItem(button.id));
        }

        if (button.id == 200) {
            this.minecraft.options.save();
            this.minecraft.setScreen(this.lastScreen);
        }

        if (button.id == 100) {
            this.minecraft.options.save();
            var var2 = new GuiTextureSettingsOF(this, this.config);
            this.minecraft.setScreen(var2);
        }

        if (button.id == 101) {
            this.minecraft.options.save();
            var var5 = new GuiDetailSettingsOF(this, this.config);
            this.minecraft.setScreen(var5);
        }

        if (button.id == 102) {
            this.minecraft.options.save();
            var var7 = new GuiOtherSettingsOF(this, this.config);
            this.minecraft.setScreen(var7);
        }

        if (button.id != OptionOF.BRIGHTNESS.ordinal() && button.id != OptionOF.AO_LEVEL.ordinal()) {
            var var8 = new ScreenSizeCalculator(this.minecraft.options, this.minecraft.width, this.minecraft.height);
            int var3 = var8.getWidth();
            int var4 = var8.getHeight();
            this.init(this.minecraft, var3, var4);
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float var3) {
        this.renderBackground();
        this.drawCenteredString(this.font, this.title, this.width / 2, 20, 16777215);

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

