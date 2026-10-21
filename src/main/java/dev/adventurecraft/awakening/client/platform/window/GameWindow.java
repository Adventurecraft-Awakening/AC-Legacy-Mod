// Own GLFW resources and preserve the OpenGL context and input callbacks when changing display modes.
package dev.adventurecraft.awakening.client.platform.window;

import java.nio.ByteBuffer;
import java.util.HashSet;

import dev.adventurecraft.awakening.client.platform.input.InputService;
import dev.adventurecraft.awakening.ACMainThread;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.LWJGLUtil;
import org.lwjgl.glfw.*;
import org.lwjgl.glfw.GLFWVidMode.Buffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.Callback;

public class GameWindow {

    private static final WindowMode desktop_mode;

    private static String title = "Game";

    private static long handle = MemoryUtil.NULL;

    private static boolean resizable = false;

    private static boolean borderless_fullscreen = false;

    private static int swap_interval = 0;

    private static WindowMode current_mode;

    private static int width = 0;

    private static int height = 0;

    private static int framebufferWidth, framebufferHeight;
    private static float contentScaleX = 1, contentScaleY = 1;

    public record LogicalSize(int width, int height) {}
    public record FramebufferExtent(int width, int height) {}
    public record ContentScale(float x, float y) {}

    private static int x = -1;

    private static int y = -1;

    private static boolean fullscreen;

    private static boolean window_resized = false;

    private static boolean window_created = false;

    private static boolean display_mode_pending = false;

    private static boolean glfw_initialized;

    private static boolean context_initialized;

    private static ByteBuffer[] cached_icons = null;

    private GameWindow() {
    }

    static {
        initializeGlfw();

        long monitor = GLFW.glfwGetPrimaryMonitor();
        GLFWVidMode vidMode = monitor == MemoryUtil.NULL ? null : GLFW.glfwGetVideoMode(monitor);
        if (vidMode == null) {
            GLFW.glfwTerminate();
            glfw_initialized = false;
            free(GLFW.glfwSetErrorCallback(null));
            throw new ExceptionInInitializerError("No primary monitor video mode is available");
        }
        desktop_mode = new WindowMode(vidMode.width(), vidMode.height(), vidMode.redBits() + vidMode.greenBits() + vidMode.blueBits(), vidMode.refreshRate());
        current_mode = desktop_mode;
    }

    private static void initializeGlfw() {
        if (glfw_initialized) {
            return;
        }
        GLFWErrorCallback.createPrint(System.err).set();
        if (!GLFW.glfwInit()) {
            free(GLFW.glfwSetErrorCallback(null));
            throw new IllegalStateException("Unable to initialize GLFW");
        }
        glfw_initialized = true;
    }

    public static WindowMode getDisplayMode() {
        return current_mode;
    }

    public static int setIcon(ByteBuffer[] icons) {
        if (cached_icons != icons) {
            cached_icons = new ByteBuffer[icons.length];
            for (int i = 0; i < icons.length; i++) {
                cached_icons[i] = BufferUtils.createByteBuffer(icons[i].capacity());
                int old_position = icons[i].position();
                cached_icons[i].put(icons[i]);
                icons[i].position(old_position);
                cached_icons[i].flip();
            }
        }

        if (isCreated()) {
            GLFW.glfwSetWindowIcon(handle, iconsToGLFWBuffer(cached_icons));
            return 1;
        } else {
            return 0;
        }
    }

    public static WindowMode getDesktopDisplayMode() {
        return desktop_mode;
    }

    private static GLFWImage.Buffer iconsToGLFWBuffer(ByteBuffer[] icons) {
        GLFWImage.Buffer buffer = GLFWImage.create(icons.length);
        for (int i = 0; i < icons.length; i++) {
            ByteBuffer icon = icons[i];
            int size = icon.limit() / 4;
            int dimension = (int) Math.sqrt(size);
            buffer.get(i).set(dimension, dimension, icon);
        }
        return buffer;
    }

