package dev.adventurecraft.awakening.client.gui;

import dev.adventurecraft.awakening.client.options.OptionOF;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.Option;

public class GuiOtherSettingsOF extends GuiBaseSettingsOF {

    public GuiOtherSettingsOF(Screen prevScreen, GameOptions options) {
        super(prevScreen, options, "options.of.otherTitle");
    }

    @Override
    public Option[] getOptions() {
        return new Option[]{
            OptionOF.SMOOTH_FPS, OptionOF.SMOOTH_INPUT,
            OptionOF.FAST_DEBUG_INFO, OptionOF.AUTO_FAR_CLIP};
    }
}
