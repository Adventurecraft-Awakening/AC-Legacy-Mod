package dev.adventurecraft.awakening.extension.client.gui.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;

public interface ExScreen {

    Minecraft getMinecraft();

    Font getFont();

    boolean isDisabledInputGrabbing();

    void setDisabledInputGrabbing(boolean value);
}