    public static void update() throws LWJGLException {
        update(true);
    }

    public static void update(boolean processMessages) throws LWJGLException {
        window_resized = false;
        // Present the frame recorded for the current extent before resize events
        // can replace its target and start an empty frame at the new extent.
        swapBuffers();
        if (display_mode_pending) {
            applyDisplayMode();
            display_mode_pending = false;
        }

        InputService.pollEvents(processMessages);
    }

    public static void swapBuffers() {
        GLFW.glfwSwapBuffers(handle);
    }

    public static void create() throws LWJGLException {
        create(new PixelFormat());
    }

    public static void create(PixelFormat format) throws LWJGLException {
        java.util.Objects.requireNonNull(format, "pixelFormat must be not be null.");
        if (isCreated())
            throw new IllegalStateException("Only one game window may be instantiated at any one time.");

        initializeGlfw();
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_OPENGL_API);
        GLFW.glfwWindowHint(GLFW.GLFW_ALPHA_BITS, format.getAlphaBits());
        GLFW.glfwWindowHint(GLFW.GLFW_DEPTH_BITS, format.getDepthBits());
        GLFW.glfwWindowHint(GLFW.GLFW_STENCIL_BITS, format.getStencilBits());
        GLFW.glfwWindowHint(GLFW.GLFW_SAMPLES, format.getSamples());
        GLFW.glfwWindowHint(GLFW.GLFW_STEREO, format.isStereo() ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_SRGB_CAPABLE, format.isSRGB() ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_DEBUG_CONTEXT, ACMainThread.glDebugContext ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_COCOA_RETINA_FRAMEBUFFER, GLFW.GLFW_FALSE);

        GLFW.glfwWindowHint(GLFW.GLFW_REFRESH_RATE, current_mode.getFrequency());

        handle = GLFW.glfwCreateWindow(current_mode.getWidth(), current_mode.getHeight(), title, MemoryUtil.NULL, MemoryUtil.NULL);
        if (handle == MemoryUtil.NULL) {
            throw new LWJGLException("GameWindow could not be created.");
        }

        free(GLFW.glfwSetWindowSizeCallback(handle, GameWindow::resizeCallback));
        free(GLFW.glfwSetFramebufferSizeCallback(handle, GameWindow::framebufferResizeCallback));
        free(GLFW.glfwSetWindowContentScaleCallback(handle, (window, xScale, yScale) -> {
            contentScaleX = xScale; contentScaleY = yScale;
        }));

