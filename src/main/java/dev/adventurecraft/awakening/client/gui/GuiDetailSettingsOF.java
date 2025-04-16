package dev.adventurecraft.awakening.client.gui;

import dev.adventurecraft.awakening.client.options.OptionOF;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;

public class GuiDetailSettingsOF extends GuiBaseSettingsOF {

    public GuiDetailSettingsOF(Screen prevScreen, Options options) {
        super(prevScreen, options, "options.of.detailsTitle");
    }

    @Override
    public Option[] getOptions() {
        return new Option[]{
            OptionOF.CLOUDS, OptionOF.CLOUD_HEIGHT,
            OptionOF.SKY, OptionOF.STARS,
            OptionOF.FOG_FANCY, OptionOF.FOG_START,
            OptionOF.WATER, OptionOF.RAIN,
            OptionOF.CLEAR_WATER, OptionOF.GRASS_3D,
            OptionOF.GRASS, OptionOF.CONNECTED_GRASS,
            OptionOF.PARTICLE_LIMIT};
    }
}
