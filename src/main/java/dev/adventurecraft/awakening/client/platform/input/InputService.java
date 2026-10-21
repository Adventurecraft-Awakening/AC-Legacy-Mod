package dev.adventurecraft.awakening.client.platform.input;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.Callback;
import org.lwjgl.system.MemoryStack;

/** Owns GLFW input and exposes window-local logical coordinates and raw GLFW key codes. */
public final class InputService {
    private static final int EVENT_LIMIT = 1024;
    private static final boolean[] KEYS = new boolean[GLFW.GLFW_KEY_LAST + 1];
    private static final boolean[] BUTTONS = new boolean[GLFW.GLFW_MOUSE_BUTTON_LAST + 1];
    private static final Set<Events> SUBSCRIBERS = new LinkedHashSet<>();
    private static final Set<Runnable> POLLERS = new LinkedHashSet<>();
    private static final Set<Runnable> DETACH_LISTENERS = new LinkedHashSet<>();
    private static long window;
    private static int logicalWidth, logicalHeight;
    private static double cursorX, cursorY, motionX, motionY, scrollX, scrollY;
    private static boolean grabbed, inside;

    private InputService() {}

    public record KeyEvent(int key, int scanCode, boolean pressed, int codePoint, boolean repeat, int modifiers, long nanos) {}
    public record MouseEvent(int button, boolean pressed, double x, double y, double dx, double dy,
                             double scrollX, double scrollY, boolean relative, int modifiers, long nanos) {}

    /** Independent bounded event queues, so a compatibility consumer cannot drain native input. */
    public static final class Events implements AutoCloseable {
        private final ArrayDeque<KeyEvent> keys = new ArrayDeque<>();
        private final ArrayDeque<MouseEvent> mouse = new ArrayDeque<>();
        private boolean closed;
        private Events() {}
        public KeyEvent nextKey() { return keys.pollFirst(); }
        public MouseEvent nextMouse() { return mouse.pollFirst(); }
        public void clearKeys() { keys.clear(); }
        public void clearMouse() { mouse.clear(); }
        @Override public void close() {
            if (!closed) { closed = true; SUBSCRIBERS.remove(this); keys.clear(); mouse.clear(); }
        }
    }

    public static Events subscribe() {
        var events = new Events();
        SUBSCRIBERS.add(events);
        return events;
    }

    public static boolean isAttached() { return window != 0; }
    public static boolean isKeyDown(int key) { return key >= 0 && key < KEYS.length && KEYS[key]; }
    public static boolean isButtonDown(int button) { return button >= 0 && button < BUTTONS.length && BUTTONS[button]; }
    public static double cursorX() { return cursorX; }
    public static double cursorY() { return cursorY; }
    public static double motionX() { return motionX; }
    public static double motionY() { return motionY; }
    public static double scrollX() { return scrollX; }
    public static double scrollY() { return scrollY; }
    public static int logicalWidth() { return logicalWidth; }
    public static int logicalHeight() { return logicalHeight; }
    public static boolean isInsideWindow() { return inside; }
    public static boolean isGrabbed() { return grabbed; }
    public static void addPoller(Runnable poller) { POLLERS.add(poller); }
    public static void removePoller(Runnable poller) { POLLERS.remove(poller); }
    public static void addDetachListener(Runnable listener) { DETACH_LISTENERS.add(listener); }
    public static void removeDetachListener(Runnable listener) { DETACH_LISTENERS.remove(listener); }

