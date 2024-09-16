package dev.adventurecraft.awakening.client.gui;

import dev.adventurecraft.awakening.client.options.OptionOF;
import java.util.List;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionButton;
import net.minecraft.client.gui.components.SliderButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.locale.I18n;

public abstract class GuiBaseSettingsOF extends Screen implements OptionTooltipProvider {

    protected Screen prevScreen;
    protected String title;
    protected Options options;
    protected int lastMouseX = 0;
    protected int lastMouseY = 0;
    protected long mouseStillTime = 0L;

    public GuiBaseSettingsOF(Screen prevScreen, Options options, String title) {
        this.prevScreen = prevScreen;
        this.options = options;
        this.title = title;
    }

    @Override
    public void init() {
        I18n ts = I18n.getInstance();
        int index = 0;
        Option[] options = getOptions();

        for (Option option : options) {
            int x = this.width / 2 - 155 + index % 2 * 160;
            int y = this.height / 6 + 24 * (index / 2);

            int id = option.getId();
            String text = this.options.getMessage(option);

            if (!option.isProgress()) {
                this.buttons.add(new OptionButton(id, x, y, option, text));
            } else {
                this.buttons.add(new SliderButton(id, x, y, option, text, this.options.getProgressValue(option)));
            }

            ++index;
        }

        this.buttons.add(new Button(200, this.width / 2 - 100, this.height / 6 + 168, ts.get("gui.done")));
    }

    @Override
    protected void buttonClicked(Button button) {
        if (!button.active) {
            return;
        }

        if (button.id < 100 && button instanceof OptionButton opButton) {
            this.options.toggle(opButton.getOption(), 1);
            button.message = this.options.getMessage(Option.getItem(button.id));
        }

        if (button.id == 200) {
            this.minecraft.options.save();
            this.minecraft.setScreen(this.prevScreen);
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float var3) {
        I18n ts = I18n.getInstance();

        this.renderBackground();
        this.drawCenteredString(this.font, ts.get(this.title), this.width / 2, 20, 16777215);

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
        Button button = getSelectedButton(screen.buttons, mouseX, mouseY);
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
            screen.font.drawShadow(line, x + 5, y + 5 + lineIndex * 11, 14540253);
        }
    }

    public static Button getSelectedButton(List<Button> buttons, int x, int y) {
        for (Button button : buttons) {
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
