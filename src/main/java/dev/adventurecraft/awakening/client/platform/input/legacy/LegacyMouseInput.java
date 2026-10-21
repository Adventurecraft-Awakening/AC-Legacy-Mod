// Modified for AdventureCraft on 2026-10-20: integrate the legacy event adapter into native platform input.
package dev.adventurecraft.awakening.client.platform.input.legacy;

import dev.adventurecraft.awakening.client.platform.input.InputService;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.input.Mouse;

/** Packs native logical-coordinate events into the LWJGL 2 input ABI. */
public final class LegacyMouseInput {
    private InputService.Events events;
    private double motionX, motionY, scroll;
    private double eventDx, eventDy, eventScroll;

    public void createMouse() {
        if (!InputService.isAttached()) throw new IllegalStateException("A window is required for mouse input");
        events = InputService.subscribe();
        resetMotion();
    }
    public void destroyMouse() {
        if (events != null) { events.close(); events = null; }
    }
    private void resetMotion() {
        motionX = InputService.motionX(); motionY = InputService.motionY(); scroll = InputService.scrollY();
    }
    public void pollMouse(IntBuffer coordinates, ByteBuffer buttons) {
        double dx = InputService.motionX() - motionX, dy = InputService.motionY() - motionY;
        if (InputService.isGrabbed()) {
            coordinates.put(0, (int) dx).put(1, (int) -dy);
            // Preserve subpixel movement across polls.
            motionX += (int) dx; motionY += (int) dy;
        } else {
            coordinates.put(0, (int) InputService.cursorX())
                .put(1, InputService.logicalHeight() - 1 - (int) InputService.cursorY());
            motionX = InputService.motionX(); motionY = InputService.motionY();
        }
        int wheel = (int) (InputService.scrollY() - scroll);
        coordinates.put(2, wheel); scroll += wheel;
        for (int i = 0; i < getButtonCount(); i++) buttons.put(i, InputService.isButtonDown(i) ? (byte) 1 : (byte) 0);
    }
    public void readMouse(ByteBuffer target) {
        while (target.remaining() >= Mouse.EVENT_SIZE) {
            var event = events.nextMouse();
            if (event == null) return;
            eventDx += event.dx(); eventDy -= event.dy(); eventScroll += event.scrollY();
            int x = event.relative() ? (int) eventDx : (int) event.x();
            int y = event.relative() ? (int) eventDy : InputService.logicalHeight() - 1 - (int) event.y();
            int wheel = (int) eventScroll; eventScroll -= wheel;
            if (event.relative()) { eventDx -= x; eventDy -= y; }
            else { eventDx = eventDy = 0; }
            target.put((byte) event.button()).put(event.pressed() ? (byte) 1 : (byte) 0)
                .putInt(x).putInt(y).putInt(wheel).putLong(event.nanos());
        }
    }
    public void setCursorPosition(int x, int y) {
        InputService.setCursorPosition(x, InputService.logicalHeight() - 1 - y);
        resetMotion();
    }
    public void grabMouse(boolean grab) {
        InputService.setGrabbed(grab);
        events.clearMouse();
        eventDx = eventDy = eventScroll = 0;
        resetMotion();
    }
    public boolean hasWheel() { return true; }
    public int getButtonCount() { return GLFW.GLFW_MOUSE_BUTTON_LAST + 1; }
    public boolean isInsideWindow() { return InputService.isInsideWindow(); }
}
