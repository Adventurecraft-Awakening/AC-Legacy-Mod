package dev.adventurecraft.awakening.client.gui;

import dev.adventurecraft.awakening.client.options.OptionOF;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.Option;

public class GuiWorldSettingsOF extends GuiBaseSettingsOF {

    public GuiWorldSettingsOF(Screen prevScreen, GameOptions options) {
        super(prevScreen, options, "options.of.worldTitle");
    }

    @Override
    public Option[] getOptions() {
        return new Option[]{
            OptionOF.LOAD_FAR, OptionOF.PRELOADED_CHUNKS,
            OptionOF.CHUNK_UPDATES, OptionOF.CHUNK_UPDATES_DYNAMIC,
            OptionOF.AUTOSAVE_TICKS, OptionOF.TIME,
            OptionOF.WEATHER, OptionOF.FAR_VIEW};
    }
}
