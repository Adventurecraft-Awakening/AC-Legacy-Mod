package dev.adventurecraft.awakening.extension.client.gui.components;

import dev.adventurecraft.awakening.layout.IntRect;
import net.minecraft.client.gui.Font;

public interface ExEditBox {

    IntRect getRect();

    Font getFont();

    int getActiveTextColor();

    void setActiveTextColor(int color);

    default void resetActiveTextColor() {
        setActiveTextColor(0xe0e0e0);
    }

    int getInactiveTextColor();

    void setInactiveTextColor(int color);

    default void resetInactiveTextColor() {
        setInactiveTextColor(0x707070);
    }
}
