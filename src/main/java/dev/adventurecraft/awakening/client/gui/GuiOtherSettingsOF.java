package dev.adventurecraft.awakening.client.gui;

import dev.adventurecraft.awakening.client.options.OptionOF;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;

public class GuiOtherSettingsOF extends GuiBaseSettingsOF {

    public GuiOtherSettingsOF(Screen prevScreen, Options options) {
        super(prevScreen, options, "options.of.otherTitle");
    }

    @Override
    public Option[] getOptions() {
        return new Option[]{
            OptionOF.SMOOTH_FPS, OptionOF.SMOOTH_INPUT,
            OptionOF.FAST_DEBUG_INFO, OptionOF.AUTO_FAR_CLIP};
    }
}
