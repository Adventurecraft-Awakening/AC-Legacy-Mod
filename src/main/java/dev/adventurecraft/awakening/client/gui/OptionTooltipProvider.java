package dev.adventurecraft.awakening.client.gui;

import dev.adventurecraft.awakening.ACMod;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.Option;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.OptionButton;
import net.minecraft.client.gui.components.SliderButton;
import net.minecraft.locale.I18n;

public interface OptionTooltipProvider {

    Pattern QUOTE_PATTERN = Pattern.compile("(\")(?:\\\\.|[^\\\\])*?\\1");

    Option[] getOptions();

    default List<String> getTooltipLines(GuiComponent element) {
        I18n ts = I18n.getInstance();
        if (element instanceof OptionButton button) {
            Option option = button.getOption();
            String value = ts.get(option.getCaptionId() + ".tooltip");
            return parseOptionTooltip(value);
        }
        if (element instanceof SliderButton slider) {
            Option option = slider.option;
            String value = ts.get(option.getCaptionId() + ".tooltip");
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
