package org.lwjgl.opengl;

import dev.adventurecraft.awakening.client.platform.window.GameWindow;
import org.lwjgl.LWJGLException;
import java.nio.ByteBuffer;

/** Binary-compatible window boundary for the original game and bundled input library. */
public final class Display {
    private Display() {}

    public static DisplayMode getDisplayMode() { return DisplayMode.from(GameWindow.getDisplayMode()); }
    public static int setIcon(ByteBuffer[] icons) { return GameWindow.setIcon(icons); }
    public static DisplayMode getDesktopDisplayMode() { return DisplayMode.from(GameWindow.getDesktopDisplayMode()); }
    public static void update() throws LWJGLException { GameWindow.update(); }
    public static void update(boolean processMessages) throws LWJGLException { GameWindow.update(processMessages); }
    public static void swapBuffers() { GameWindow.swapBuffers(); }
    public static void create() throws LWJGLException { GameWindow.create(); dev.adventurecraft.awakening.client.platform.input.legacy.LegacyInputLifecycle.initialize(); }
    public static void create(PixelFormat pixelFormat) throws LWJGLException {
        java.util.Objects.requireNonNull(pixelFormat, "pixelFormat must be not be null.");
        GameWindow.create(pixelFormat);
        dev.adventurecraft.awakening.client.platform.input.legacy.LegacyInputLifecycle.initialize();
    }
    /** GLFW desktop windows do not embed in the legacy applet canvas. */
    public static void setParent(java.awt.Canvas parent) { }
    public static void setLocation(int new_x, int new_y) { GameWindow.setLocation(new_x, new_y); }
    public static void setFullscreen(boolean fullscreen) throws LWJGLException { GameWindow.setFullscreen(fullscreen); }
    public static void setDisplayMode(DisplayMode mode) throws LWJGLException { GameWindow.setDisplayMode(mode); }
    public static void setDisplayModeAndFullscreen(DisplayMode mode) throws LWJGLException { GameWindow.setDisplayModeAndFullscreen(mode); }
    public static DisplayMode[] getAvailableDisplayModes() { return java.util.Arrays.stream(GameWindow.getAvailableDisplayModes()).map(DisplayMode::from).toArray(DisplayMode[]::new); }
    public static void destroy() { GameWindow.destroy(); }
    public static boolean isCreated() { return GameWindow.isCreated(); }
    public static boolean isCloseRequested() { return GameWindow.isCloseRequested(); }
    public static boolean isActive() { return GameWindow.isActive(); }
    public static boolean isMaximized() { return GameWindow.isMaximized(); }
    public static void setResizable(boolean isResizable) { GameWindow.setResizable(isResizable); }
    public static void setBorderlessFullscreen(boolean isBorderlessFullscreen) { GameWindow.setBorderlessFullscreen(isBorderlessFullscreen); }
    public static void sync(int fps) { GameWindow.sync(fps); }
    public static String getTitle() { return GameWindow.getTitle(); }
    public static void setTitle(String newTitle) { GameWindow.setTitle(newTitle); }
    public static void setVSyncEnabled(boolean enabled) { GameWindow.setVSyncEnabled(enabled); }
    public static void setSwapInterval(int interval) { GameWindow.setSwapInterval(interval); }
    public static int getX() { return GameWindow.getX(); }
    public static int getY() { return GameWindow.getY(); }
    public static int getWidth() { return GameWindow.getWidth(); }
    public static int getHeight() { return GameWindow.getHeight(); }
    public static boolean isFullscreen() { return GameWindow.isFullscreen(); }
    public static boolean isBorderlessFullscreen() { return GameWindow.isBorderlessFullscreen(); }
    public static int getSwapInterval() { return GameWindow.getSwapInterval(); }
    public static boolean wasResized() { return GameWindow.wasResized(); }
    public static long getHandle() { return GameWindow.getHandle(); }
}
