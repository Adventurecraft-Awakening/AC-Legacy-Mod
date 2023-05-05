package dev.adventurecraft.awakening.client.gui;

import dev.adventurecraft.awakening.ACMod;
import net.minecraft.client.gui.GuiElement;
import net.minecraft.client.gui.widgets.OptionButtonWidget;
import net.minecraft.client.gui.widgets.SliderWidget;
import net.minecraft.client.options.Option;
import net.minecraft.client.resource.language.TranslationStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface OptionTooltipProvider {

    Pattern QUOTE_PATTERN = Pattern.compile("(\")(?:\\\\.|[^\\\\])*?\\1");

    Option[] getOptions();

    default List<String> getTooltipLines(GuiElement element) {
        TranslationStorage ts = TranslationStorage.getInstance();
        if (element instanceof OptionButtonWidget button) {
            Option option = button.getOption();
            String value = ts.translate(option.getTranslationKey() + ".tooltip");
            return parseOptionTooltip(value);
        }
        if (element instanceof SliderWidget slider) {
            Option option = slider.option;
            String value = ts.translate(option.getTranslationKey() + ".tooltip");
            return parseOptionTooltip(value);
        }
        return null;
    }

    static List<String> parseOptionTooltip(String value) {
        Matcher matcher = QUOTE_PATTERN.matcher(value);
        var lines = new ArrayList<String>();
        while (matcher.find()) {
            int start = matcher.start(0) + 1;
            int end = matcher.end(0) - 1;
            String substring = value.substring(start, end);
            String line = substring.replace("\\\"", "\"");
            lines.add(line);
        }
        if (lines.isEmpty()) {
            return null;
        }
        return lines;
    }
}
