package dev.adventurecraft.awakening.client.platform.input.legacy;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

/** Initializes original-game input facades at the compatibility boundary. */
public final class LegacyInputLifecycle {
    private LegacyInputLifecycle() {}
    public static void initialize() {
        if (Boolean.getBoolean("ac.window.noinput")) return;
        if (!Boolean.getBoolean("ac.window.nomouse") && !Mouse.isCreated()) {
            try { Mouse.create(); } catch (LWJGLException e) { e.printStackTrace(System.err); }
        }
        if (!Boolean.getBoolean("ac.window.nokeyboard") && !Keyboard.isCreated()) {
            try { Keyboard.create(); } catch (LWJGLException e) { e.printStackTrace(System.err); }
        }
    }
}
