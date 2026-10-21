package org.lwjgl.opengl;

import dev.adventurecraft.awakening.client.render.compat.LegacyGlAdapter;

/** Legacy Minecraft error reporting ABI. Rendering is owned by Vulkan. */
public final class Util {
    private Util() { }

    public static void checkGLError() {
        int error = LegacyGlAdapter.getError();
        if (error != 0) throw new OpenGLException(error);
    }

    public static String translateGLErrorString(int error) {
        return switch (error) {
            case 0 -> "No error";
            case 0x0500 -> "Invalid enum";
            case 0x0501 -> "Invalid value";
            case 0x0502 -> "Invalid operation";
            case 0x0503 -> "Stack overflow";
            case 0x0504 -> "Stack underflow";
            case 0x0505 -> "Out of memory";
            case 0x0506 -> "Invalid framebuffer operation";
            case 0x8031 -> "Table too large";
            default -> null;
        };
    }
}
