package dev.adventurecraft.awakening.client.gui;

import dev.adventurecraft.awakening.client.gui.components.AC_EditBox;
import dev.adventurecraft.awakening.extension.client.gui.screen.ExScreen;
import dev.adventurecraft.awakening.extension.client.render.ExTextRenderer;
import dev.adventurecraft.awakening.layout.IntPoint;
import dev.adventurecraft.awakening.layout.IntRect;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;

import javax.annotation.Nullable;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class SearchPatternBox {

    private final AC_EditBox textBox;
    private final Font font;
    private final Button matchCaseButton;
    private final Button matchWordsButton;
    private final Button useRegexButton;

    private boolean matchCase;
    private boolean matchWords;
    private boolean useRegex;

    private boolean dirty;
    private @Nullable Pattern pattern;
    private @Nullable PatternSyntaxException syntaxError;

    public SearchPatternBox(Screen parent, IntRect layoutRect) {
        this.textBox = new AC_EditBox(layoutRect, "");
        this.font = ((ExScreen) parent).getFont();

        int bX = layoutRect.x - 1;
        int bY = layoutRect.y;
        int bW = layoutRect.h + 1;
        int bH = layoutRect.h - 1;

        this.matchCaseButton = new Button(10001, bX - bW * 3, bY, bW, bH, "");
        this.matchWordsButton = new Button(10002, bX - bW * 2, bY, bW, bH, "");
        this.useRegexButton = new Button(10003, bX - bW, bY, bW, bH, "");

        var buttonList = (List<Button>) parent.buttons;
        buttonList.add(this.matchCaseButton);
        buttonList.add(this.matchWordsButton);
        buttonList.add(this.useRegexButton);

        this.refreshButtonLabels();
    }

    public void markDirty() {
        this.dirty = true;
    }

    public void charTyped(char codepoint, int key) {
        this.textBox.charTyped(codepoint, key);
        this.markDirty();
    }

    public void clicked(IntPoint mouseLocation, int buttonIndex) {
        this.textBox.clicked(mouseLocation.x, mouseLocation.y, buttonIndex);
    }

    public boolean buttonClicked(Button button) {
        if (button.active && this.handleButtonClick(button)) {
            this.markDirty();
            return true;
        }
        return false;
    }

    private boolean handleButtonClick(Button button) {
        if (button == this.matchCaseButton) {
            this.matchCase = !this.matchCase;
            return true;
        }
        else if (button == this.matchWordsButton) {
            this.matchWords = !this.matchWords;
            return true;
        }
        else if (button == this.useRegexButton) {
            this.useRegex = !this.useRegex;
            return true;
        }
        return false;
    }

    public void tick() {
        this.textBox.tick();
    }

    public void render() {
        this.textBox.render(this.font);

        if (this.syntaxError != null) {
            this.renderSyntaxError(this.syntaxError);
        }
    }

    public @Nullable Pattern getPattern() {
        if (this.dirty) {
            String text = this.textBox.getValue();
            if (text == null) {
                text = "";
            }
            this.refresh();
            this.refreshButtonLabels();
            this.pattern = this.compilePattern(text);
            this.dirty = false;
        }
        return this.pattern;
    }

    private @Nullable Pattern compilePattern(String pattern) {
        if (pattern.isEmpty()) {
            return null;
        }

        if (!this.useRegex) {
            pattern = Pattern.quote(pattern);
        }
        if (this.matchWords) {
            pattern = "\\b" + pattern + "\\b";
        }

        int flags = 0;
        if (!this.matchCase) {
            flags |= Pattern.CASE_INSENSITIVE;
        }

        try {
            return Pattern.compile(pattern, flags);
        }
        catch (PatternSyntaxException ex) {
            this.syntaxError = ex;

            var box = this.textBox;
            box.setActiveTextColor(0xff0000);
            box.setInactiveTextColor(0x8f0000);
            return null;
        }
    }

    private void setButtonLabel(Button button, boolean enabled, String message) {
        String style = enabled ? "§a" : "§7";
        button.message = style + message;
    }

    private void refreshButtonLabels() {
        this.setButtonLabel(this.matchCaseButton, this.matchCase, "Cc");
        this.setButtonLabel(this.matchWordsButton, this.matchWords, "W");
        this.setButtonLabel(this.useRegexButton, this.useRegex, ".*");
    }

    private void refresh() {
        this.syntaxError = null;

        var box = this.textBox;
        box.resetActiveTextColor();
        box.resetInactiveTextColor();
    }

    private void renderSyntaxError(PatternSyntaxException error) {
        var text = new StringBuilder();
        text.append(error.getDescription());

        int index = error.getIndex();
        if (index >= 0) {
            text.append(" near index ");
            text.append(index);
        }

        var rect = this.textBox.getRect();
        var font = (ExTextRenderer) this.font;

        int textWidth = font.measureText(text, 0).width();
        int x = rect.x + rect.w - textWidth - 4;
        int y = rect.y + rect.h + 4;
        font.drawString(text, x, y, 0x8f, true);
    }

    public IntRect getBoxRect() {
        return this.textBox.getRect();
    }
}
