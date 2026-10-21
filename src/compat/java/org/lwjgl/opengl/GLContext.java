package org.lwjgl.opengl;

/** Original game ABI; the renderer owns Vulkan resources without an OpenGL context. */
public final class GLContext {
    private static final ContextCapabilities CAPABILITIES = new ContextCapabilities();

    private GLContext() { }

    public static ContextCapabilities getCapabilities() {
        return CAPABILITIES;
    }
}
