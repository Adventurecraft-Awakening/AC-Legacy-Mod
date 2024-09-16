package dev.adventurecraft.awakening.client.gui;

import dev.adventurecraft.awakening.client.options.OptionOF;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;

public class GuiTextureSettingsOF extends GuiBaseSettingsOF {

    public GuiTextureSettingsOF(Screen prevScreen, Options options) {
        super(prevScreen, options, "options.of.texturesTitle");
    }

    @Override
    public Option[] getOptions() {
        return new Option[]{
            OptionOF.ANIMATED_WATER, OptionOF.ANIMATED_LAVA,
            OptionOF.ANIMATED_FIRE, OptionOF.ANIMATED_PORTAL,
            OptionOF.ANIMATED_REDSTONE, OptionOF.ANIMATED_EXPLOSION,
            OptionOF.ANIMATED_FLAME, OptionOF.ANIMATED_SMOKE,
            OptionOF.MIPMAP_TYPE, OptionOF.MIPMAP_LEVEL,
            OptionOF.AF_LEVEL, OptionOF.LEAVES};
    }
}
