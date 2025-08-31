package dev.adventurecraft.awakening.common.gui;

import dev.adventurecraft.awakening.common.Coord;
import dev.adventurecraft.awakening.tile.entity.AC_TileEntityMinMax;
import net.minecraft.client.gui.screens.Screen;

public final class AC_GuiStrings {

    public static String formatMin(Coord min) {
        return String.format("Min: (%d, %d, %d)", min.x, min.y, min.z);
    }

    public static String formatMax(Coord max) {
        return String.format("Max: (%d, %d, %d)", max.x, max.y, max.z);
    }

    public static void drawMinMax(Screen screen, Coord min, Coord max, int x, int y, int color) {
        screen.drawString(screen.font, AC_GuiStrings.formatMin(min), x, y, color);
        screen.drawString(screen.font, AC_GuiStrings.formatMax(max), x, y + 20, color);
    }

    public static void drawMinMax(Screen screen, AC_TileEntityMinMax tile, int x, int y, int color) {
        drawMinMax(screen, tile.min(), tile.max(), x, y, color);
    }
}