        try {
            createWindow();
            readWindowMetrics();
            GLFW.glfwMakeContextCurrent(handle);
            GL.createCapabilities();
            GLFW.glfwSwapInterval(swap_interval);
            context_initialized = true;
        } catch (LWJGLException | RuntimeException | Error e) {
            try {
                destroy();
            } catch (Throwable cleanupError) {
                e.addSuppressed(cleanupError);
            }
            throw e;
        }
    }

    public static void setLocation(int new_x, int new_y) {
        x = new_x;
        y = new_y;

        if (isCreated() && !isFullscreen()) {
            GLFW.glfwSetWindowPos(handle, x, y);
        }
    }

    public static void setFullscreen(boolean fullscreen) throws LWJGLException {
        setDisplayModeAndFullscreenInternal(fullscreen, current_mode);
    }

    public static void setDisplayMode(WindowMode mode) throws LWJGLException {
        setDisplayModeAndFullscreenInternal(fullscreen, mode);
    }

    public static void setDisplayModeAndFullscreen(WindowMode mode) throws LWJGLException {
        setDisplayModeAndFullscreenInternal(mode.isFullscreenCapable(), mode);
    }

    private static void setDisplayModeAndFullscreenInternal(boolean isFullscreen, WindowMode mode) throws LWJGLException {
        if (mode == null)
            throw new NullPointerException("mode must be not be null.");

        WindowMode old_mode = current_mode;
        current_mode = mode;
        boolean was_fullscreen = isFullscreen();
        fullscreen = isFullscreen;
        if (was_fullscreen != isFullscreen() || !mode.equals(old_mode)) {
            if (!isCreated())
                return;
            display_mode_pending = true;
        }
    }

    private static void createWindow() throws LWJGLException {
        if (isCreated()) {
            return;
        }

        window_created = true;

        // Configure GLFW
        GLFW.glfwSetWindowAttrib(handle, GLFW.GLFW_RESIZABLE, resizable ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
        applyDisplayMode();

        readWindowMetrics();
        InputService.attach(handle, width, height);

        // set cached window icon if exists
        if (cached_icons != null) {
            setIcon(cached_icons);
        } else {
            setIcon(new ByteBuffer[]{LWJGLUtil.LWJGLIcon32x32, LWJGLUtil.LWJGLIcon16x16});
        }

        GLFW.glfwShowWindow(handle);
        GLFW.glfwFocusWindow(handle);

        display_mode_pending = false;
    }

    private static void applyDisplayMode() {
        width = current_mode.getWidth();
        height = current_mode.getHeight();
        boolean exclusive = fullscreen && !borderless_fullscreen;
        GLFW.glfwSetWindowAttrib(handle, GLFW.GLFW_DECORATED, fullscreen && borderless_fullscreen ? GLFW.GLFW_FALSE : GLFW.GLFW_TRUE);
        GLFW.glfwSetWindowMonitor(handle, exclusive ? GLFW.glfwGetPrimaryMonitor() : MemoryUtil.NULL,
            getWindowX(), getWindowY(), width, height, current_mode.getFrequency());
    }

    private static void free(Callback callback) {
        if (callback != null) callback.free();
    }

    private static void readWindowMetrics() {
        try (var stack = MemoryStack.stackPush()) {
            var w = stack.mallocInt(1); var h = stack.mallocInt(1);
            GLFW.glfwGetWindowSize(handle, w, h);
            width = w.get(0); height = h.get(0);
            GLFW.glfwGetFramebufferSize(handle, w, h);
            framebufferWidth = w.get(0); framebufferHeight = h.get(0);
            var sx = stack.mallocFloat(1); var sy = stack.mallocFloat(1);
            GLFW.glfwGetWindowContentScale(handle, sx, sy);
            contentScaleX = sx.get(0); contentScaleY = sy.get(0);
        }
        InputService.setLogicalSize(width, height);
    }

    public static WindowMode[] getAvailableDisplayModes() {
        long primaryMonitor = GLFW.glfwGetPrimaryMonitor();
        if (primaryMonitor == MemoryUtil.NULL) {
            return new WindowMode[0];
        }
        Buffer videoModes = GLFW.glfwGetVideoModes(primaryMonitor);
        if (videoModes == null) {
            return new WindowMode[0];
        }
        HashSet<WindowMode> modes = new HashSet<>(videoModes.limit());
        for (int i = 0; i < videoModes.limit(); i++) {
            GLFWVidMode mode = videoModes.get(i);
            modes.add(new WindowMode(mode.width(), mode.height(), mode.redBits() + mode.blueBits() + mode.greenBits(), mode.refreshRate()));
        }
        return modes.toArray(new WindowMode[0]);
    }

    private static void resizeCallback(long window, int width, int height) {
        if (window == handle) {
            window_resized = true;
            GameWindow.width = width;
            GameWindow.height = height;
            InputService.setLogicalSize(width, height);
        }
    }

    private static void framebufferResizeCallback(long window, int width, int height) {
        if (window != handle) return;
        framebufferWidth = width;
        framebufferHeight = height;
        window_resized = true;

    }

    private static void destroyWindow() {
        if (handle == MemoryUtil.NULL) {
            return;
        }
        InputService.detach();
        free(GLFW.glfwSetWindowSizeCallback(handle, null));
        free(GLFW.glfwSetFramebufferSizeCallback(handle, null));
        free(GLFW.glfwSetWindowContentScaleCallback(handle, null));

        try {
            if (context_initialized) {
                GL.setCapabilities(null);
                GLFW.glfwMakeContextCurrent(MemoryUtil.NULL);
            }
        } finally {
            context_initialized = false;
            GLFW.glfwDestroyWindow(handle);
            handle = MemoryUtil.NULL;
            window_created = false;
            display_mode_pending = false;
        }
    }

    public static void destroy() {
        try {
            destroyWindow();
        } finally {
            if (glfw_initialized) {
                GLFW.glfwTerminate();
                glfw_initialized = false;
            }
            free(GLFW.glfwSetErrorCallback(null));
        }
    }

    public static boolean isCreated() {
        return window_created;
    }

    public static boolean isCloseRequested() {
        return GLFW.glfwWindowShouldClose(handle);
    }

    public static boolean isActive() {
        return GLFW.glfwGetWindowAttrib(handle, GLFW.GLFW_ICONIFIED) == 0;
    }

    public static boolean isMaximized() {
        return GLFW.glfwGetWindowAttrib(handle, GLFW.GLFW_MAXIMIZED) != 0;
    }

    public static void setResizable(boolean isResizable) {
        resizable = isResizable;
        if (isCreated()) {
            GLFW.glfwSetWindowAttrib(handle, GLFW.GLFW_RESIZABLE, resizable ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
        }
    }

    public static void setBorderlessFullscreen(boolean isBorderlessFullscreen) {
        borderless_fullscreen = isBorderlessFullscreen;
        display_mode_pending = true;
    }

    public static void sync(int fps) {
        FramePacer.sync(fps);
    }

    public static String getTitle() {
        return title;
    }

    public static void setTitle(String newTitle) {
        if (newTitle == null) {
            newTitle = "";
        }
        title = newTitle;
        if (isCreated()) {
            GLFW.glfwSetWindowTitle(handle, title);
        }
    }

    public static void setVSyncEnabled(boolean enabled) {
        setSwapInterval(enabled ? 1 : 0);
    }

    public static void setSwapInterval(int interval) {
        swap_interval = interval;
        if (context_initialized) {
            GLFW.glfwSwapInterval(swap_interval);
        }
    }


    private static int getWindowX() {
        if (!isFullscreen()) {
            // if no display location set, center window
            if (x == -1) {
                return Math.max(0, (desktop_mode.getWidth() - current_mode.getWidth()) / 2);
            } else {
                return x;
            }
        } else {
            return 0;
        }
    }

    private static int getWindowY() {
        if (!isFullscreen()) {
            // if no display location set, center window
            if (y == -1) {
                return Math.max(0, (desktop_mode.getHeight() - current_mode.getHeight()) / 2);
            } else {
                return y;
            }
        } else {
            return 0;
        }
    }

    public static int getX() {
        if (isFullscreen()) {
            return 0;
        }
        return x;
    }

    public static int getY() {
        if (isFullscreen()) {
            return 0;
        }
        return y;
    }

    public static int getWidth() {
        return width;
    }

    public static int getHeight() {
        return height;
    }

    public static LogicalSize logicalSize() { return new LogicalSize(width, height); }
    public static FramebufferExtent framebufferExtent() { return new FramebufferExtent(framebufferWidth, framebufferHeight); }
    public static ContentScale contentScale() { return new ContentScale(contentScaleX, contentScaleY); }
    public static int getFramebufferWidth() { return framebufferWidth; }
    public static int getFramebufferHeight() { return framebufferHeight; }
    public static float getContentScaleX() { return contentScaleX; }
    public static float getContentScaleY() { return contentScaleY; }
    public static boolean isDrawable() { return isCreated() && framebufferWidth > 0 && framebufferHeight > 0; }

    public static boolean isFullscreen() {
        return fullscreen;
    }

    public static boolean isBorderlessFullscreen() {
        return borderless_fullscreen;
    }

    public static int getSwapInterval() {
        return swap_interval;
    }

    public static boolean wasResized() {
        return window_resized;
    }

    public static long getHandle() {
        return handle;
    }
}
