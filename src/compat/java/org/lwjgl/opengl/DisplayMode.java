package org.lwjgl.opengl;

import dev.adventurecraft.awakening.client.platform.window.WindowMode;

/** Legacy data type retained at the original game's window boundary. */
public final class DisplayMode extends WindowMode {
    public DisplayMode(int width, int height) { super(width, height); }
    DisplayMode(int width, int height, int bpp, int frequency) { super(width, height, bpp, frequency); }
    private DisplayMode(WindowMode mode) {
        super(mode.getWidth(), mode.getHeight(), mode.getBitsPerPixel(), mode.getFrequency(), mode.isFullscreenCapable());
    }
    static DisplayMode from(WindowMode mode) {
        return mode instanceof DisplayMode legacy ? legacy : new DisplayMode(mode);
    }
}
