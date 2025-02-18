package dev.adventurecraft.awakening.client.gui;

import dev.adventurecraft.awakening.client.options.OptionOF;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;

public class GuiWorldSettingsOF extends GuiBaseSettingsOF {

    public GuiWorldSettingsOF(Screen prevScreen, Options options) {
        super(prevScreen, options, "options.of.worldTitle");
    }

    @Override
    public Option[] getOptions() {
        return new Option[]{
            OptionOF.LOAD_FAR, OptionOF.PRELOADED_CHUNKS,
            OptionOF.CHUNK_UPDATES, OptionOF.CHUNK_UPDATES_DYNAMIC,
            OptionOF.AUTOSAVE_TICKS, OptionOF.TIME,
            OptionOF.WEATHER, OptionOF.FAR_VIEW,
            OptionOF.CHAT_MESSAGE_BUFFER_LIMIT,
            OptionOF.ALLOW_JAVA_IN_SCRIPT};
    }
}