    public static void attach(long handle, int width, int height) {
        if (isAttached()) throw new IllegalStateException("Input is already attached to a window");
        window = handle;
        setLogicalSize(width, height);
        Arrays.fill(KEYS, false); Arrays.fill(BUTTONS, false);
        motionX = motionY = scrollX = scrollY = 0;
        grabbed = false;
        inside = GLFW.glfwGetWindowAttrib(window, GLFW.GLFW_HOVERED) == GLFW.GLFW_TRUE;
        try (var stack = MemoryStack.stackPush()) {
            var x = stack.mallocDouble(1); var y = stack.mallocDouble(1);
            GLFW.glfwGetCursorPos(window, x, y);
            cursorX = x.get(0); cursorY = y.get(0);
        }
        if (Boolean.getBoolean("ac.window.noinput")) return;
        if (!Boolean.getBoolean("ac.window.nokeyboard")) {
            free(GLFW.glfwSetKeyCallback(window, (handle1, key, scanCode, action, modifiers) -> {
                boolean pressed = action != GLFW.GLFW_RELEASE;
                if (key >= 0 && key < KEYS.length) KEYS[key] = pressed;
                key(new KeyEvent(key, scanCode, pressed, 0, action == GLFW.GLFW_REPEAT, modifiers, System.nanoTime()));
            }));
            free(GLFW.glfwSetCharCallback(window, (handle1, codePoint) ->
                key(new KeyEvent(-1, 0, true, codePoint, false, 0, System.nanoTime()))));
        }
        if (!Boolean.getBoolean("ac.window.nomouse")) {
            if (GLFW.glfwRawMouseMotionSupported() && !Boolean.getBoolean("org.lwjgl.input.Mouse.disableRawInput"))
                GLFW.glfwSetInputMode(window, GLFW.GLFW_RAW_MOUSE_MOTION, GLFW.GLFW_TRUE);
            free(GLFW.glfwSetMouseButtonCallback(window, (handle1, button, action, modifiers) -> {
                boolean pressed = action == GLFW.GLFW_PRESS;
                if (button >= 0 && button < BUTTONS.length) BUTTONS[button] = pressed;
                mouse(new MouseEvent(button, pressed, cursorX, cursorY, 0, 0, 0, 0, grabbed, modifiers, System.nanoTime()));
            }));
            free(GLFW.glfwSetCursorPosCallback(window, (handle1, x, y) -> {
                double dx = x - cursorX, dy = y - cursorY;
                cursorX = x; cursorY = y; motionX += dx; motionY += dy;
                if (dx != 0 || dy != 0)
                    mouse(new MouseEvent(-1, false, x, y, dx, dy, 0, 0, grabbed, 0, System.nanoTime()));
            }));
            free(GLFW.glfwSetScrollCallback(window, (handle1, x, y) -> {
                scrollX += x; scrollY += y;
                mouse(new MouseEvent(-1, false, cursorX, cursorY, 0, 0, x, y, grabbed, 0, System.nanoTime()));
            }));
            free(GLFW.glfwSetCursorEnterCallback(window, (handle1, entered) -> inside = entered));
        }
    }

    private static void key(KeyEvent event) {
        for (var events : SUBSCRIBERS) {
            if (events.keys.size() == EVENT_LIMIT) events.keys.removeFirst();
            events.keys.addLast(event);
        }
    }
    private static void mouse(MouseEvent event) {
        for (var events : SUBSCRIBERS) {
            if (events.mouse.size() == EVENT_LIMIT) events.mouse.removeFirst();
            events.mouse.addLast(event);
        }
    }

    public static void setLogicalSize(int width, int height) { logicalWidth = width; logicalHeight = height; }
    public static void pollEvents(boolean pollConsumers) {
        GLFW.glfwPollEvents();
        if (pollConsumers) for (var poller : Set.copyOf(POLLERS)) poller.run();
    }
    public static void setGrabbed(boolean value) {
        requireWindow();
        GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, value ? GLFW.GLFW_CURSOR_DISABLED : GLFW.GLFW_CURSOR_NORMAL);
        grabbed = value;
        synchronizeCursor();
    }
    public static void setCursorPosition(double x, double y) {
        requireWindow();
        int mode = GLFW.glfwGetInputMode(window, GLFW.GLFW_CURSOR);
        GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
        GLFW.glfwSetCursorPos(window, x, y);
        GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, mode);
        synchronizeCursor();
    }
    private static void synchronizeCursor() {
        try (var stack = MemoryStack.stackPush()) {
            var x = stack.mallocDouble(1); var y = stack.mallocDouble(1);
            GLFW.glfwGetCursorPos(window, x, y);
            cursorX = x.get(0); cursorY = y.get(0);
        }
    }
    private static void requireWindow() { if (!isAttached()) throw new IllegalStateException("Input requires a window"); }
    private static void free(Callback callback) { if (callback != null) callback.free(); }

    public static void detach() {
        if (!isAttached()) return;
        for (var listener : Set.copyOf(DETACH_LISTENERS)) listener.run();
        free(GLFW.glfwSetKeyCallback(window, null));
        free(GLFW.glfwSetCharCallback(window, null));
        free(GLFW.glfwSetMouseButtonCallback(window, null));
        free(GLFW.glfwSetCursorPosCallback(window, null));
        free(GLFW.glfwSetScrollCallback(window, null));
        free(GLFW.glfwSetCursorEnterCallback(window, null));
        for (var events : SUBSCRIBERS) { events.clearKeys(); events.clearMouse(); }
        Arrays.fill(KEYS, false); Arrays.fill(BUTTONS, false);
        window = 0; grabbed = false; inside = false;
    }
}
