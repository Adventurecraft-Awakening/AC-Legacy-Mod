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

import java.util.List;

public abstract class GuiBaseSettingsOF extends Screen implements OptionTooltipProvider {

    protected Screen prevScreen;
    protected String title;
    protected GameOptions options;
    protected int lastMouseX = 0;
    protected int lastMouseY = 0;
    protected long mouseStillTime = 0L;

    public GuiBaseSettingsOF(Screen prevScreen, GameOptions options, String title) {
        this.prevScreen = prevScreen;
        this.options = options;
        this.title = title;
    }

    @Override
    public void initVanillaScreen() {
        TranslationStorage ts = TranslationStorage.getInstance();
        int index = 0;
        Option[] options = getOptions();

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

        this.buttons.add(new ButtonWidget(200, this.width / 2 - 100, this.height / 6 + 168, ts.translate("gui.done")));
    }

    @Override
    protected void buttonClicked(ButtonWidget button) {
        if (!button.active) {
            return;
        }

        if (button.id < 100 && button instanceof OptionButtonWidget) {
            this.options.setIntOption(((OptionButtonWidget) button).getOption(), 1);
            button.text = this.options.getTranslatedValue(Option.getById(button.id));
        }

        if (button.id == 200) {
            this.client.options.saveOptions();
            this.client.openScreen(this.prevScreen);
        }

        if (button.id != OptionOF.CLOUD_HEIGHT.ordinal()) {
            ScreenScaler scaler = new ScreenScaler(this.client.options, this.client.actualWidth, this.client.actualHeight);
            int width = scaler.getScaledWidth();
            int hight = scaler.getScaledHeight();
            this.init(this.client, width, hight);
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float var3) {
        TranslationStorage ts = TranslationStorage.getInstance();

        this.renderBackground();
        this.drawTextWithShadowCentred(this.textRenderer, ts.translate(this.title), this.width / 2, 20, 16777215);

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
            renderTooltip(this, mouseX, mouseY);
        }
    }

    public static <S extends Screen & OptionTooltipProvider> void renderTooltip(S screen, int mouseX, int mouseY) {
        int x = screen.width / 2 - 150;
        int y = screen.height / 6 - 5;
        if (mouseY <= y + 98) {
            y += 105;
        }

        int xEnd = x + 150 + 150;
        int yEnd = y + 84 + 10;
        ButtonWidget button = getSelectedButton(screen.buttons, mouseX, mouseY);
        if (button == null) {
            return;
        }

        List<String> tooltipLines = screen.getTooltipLines(button);
        if (tooltipLines == null) {
            return;
        }

        screen.fillGradient(x, y, xEnd, yEnd, -536870912, -536870912);

        for (int lineIndex = 0; lineIndex < tooltipLines.size(); ++lineIndex) {
            String line = tooltipLines.get(lineIndex);
            screen.textRenderer.drawTextWithShadow(line, x + 5, y + 5 + lineIndex * 11, 14540253);
        }
    }

    public static ButtonWidget getSelectedButton(List<ButtonWidget> buttons, int x, int y) {
        for (ButtonWidget button : buttons) {
            boolean hit = x >= button.x &&
                y >= button.y &&
                x < button.x + button.width &&
                y < button.y + button.height;

            if (hit) {
                return button;
            }
        }
        return null;
    }
}
