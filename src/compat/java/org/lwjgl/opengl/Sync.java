package org.lwjgl.opengl;

import dev.adventurecraft.awakening.client.platform.window.FramePacer;

/** Legacy frame pacing boundary. */
public final class Sync {
    private Sync() {}
    public static void sync(int fps) { FramePacer.sync(fps); }
}
